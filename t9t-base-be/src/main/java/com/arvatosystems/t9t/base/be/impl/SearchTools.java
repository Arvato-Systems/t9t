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
package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ISearchTools;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

@Singleton
public class SearchTools implements ISearchTools {

    protected boolean containsFieldPathElements(final String fn, final List<String> pathElements) {
        for (final String pe : pathElements) {
            if (fn.contains(pe)) {
                return true;
            }
        }
        return false;
    }

    protected boolean searchForAndFieldname(final SearchFilter searchFilter, final String fieldname, final Function<SearchFilter, SearchFilter> converter,
            final Consumer<SearchFilter> assigner) {
        if (searchFilter instanceof FieldFilter) {
            if (((FieldFilter) searchFilter).getFieldName().equals(fieldname)) {
                if (converter != null) {
                    assigner.accept(converter.apply(searchFilter));
                }
                return true;
            }
            return false;
        }
        if (searchFilter instanceof AndFilter) {
            final AndFilter andFilter = (AndFilter) searchFilter;
            boolean b1 = searchForAndFieldname(andFilter.getFilter1(), fieldname, converter, (final SearchFilter sf) -> andFilter.setFilter1(sf));
            boolean b2 = searchForAndFieldname(andFilter.getFilter2(), fieldname, converter, (final SearchFilter sf) -> andFilter.setFilter2(sf));
            return b1 || b2;
        }
        return false;
    }

    @Override
    public void mapNames(final SearchCriteria searchCriteria, final Function<String, String> mapper) {
     // tree walk search filter to replace field names
        if (searchCriteria.getSearchFilter() != null) {
            mapNames(searchCriteria.getSearchFilter(), mapper);
        }

        // replace sort columnn names
        if (searchCriteria.getSortColumns() != null) {
            mapNames(searchCriteria.getSortColumns(), mapper);
        }
    }

    @Override
    public void mapNames(final SearchFilter searchFilter, final Function<String, String> mapper) {
        if (searchFilter == null) {
            return;
        }
        if (searchFilter instanceof FieldFilter) {
            final FieldFilter fieldFilter = (FieldFilter) searchFilter;
            fieldFilter.setFieldName(mapper.apply(fieldFilter.getFieldName()));
        } else if (searchFilter instanceof NotFilter) {
            mapNames(((NotFilter) searchFilter).getFilter(), mapper);
        } else if (searchFilter instanceof AndFilter) {
            final AndFilter andFilter = (AndFilter) searchFilter;
            mapNames(andFilter.getFilter1(), mapper);
            mapNames(andFilter.getFilter2(), mapper);
        } else if (searchFilter instanceof OrFilter) {
            final OrFilter orFilter = (OrFilter) searchFilter;
            mapNames(orFilter.getFilter1(), mapper);
            mapNames(orFilter.getFilter2(), mapper);
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
        }
    }

    @Override
    public void mapNames(final List<SortColumn> sortColumns, final Function<String, String> mapper) {
        for (final SortColumn column: sortColumns) {
            column.setFieldName(mapper.apply(column.getFieldName()));
        }
    }

    @Override
    public void mapNames(final SearchCriteria searchCriteria, final Map<String, String> nameMappings) {
        // processing of filter names (mapping to names defined in SOLR)
        if (nameMappings != null) {
            mapNames(searchCriteria, mapMapper(nameMappings));
        }
    }

    @Override
    public void mapNames(final SearchFilter searchFilter, final Map<String, String> nameMappings) {
        mapNames(searchFilter, mapMapper(nameMappings));
    }

    @Override
    public void mapNames(final List<SortColumn> sortColumns, final Map<String, String> nameMappings) {
        mapNames(sortColumns, mapMapper(nameMappings));
    }

    @Override
    public boolean containsFieldPathElements(final SearchCriteria searchCriteria, final List<String> pathElements) {
        return (containsFieldPathElements(searchCriteria.getSortColumns(), pathElements)
             || containsFieldPathElements(searchCriteria.getSearchFilter(), pathElements));
    }

