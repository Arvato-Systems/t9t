/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.arvatosystems.t9t.base.services.ISearchFilterUtil;
import com.arvatosystems.t9t.base.services.ISearchTools;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class SearchFilterUtil implements ISearchFilterUtil {
    private final ISearchTools searchTools = Jdp.getRequired(ISearchTools.class);

    private boolean isFieldFilterOfName(final SearchFilter filter, final Set<String> wantedFieldNames) {
        return filter instanceof FieldFilter ff && wantedFieldNames.contains(ff.getFieldName());
    }

    private boolean hasDirectWantedChild(final AndFilter andFilter, final Set<String> wantedFieldNames) {
        return isFieldFilterOfName(andFilter.getFilter1(), wantedFieldNames) || isFieldFilterOfName(andFilter.getFilter2(), wantedFieldNames); 
    }

    private boolean hasDirectWantedChild(final OrFilter orFilter, final Set<String> wantedFieldNames) {
        return isFieldFilterOfName(orFilter.getFilter1(), wantedFieldNames) || isFieldFilterOfName(orFilter.getFilter2(), wantedFieldNames); 
    }

    private boolean hasWantedChild(final NotFilter notFilter, final Set<String> wantedFieldNames) {
        return searchTools.containsFieldPathElements(notFilter, wantedFieldNames);
    }

    private boolean hasMoreThanOneWantedFieldNames(final SearchFilter searchFilter, final Set<String> wantedFieldNames) {
        final Set<String> fieldNames = searchTools.getAllSearchFilterFieldName(searchFilter, new HashSet<>());

        return fieldNames.stream().filter(fieldName -> {
            return wantedFieldNames.contains(fieldName);
        }).count() > 1;
    }

    public void populateQueue(final SearchFilter searchFilter, final Set<String> wantedFieldNames, final Queue<SearchFilter> filterQueue) {

        if (searchFilter instanceof NotFilter nf) {
            if (hasWantedChild(nf, wantedFieldNames)) {
                filterQueue.add(new NotFilter());
                populateQueue(nf.getFilter(), wantedFieldNames, filterQueue);
            }

        } else if (searchFilter instanceof AndFilter) {
            if (hasDirectWantedChild((AndFilter)searchFilter, wantedFieldNames) && hasMoreThanOneWantedFieldNames(searchFilter, wantedFieldNames)) {
                filterQueue.add(new AndFilter());
            }

            //process child tree
            final List<SearchFilter> childFilters = Arrays.asList(((AndFilter)searchFilter).getFilter1(), ((AndFilter)searchFilter).getFilter2());
            for (final SearchFilter childFilter: childFilters) {
                if (childFilter instanceof FieldFilter) {
                    if (wantedFieldNames.contains(((FieldFilter)childFilter).getFieldName())) {
                        filterQueue.add(childFilter);
                    }

                } else if (childFilter instanceof NotFilter || childFilter instanceof AndFilter || childFilter instanceof OrFilter) {
                    populateQueue(childFilter, wantedFieldNames, filterQueue);
                }
            }

        } else if (searchFilter instanceof OrFilter) {
            if (hasDirectWantedChild((OrFilter)searchFilter, wantedFieldNames) && hasMoreThanOneWantedFieldNames(searchFilter, wantedFieldNames)) {
                filterQueue.add(new OrFilter());
            }

            //process child tree
            final List<SearchFilter> childFilters = Arrays.asList(((OrFilter)searchFilter).getFilter1(), ((OrFilter)searchFilter).getFilter2());
            for (final SearchFilter childFilter : childFilters) {
                if (childFilter instanceof FieldFilter) {
                    if (wantedFieldNames.contains(((FieldFilter)childFilter).getFieldName())) {
                        filterQueue.add(childFilter);
                    }

                } else if (childFilter instanceof NotFilter || childFilter instanceof AndFilter || childFilter instanceof OrFilter) {
                    populateQueue(childFilter, wantedFieldNames, filterQueue);
                }
            }

        } else if (searchFilter instanceof FieldFilter) {
            if (wantedFieldNames.contains(((FieldFilter)searchFilter).getFieldName())) {
                filterQueue.add(searchFilter);
            }
        }
    }

    private boolean isPredicate(final SearchFilter searchFilter) {
        return searchFilter != null && (searchFilter instanceof AndFilter || searchFilter instanceof OrFilter || searchFilter instanceof NotFilter);
    }


    private boolean addOnNextEmptyFilterField(final SearchFilter copyFilter, final SearchFilter newFilterToBeAdded) {
        if (copyFilter instanceof AndFilter) {
            //always try on the outer predicate first

            if (!(isPredicate(((AndFilter)copyFilter).getFilter2())
              && addOnNextEmptyFilterField(((AndFilter)copyFilter).getFilter2(), newFilterToBeAdded))) {
                if (!(isPredicate(((AndFilter)copyFilter).getFilter1())
                  && addOnNextEmptyFilterField(((AndFilter)copyFilter).getFilter1(), newFilterToBeAdded))) {
                    return setChildFilter((AndFilter)copyFilter, newFilterToBeAdded);
                } else
                    return true;
            } else
                return true;

        } else if (copyFilter instanceof OrFilter) {
            if (!(isPredicate(((OrFilter)copyFilter).getFilter2())
              && addOnNextEmptyFilterField(((OrFilter)copyFilter).getFilter2(), newFilterToBeAdded))) {
                if (!(isPredicate(((OrFilter)copyFilter).getFilter1())
                  && addOnNextEmptyFilterField(((OrFilter)copyFilter).getFilter1(), newFilterToBeAdded))) {
                    return setChildFilter((OrFilter)copyFilter, newFilterToBeAdded);
                } else
                    return true;
            } else
                return true;

        } else if (copyFilter instanceof NotFilter) {
            if (((NotFilter)copyFilter).getFilter() == null) {
                ((NotFilter)copyFilter).setFilter(newFilterToBeAdded);
                return true;
            } else {
                return addOnNextEmptyFilterField(((NotFilter)copyFilter).getFilter(), newFilterToBeAdded);
            }
        }
        return false;
    }

    public SearchFilter generateSearchFilterFromQueue(final Queue<SearchFilter> filterQueue, SearchFilter newSearchFilter) {

        SearchFilter currSearchFilter = filterQueue.poll();
        while (currSearchFilter != null) {

            if (newSearchFilter == null) {
                newSearchFilter = currSearchFilter;
            } else {
                //it may be part of a predicate find an empty predicate to assign
                addOnNextEmptyFilterField(newSearchFilter, currSearchFilter);
            }
            currSearchFilter = filterQueue.poll();
        }
        return newSearchFilter;
    }

    private boolean setChildFilter(final AndFilter andFilter, final SearchFilter childFilter) {
        if (andFilter.getFilter1() == null) {
            andFilter.setFilter1(childFilter);
            return true;
        } else if (andFilter.getFilter2() == null) {
            andFilter.setFilter2(childFilter);
            return true;
        }
        return false;
    }


    private boolean setChildFilter(final OrFilter andFilter, final SearchFilter childFilter) {
        if (andFilter.getFilter1() == null) {
            andFilter.setFilter1(childFilter);
            return true;
        } else if (andFilter.getFilter2() == null) {
            andFilter.setFilter2(childFilter);
            return true;
        }
        return false;
    }

    @Override
    public SearchFilter selectFiltersBasedOnFieldName(final SearchFilter searchFilter, final Set<String> fieldNamesWanted) {
        final Queue<SearchFilter> filterQueue = new LinkedList<>();
        populateQueue(searchFilter, fieldNamesWanted, filterQueue);
        return generateSearchFilterFromQueue(filterQueue, null);

    }
}
