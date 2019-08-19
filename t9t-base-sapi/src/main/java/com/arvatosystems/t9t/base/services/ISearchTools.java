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
package com.arvatosystems.t9t.base.services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;

public interface ISearchTools {

    default Function<String, String> mapMapper(final Map<String, String> nameMappings) {
        if (nameMappings == null)
            // return identity function
            return fieldName -> fieldName;
        else
            // return mapping function
            return fieldName -> { String replacement = nameMappings.get(fieldName); return replacement == null ? fieldName : replacement; };
    }

    /** Maps field names in search requests, for use by other search engines than the DB, for example SOLR.
     * Injectable in order to allow extension by additional filter types. */
    void mapNames(SearchCriteria searchCriteria, final Function<String, String> mapper);
    void mapNames(SearchFilter searchFilter,     final Function<String, String> mapper);
    void mapNames(List<SortColumn> sortColumns,  final Function<String, String> mapper);

    /** Maps field names in search requests, for use by other search engines than the DB, for example SOLR.
     * Injectable in order to allow extension by additional filter types. */
    void mapNames(SearchCriteria searchCriteria, final Map<String, String> nameMappings);
    void mapNames(SearchFilter searchFilter,     final Map<String, String> nameMappings);
    void mapNames(List<SortColumn> sortColumns,  final Map<String, String> nameMappings);

    /* Checks is a certain path element is part of a field name. */
    boolean containsFieldPathElements(SearchCriteria searchCriteria, final List<String> pathElements);
    boolean containsFieldPathElements(SearchFilter searchFilter,     final List<String> pathElements);
    boolean containsFieldPathElements(List<SortColumn> sortColumns,  final List<String> pathElements);

    /** Searches if the expression contains an AND condition with a FieldFilter for name fieldname.
     * Returns true if found, else false.
     * Also, the first occurrence is processed via the optional provided lambda.
     *
     * @param searchCriteria
     * @param fieldname
     * @param converter
     * @return
     */
    boolean searchForAndFieldname(SearchCriteria searchCriteria, String fieldname, Function<SearchFilter,SearchFilter> converter);

    /**
     * retrieve all FieldFilter fieldNames of the SearchFilter to the set of fieldNames
     * @param searchFilter
     * @param fieldNames
     * @return
     */
    Set<String> getAllSearchFilterFieldName(SearchFilter searchFilter, Set<String> fieldNames);

    FieldFilter getFieldFilterByFieldName(SearchFilter searchFilter, String fieldName);
}