    @Override
    public boolean containsFieldPathElements(final SearchFilter searchFilter, final List<String> pathElements) {
        if (searchFilter == null) {
            return false;
        }
        if (searchFilter instanceof FieldFilter) {
            return containsFieldPathElements(((FieldFilter) searchFilter).getFieldName(), pathElements);
        } else if (searchFilter instanceof NotFilter) {
            return containsFieldPathElements(((NotFilter) searchFilter).getFilter(), pathElements);
        } else if (searchFilter instanceof AndFilter) {
            final AndFilter andFilter = (AndFilter) searchFilter;
            return containsFieldPathElements(andFilter.getFilter1(), pathElements) || containsFieldPathElements(andFilter.getFilter2(), pathElements);
        } else if (searchFilter instanceof OrFilter) {
            final OrFilter orFilter = (OrFilter) searchFilter;
            return containsFieldPathElements(orFilter.getFilter1(), pathElements) || containsFieldPathElements(orFilter.getFilter2(), pathElements);
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
        }
    }

    @Override
    public boolean containsFieldPathElements(final List<SortColumn> sortColumns, final List<String> pathElements) {
        if (pathElements == null || pathElements.isEmpty()) {
            return false;
        }
        if (sortColumns == null || sortColumns.isEmpty()) {
            return false;
        }
        for (final SortColumn sc : sortColumns) {
            if (containsFieldPathElements(sc.getFieldName(), pathElements)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean searchForAndFieldname(final SearchCriteria searchCriteria, final String fieldname, final Function<SearchFilter, SearchFilter> converter) {
        final SearchFilter searchFilter = searchCriteria.getSearchFilter();
        if (searchFilter == null) {
            return false;
        }

        // check for top level match
        return searchForAndFieldname(searchFilter, fieldname, converter, (final SearchFilter sf) -> searchCriteria.setSearchFilter(sf));
    }

    @Override
    public Set<String> getAllSearchFilterFieldName(final SearchFilter searchFilter, final Set<String> fieldNames) {
        if (searchFilter == null) {
            return null;
        }
        if (searchFilter instanceof FieldFilter) {
            fieldNames.add(((FieldFilter) searchFilter).getFieldName());
            return fieldNames;
        } else if (searchFilter instanceof NotFilter) {
            return getAllSearchFilterFieldName(((NotFilter) searchFilter).getFilter(), fieldNames);
        } else if (searchFilter instanceof AndFilter) {
            final AndFilter andFilter = (AndFilter) searchFilter;
            final Set<String> names = getAllSearchFilterFieldName(andFilter.getFilter1(), fieldNames);
            return getAllSearchFilterFieldName(andFilter.getFilter2(), names);
        } else if (searchFilter instanceof OrFilter) {
            final OrFilter orFilter = (OrFilter) searchFilter;
            final Set<String> names = getAllSearchFilterFieldName(orFilter.getFilter1(), fieldNames);
            return getAllSearchFilterFieldName(orFilter.getFilter2(), names);
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
        }
    }

    @Override
    public FieldFilter getFieldFilterByFieldName(final SearchFilter searchFilter, final String fieldName) {
        if (searchFilter != null) {
            if (searchFilter instanceof FieldFilter) {
                final FieldFilter fieldFilter = (FieldFilter) searchFilter;
                if (fieldFilter.getFieldName().equals(fieldName)) {
                    return fieldFilter;
                }
            } else if (searchFilter instanceof NotFilter) {
                final NotFilter notFilter = (NotFilter) searchFilter;
                return getFieldFilterByFieldName(notFilter.getFilter(), fieldName);
            } else if (searchFilter instanceof AndFilter) {
                final AndFilter andFilter = (AndFilter) searchFilter;
                final FieldFilter fieldFilter = getFieldFilterByFieldName(andFilter.getFilter1(), fieldName);
                if (fieldFilter != null) {
                    return fieldFilter;
                } else {
                    return getFieldFilterByFieldName(andFilter.getFilter2(), fieldName);
                }
            } else if (searchFilter instanceof OrFilter) {
                final OrFilter orFilter = (OrFilter) searchFilter;
                final FieldFilter fieldFilter = getFieldFilterByFieldName(orFilter.getFilter1(), fieldName);
                if (fieldFilter != null) {
                    return fieldFilter;
                } else {
                    return getFieldFilterByFieldName(orFilter.getFilter2(), fieldName);
                }
            } else {
                throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
            }
        }
        return null;
    }
}
