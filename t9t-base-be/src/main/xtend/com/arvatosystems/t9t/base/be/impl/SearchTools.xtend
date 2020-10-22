/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.arvatosystems.t9t.base.be.impl

import com.arvatosystems.t9t.base.search.SearchCriteria
import com.arvatosystems.t9t.base.services.ISearchTools
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.FieldFilter
import de.jpaw.bonaparte.pojos.api.NotFilter
import de.jpaw.bonaparte.pojos.api.OrFilter
import de.jpaw.bonaparte.pojos.api.SearchFilter
import de.jpaw.bonaparte.pojos.api.SortColumn
import de.jpaw.dp.Singleton
import java.util.List
import java.util.Map
import java.util.Set
import java.util.function.Consumer
import java.util.function.Function

@Singleton
class SearchTools implements ISearchTools {

    def protected containsFieldPathElements(String fn, List<String> pathElements) {
        for (pe : pathElements)
            if (fn.contains(pe))
                return true
        return false
    }

    override containsFieldPathElements(List<SortColumn> sortColumns, List<String> pathElements) {
        if (pathElements.nullOrEmpty)
            return false
        if (sortColumns.nullOrEmpty)
            return false
        for (sc : sortColumns) {
            if (sc.fieldName.containsFieldPathElements(pathElements))
                return true
        }
        return false;
    }

    override containsFieldPathElements(SearchFilter searchFilter, List<String> pathElements) {
        if (searchFilter === null)
            return false
        if (searchFilter instanceof FieldFilter) {
            return searchFilter.fieldName.containsFieldPathElements(pathElements)
        } else if (searchFilter instanceof NotFilter) {
            return searchFilter.filter.containsFieldPathElements(pathElements)
        } else if (searchFilter instanceof AndFilter) {
            return searchFilter.filter1.containsFieldPathElements(pathElements) || searchFilter.filter2.containsFieldPathElements(pathElements)
        } else if (searchFilter instanceof OrFilter) {
            return searchFilter.filter1.containsFieldPathElements(pathElements) || searchFilter.filter2.containsFieldPathElements(pathElements)
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.class.canonicalName);
        }
    }

    override containsFieldPathElements(SearchCriteria sc, List<String> pathElements) {
        return sc.sortColumns.containsFieldPathElements(pathElements) || sc.searchFilter.containsFieldPathElements(pathElements)
    }

    override Set<String> getAllSearchFilterFieldName(SearchFilter searchFilter, Set<String> fieldNames) {

        if (searchFilter === null)
            return null
        if (searchFilter instanceof FieldFilter) {
            fieldNames.add(searchFilter.fieldName)
            return fieldNames
        } else if (searchFilter instanceof NotFilter) {
            return getAllSearchFilterFieldName(searchFilter.filter, fieldNames)
        } else if (searchFilter instanceof AndFilter) {
            var names = getAllSearchFilterFieldName(searchFilter.filter1, fieldNames)
            return getAllSearchFilterFieldName(searchFilter.filter2, names)
        } else if (searchFilter instanceof OrFilter) {
            var names = getAllSearchFilterFieldName(searchFilter.filter1, fieldNames)
            return getAllSearchFilterFieldName(searchFilter.filter2, names)
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.class.canonicalName);
        }
    }

    override mapNames(SearchFilter searchFilter, Map<String, String> nameMappings) {
        mapNames(searchFilter, mapMapper(nameMappings))
    }

    override mapNames(List<SortColumn> sc, Map<String, String> nameMappings) {
        mapNames(sc, mapMapper(nameMappings))
    }

    override mapNames(SearchCriteria sc, Map<String, String> nameMappings) {
        // processing of filter names (mapping to names defined in SOLR)
        if (nameMappings !== null) {
            mapNames(sc, mapMapper(nameMappings))
        }
    }

    override mapNames(SearchCriteria sc, Function<String, String> mapper) {
        // tree walk search filter to replace field names
        sc.searchFilter?.mapNames(mapper)

        // replace sort columnn names
        sc.sortColumns?.mapNames(mapper)
    }

    override mapNames(SearchFilter searchFilter, Function<String, String> mapper) {
        if (searchFilter === null)
            return;
        if (searchFilter instanceof FieldFilter) {
            searchFilter.fieldName = mapper.apply(searchFilter.fieldName)
        } else if (searchFilter instanceof NotFilter) {
            searchFilter.filter.mapNames(mapper)
        } else if (searchFilter instanceof AndFilter) {
            searchFilter.filter1.mapNames(mapper)
            searchFilter.filter2.mapNames(mapper)
        } else if (searchFilter instanceof OrFilter) {
            searchFilter.filter1.mapNames(mapper)
            searchFilter.filter2.mapNames(mapper)
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.class.canonicalName);
        }
    }

    override mapNames(List<SortColumn> sortColumns, Function<String, String> mapper) {
        sortColumns.forEach[ fieldName = mapper.apply(fieldName) ]
    }

    def protected boolean searchForAndFieldname(SearchFilter f, String fieldname, Function<SearchFilter, SearchFilter> converter, Consumer<SearchFilter> assigner) {
        if (f instanceof FieldFilter) {
            if (f.fieldName == fieldname) {
                if (converter !== null)
                    assigner.accept(converter.apply(f))
                return true;
            }
            return false;
        }
        if (f instanceof AndFilter) {
            val b1 = searchForAndFieldname(f.filter1, fieldname, converter, [ f.filter1 = it])
            val b2 = searchForAndFieldname(f.filter2, fieldname, converter, [ f.filter2 = it])
            return b1 || b2
        }
        return false;
    }

    override searchForAndFieldname(SearchCriteria searchCriteria, String fieldname, Function<SearchFilter, SearchFilter> converter) {
        val f = searchCriteria.searchFilter
        if (f === null)
            return false
        // check for top level match
        return searchForAndFieldname(f, fieldname, converter, [ searchCriteria.searchFilter = it])
    }

    override FieldFilter getFieldFilterByFieldName(SearchFilter searchFilter, String fieldName) {
        if (searchFilter !== null) {
            if (searchFilter instanceof FieldFilter) {
                if (searchFilter.fieldName.equals(fieldName)) {
                    return searchFilter
                }
            } else if (searchFilter instanceof NotFilter) {
                return getFieldFilterByFieldName(searchFilter.filter, fieldName)
            } else if (searchFilter instanceof AndFilter) {
                var fieldFilter = getFieldFilterByFieldName(searchFilter.filter1, fieldName)
                return fieldFilter ?: getFieldFilterByFieldName(searchFilter.filter2, fieldName)
            } else if (searchFilter instanceof OrFilter) {
                var fieldFilter = getFieldFilterByFieldName(searchFilter.filter1, fieldName)
                return fieldFilter ?: getFieldFilterByFieldName(searchFilter.filter2, fieldName)
            } else {
                throw new RuntimeException("Unimplemented search extension " + searchFilter.class.canonicalName);
            }
        }
    }

}
