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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.camunda.bpm.engine.query.Query;

import com.arvatosystems.t9t.base.search.SearchRequest;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;

public class SearchRequestMapper<QUERY extends Query<QUERY, DATA>, DATA> {

    private final Map<String, BiFunction<FieldFilter, QUERY, QUERY>> fieldFilterMappings = new HashMap<>();
    private final Map<String, BiFunction<SortColumn, QUERY, QUERY>> sortMappings = new HashMap<>();

    public <FIELD extends FieldFilter> void addFilterMapping(String fieldName, Class<FIELD> filterType, BiFunction<FIELD, QUERY, QUERY> mapper) {
        fieldFilterMappings.put(fieldName, (fieldFilter, query) -> {

            if (fieldFilter.getFieldName()
                           .equals(fieldName)) {

                if (!filterType.isAssignableFrom(fieldFilter.getClass())) {
                    throw new IllegalArgumentException("Field " + fieldName + " can only by filtered using a filter of type " + filterType.getSimpleName());
                }

                final QUERY newQuery = mapper.apply(filterType.cast(fieldFilter), query);

                if (newQuery == null) {
                    throw new IllegalArgumentException("Provided filter settings are not suitable for field " + fieldName);
                }

                return newQuery;
            }

            return query;
        });
    }

    public void addSortMapping(String fieldName, Function<QUERY, QUERY> mapper) {
        sortMappings.put(fieldName, (sortColumn, query) -> {

            if (sortColumn.getFieldName()
                          .equals(fieldName)) {

                QUERY newQuery = mapper.apply(query);

                if (newQuery == null) {
                    throw new IllegalArgumentException("Field " + sortColumn.getFieldName() + " not available for sort");
                }

                if (sortColumn.getDescending()) {
                    newQuery = newQuery.desc();
                } else {
                    newQuery = newQuery.asc();
                }

                return newQuery;
            }

            return query;
        });
    }



    private QUERY transferSearchParameters(QUERY query, SearchRequest<?, ?> request) {

        for (FieldFilter filter : convertToAndFilterList(request.getSearchFilter())) {
            final BiFunction<FieldFilter, QUERY, QUERY> mapping = fieldFilterMappings.get(filter.getFieldName());

            if (mapping != null) {
                query = mapping.apply(filter, query);
            }
        }

        if (request.getSortColumns() != null) {
            for (SortColumn sort : request.getSortColumns()) {
                final BiFunction<SortColumn, QUERY, QUERY> mapping = sortMappings.get(sort.getFieldName());

                if (mapping != null) {
                    query = mapping.apply(sort, query);
                }
            }
        }

        return query;
    }

    public List<DATA> search(QUERY query, SearchRequest<?, ?> request) {
        query = transferSearchParameters(query, request);

        return query.listPage(request.getOffset(), request.getLimit());
    }

    private static void convertToAndFilterList(SearchFilter filter, List<FieldFilter> result) {
        if (filter instanceof AndFilter) {
            final AndFilter and = (AndFilter) filter;

            convertToAndFilterList(and.getFilter1(), result);
            convertToAndFilterList(and.getFilter2(), result);
        } else if (filter instanceof FieldFilter) {
            result.add((FieldFilter) filter);
        } else {
            throw new IllegalArgumentException("Only FieldFilter and AndFilter are supported");
        }
    }

    private static List<FieldFilter> convertToAndFilterList(SearchFilter filter) {
        final List<FieldFilter> result = new LinkedList<>();

        if (filter != null) {
            convertToAndFilterList(filter, result);
        }

        return result;
    }
}
