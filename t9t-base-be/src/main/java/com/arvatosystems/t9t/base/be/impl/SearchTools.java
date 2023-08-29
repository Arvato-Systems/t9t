/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ISearchTools;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;
import de.jpaw.dp.Singleton;

@Singleton
public class SearchTools implements ISearchTools {

    protected boolean containsFieldPathElements(final String fn, final Collection<String> pathElements) {
        for (final String pe : pathElements) {
            if (fn.contains(pe)) {
                return true;
            }
        }
        return false;
    }

    protected boolean searchForAndFieldname(final SearchFilter searchFilter, final String fieldname, final Function<SearchFilter, SearchFilter> converter,
            final Consumer<SearchFilter> assigner) {
        if (searchFilter instanceof FieldFilter ff) {
            if (ff.getFieldName().equals(fieldname)) {
                if (converter != null) {
                    assigner.accept(converter.apply(searchFilter));
                }
                return true;
            }
            return false;
        }
        if (searchFilter instanceof AndFilter andFilter) {
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
        if (searchFilter instanceof FieldFilter fieldFilter) {
            fieldFilter.setFieldName(mapper.apply(fieldFilter.getFieldName()));
        } else if (searchFilter instanceof NotFilter notFilter) {
            mapNames(notFilter.getFilter(), mapper);
        } else if (searchFilter instanceof AndFilter andFilter) {
            mapNames(andFilter.getFilter1(), mapper);
            mapNames(andFilter.getFilter2(), mapper);
        } else if (searchFilter instanceof OrFilter orFilter) {
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
    public boolean containsFieldPathElements(final SearchCriteria searchCriteria, final Collection<String> pathElements) {
        return (containsFieldPathElements(searchCriteria.getSortColumns(), pathElements)
             || containsFieldPathElements(searchCriteria.getSearchFilter(), pathElements));
    }

    @Override
    public boolean containsFieldPathElements(final SearchFilter searchFilter, final Collection<String> pathElements) {
        if (searchFilter == null) {
            return false;
        }
        if (searchFilter instanceof FieldFilter fieldFilter) {
            return containsFieldPathElements(fieldFilter.getFieldName(), pathElements);
        } else if (searchFilter instanceof NotFilter notFilter) {
            return containsFieldPathElements(notFilter.getFilter(), pathElements);
        } else if (searchFilter instanceof AndFilter andFilter) {
            return containsFieldPathElements(andFilter.getFilter1(), pathElements) || containsFieldPathElements(andFilter.getFilter2(), pathElements);
        } else if (searchFilter instanceof OrFilter orFilter) {
            return containsFieldPathElements(orFilter.getFilter1(), pathElements) || containsFieldPathElements(orFilter.getFilter2(), pathElements);
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
        }
    }

    @Override
    public boolean containsFieldPathElements(final List<SortColumn> sortColumns, final Collection<String> pathElements) {
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
        if (searchFilter instanceof FieldFilter fieldFilter) {
            fieldNames.add(fieldFilter.getFieldName());
            return fieldNames;
        } else if (searchFilter instanceof NotFilter notFilter) {
            return getAllSearchFilterFieldName(notFilter.getFilter(), fieldNames);
        } else if (searchFilter instanceof AndFilter andFilter) {
            final Set<String> names = getAllSearchFilterFieldName(andFilter.getFilter1(), fieldNames);
            return getAllSearchFilterFieldName(andFilter.getFilter2(), names);
        } else if (searchFilter instanceof OrFilter orFilter) {
            final Set<String> names = getAllSearchFilterFieldName(orFilter.getFilter1(), fieldNames);
            return getAllSearchFilterFieldName(orFilter.getFilter2(), names);
        } else {
            throw new RuntimeException("Unimplemented search extension " + searchFilter.getClass().getCanonicalName());
        }
    }

    @Override
    public FieldFilter getFieldFilterByFieldName(final SearchFilter searchFilter, final String fieldName) {
        if (searchFilter != null) {
            if (searchFilter instanceof FieldFilter fieldFilter) {
                if (fieldFilter.getFieldName().equals(fieldName)) {
                    return fieldFilter;
                }
            } else if (searchFilter instanceof NotFilter notFilter) {
                return getFieldFilterByFieldName(notFilter.getFilter(), fieldName);
            } else if (searchFilter instanceof AndFilter andFilter) {
                final FieldFilter fieldFilter = getFieldFilterByFieldName(andFilter.getFilter1(), fieldName);
                if (fieldFilter != null) {
                    return fieldFilter;
                } else {
                    return getFieldFilterByFieldName(andFilter.getFilter2(), fieldName);
                }
            } else if (searchFilter instanceof OrFilter orFilter) {
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

    protected <T> void descendAndReplace(final SearchFilter searchFilter, final Function<SearchFilter, T> checker,
            final Function<T, SearchFilter> replacer, final Consumer<SearchFilter> updater) {
        final T value = checker.apply(searchFilter);
        if (value != null) {
            updater.accept(replacer.apply(value));
        } else if (searchFilter instanceof NotFilter nf) {
            descendAndReplace(nf.getFilter(), checker, replacer, s -> nf.setFilter(s));
        } else if (searchFilter instanceof AndFilter af) {
            descendAndReplace(af.getFilter1(), checker, replacer, s -> af.setFilter1(s));
            descendAndReplace(af.getFilter2(), checker, replacer, s -> af.setFilter2(s));
        } else if (searchFilter instanceof OrFilter of) {
            descendAndReplace(of.getFilter1(), checker, replacer, s -> of.setFilter1(s));
            descendAndReplace(of.getFilter2(), checker, replacer, s -> of.setFilter2(s));
        }
    }

    @Override
    public void optimizeSearchFiltersString(final SearchCriteria searchCriteria, final String fieldName, final Function<String, SearchFilter> replacer) {
        if (searchCriteria.getSearchFilter() != null) {
            final Function<SearchFilter, String> checker = s -> {
                if (s instanceof UnicodeFilter uf) {
                    if (fieldName.equals(uf.getFieldName()) && uf.getEqualsValue() != null) {
                        return uf.getEqualsValue();
                    }
                } else if (s instanceof AsciiFilter af) {
                    if (fieldName.equals(af.getFieldName()) && af.getEqualsValue() != null) {
                        return af.getEqualsValue();
                    }
                }
                return null;
            };
            descendAndReplace(searchCriteria.getSearchFilter(), checker, replacer, s -> searchCriteria.setSearchFilter(s));
        }
    }

    @Override
    public void optimizeSearchFiltersUuid(final SearchCriteria searchCriteria, final String fieldName, final Function<UUID, SearchFilter> replacer) {
        if (searchCriteria.getSearchFilter() != null) {
            final Function<SearchFilter, UUID> checker = s -> {
                if (s instanceof UuidFilter uf) {
                    if (fieldName.equals(uf.getFieldName()) && uf.getEqualsValue() != null) {
                        return uf.getEqualsValue();
                    }
                }
                return null;
            };
            descendAndReplace(searchCriteria.getSearchFilter(), checker, replacer, s -> searchCriteria.setSearchFilter(s));
        }
    }
}
