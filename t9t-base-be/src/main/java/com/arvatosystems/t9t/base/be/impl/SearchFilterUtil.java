/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import java.util.ArrayList;
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
    private ISearchTools searchTools = Jdp.getRequired(ISearchTools.class);

    private boolean hasDirectWantedChild(AndFilter andFilter, Set<String> wantedFieldNames) {
        List<SearchFilter> childFilters = Arrays.asList(andFilter.getFilter1(), andFilter.getFilter2());

        return childFilters.stream().anyMatch(childFilter -> {
            if (childFilter instanceof FieldFilter) {
                return wantedFieldNames.contains(((FieldFilter)childFilter).getFieldName());
            }
            return false;
        });
    }

    private boolean hasDirectWantedChild(OrFilter orFilter, Set<String> wantedFieldNames) {
        List<SearchFilter> childFilters = Arrays.asList(orFilter.getFilter1(), orFilter.getFilter2());

        return childFilters.stream().anyMatch(childFilter -> {
            if (childFilter instanceof FieldFilter) {
                return wantedFieldNames.contains(((FieldFilter)childFilter).getFieldName());
            }
            return false;
        });
    }

    private boolean hasWantedChild(NotFilter notFilter, Set<String> wantedFieldNames) {
        return searchTools.containsFieldPathElements((SearchFilter)notFilter, new ArrayList<>(wantedFieldNames));
    }

    private boolean hasMoreThanOneWantedFieldNames(SearchFilter searchFilter, Set<String> wantedFieldNames) {
         Set<String> fieldNames = searchTools.getAllSearchFilterFieldName(searchFilter, new HashSet<>());

        return fieldNames.stream().filter(fieldName -> {
            return wantedFieldNames.contains(fieldName);
        }).count() > 1;
    }

    public void populateQueue(SearchFilter searchFilter, Set<String> wantedFieldNames, Queue<SearchFilter> filterQueue) {

        if (searchFilter instanceof NotFilter) {
            if (hasWantedChild((NotFilter)searchFilter, wantedFieldNames)) {
                filterQueue.add(new NotFilter());
                populateQueue(((NotFilter) searchFilter).getFilter(), wantedFieldNames, filterQueue);
            }

        } else if (searchFilter instanceof AndFilter) {
            if (hasDirectWantedChild((AndFilter)searchFilter, wantedFieldNames) && hasMoreThanOneWantedFieldNames(searchFilter, wantedFieldNames)) {
                filterQueue.add(new AndFilter());
            }

            //process child tree
            List<SearchFilter> childFilters = Arrays.asList(((AndFilter)searchFilter).getFilter1(), ((AndFilter)searchFilter).getFilter2());
            for (SearchFilter childFilter: childFilters) {
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
            List<SearchFilter> childFilters = Arrays.asList(((OrFilter)searchFilter).getFilter1(), ((OrFilter)searchFilter).getFilter2());
            for (SearchFilter childFilter : childFilters) {
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

    private boolean isPredicate(SearchFilter searchFilter) {
        return searchFilter != null && (searchFilter instanceof AndFilter || searchFilter instanceof OrFilter || searchFilter instanceof NotFilter);
    }


    private boolean addOnNextEmptyFilterField(SearchFilter copyFilter, SearchFilter newFilterToBeAdded) {
        if (copyFilter instanceof AndFilter) {
            //always try on the outer predicate first

            if ((isPredicate(((AndFilter)copyFilter).getFilter2()) && addOnNextEmptyFilterField(((AndFilter)copyFilter).getFilter2(), newFilterToBeAdded)) == false) {
                if ((isPredicate(((AndFilter)copyFilter).getFilter1()) && addOnNextEmptyFilterField(((AndFilter)copyFilter).getFilter1(), newFilterToBeAdded)) == false) {
                    return setChildFilter((AndFilter)copyFilter, newFilterToBeAdded);
                } else
                    return true;
            } else
                return true;

        } else if (copyFilter instanceof OrFilter) {
            if ((isPredicate(((OrFilter)copyFilter).getFilter2()) && addOnNextEmptyFilterField(((OrFilter)copyFilter).getFilter2(), newFilterToBeAdded)) == false) {
                if ((isPredicate(((OrFilter)copyFilter).getFilter1()) && addOnNextEmptyFilterField(((OrFilter)copyFilter).getFilter1(), newFilterToBeAdded)) == false) {
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

    public SearchFilter generateSearchFilterFromQueue(Queue<SearchFilter> filterQueue, SearchFilter newSearchFilter) {

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

    private boolean setChildFilter(AndFilter andFilter, SearchFilter childFilter) {
        if (andFilter.getFilter1() == null) {
            andFilter.setFilter1(childFilter);
            return true;
        } else if (andFilter.getFilter2() == null) {
            andFilter.setFilter2(childFilter);
            return true;
        }
        return false;
    }


    private boolean setChildFilter(OrFilter andFilter, SearchFilter childFilter) {
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
    public SearchFilter selectFiltersBasedOnFieldName(SearchFilter searchFilter, Set<String> fieldNamesWanted) {
        Queue<SearchFilter> filterQueue = new LinkedList<>();
        populateQueue(searchFilter, fieldNamesWanted, filterQueue);
        return generateSearchFilterFromQueue(filterQueue, null);

    }

}
