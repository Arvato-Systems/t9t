/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import com.arvatosystems.t9t.base.search.SearchCriteria;

import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface ISearchTools {

    default Function<String, String> mapMapper(final Map<String, String> nameMappings) {
        if (nameMappings == null) {
            // return identity function
            return fieldName -> fieldName;
        } else {
            // return mapping function
            return fieldName -> {
                final String replacement = nameMappings.get(fieldName);
                return replacement == null ? fieldName : replacement;
            };
        }
    }

    /**
     * Maps field names in search requests, for use by other search engines than the DB, for example SOLR.
     * Injectable in order to allow extension by additional filter types.
     */
    void mapNames(SearchCriteria searchCriteria, Function<String, String> mapper);
    void mapNames(SearchFilter searchFilter,     Function<String, String> mapper);
    void mapNames(List<SortColumn> sortColumns,  Function<String, String> mapper);

    /**
     * Maps field names in search requests, for use by other search engines than the DB, for example SOLR.
     * Injectable in order to allow extension by additional filter types.
     */
    void mapNames(SearchCriteria searchCriteria, Map<String, String> nameMappings);
    void mapNames(SearchFilter searchFilter,     Map<String, String> nameMappings);
    void mapNames(List<SortColumn> sortColumns,  Map<String, String> nameMappings);

    /* Checks is a certain path element is part of a field name. */
    boolean containsFieldPathElements(SearchCriteria searchCriteria, Collection<String> pathElements);
    boolean containsFieldPathElements(SearchFilter searchFilter,     Collection<String> pathElements);
    boolean containsFieldPathElements(List<SortColumn> sortColumns,  Collection<String> pathElements);

    /**
     * Searches if the expression contains an AND condition with a FieldFilter for name fieldname.
     * Returns true if found, else false.
     * Also, the first occurrence is processed via the optional provided lambda.
     *
     * @param searchCriteria
     * @param fieldname
     * @param converter
     * @return
     */
    boolean searchForAndFieldname(@Nonnull SearchCriteria searchCriteria, @Nonnull String fieldname, @Nonnull Function<SearchFilter, SearchFilter> converter);

    /**
     * Retrieves all FieldFilter fieldNames of the SearchFilter to the set of fieldNames
     *
     * @param searchFilter
     * @param fieldNames
     * @return
     */
    @Nullable Set<String> getAllSearchFilterFieldName(@Nullable SearchFilter searchFilter, @Nonnull Set<String> fieldNames);

    /**
     * Searches the filter tree for FieldFilters with the given fieldName and returns the first one found.
     *
     * @param searchFilter   the input filter tree
     * @param fieldName      the name the fieldFilter shoudl apply to
     * @return               the first FieldFilter found, or null if none found
     */
    FieldFilter getFieldFilterByFieldName(@Nullable SearchFilter searchFilter, @Nonnull String fieldName);

    /**
     * Searches for AsciiFilters or UnicodeFilters which have an equals condition referencing the passed fieldName
     * and replaces them with a more effective filter.
     *
     * @param searchCriteria
     * @param fieldName
     * @param replacer
     */
    void optimizeSearchFiltersString(@Nonnull SearchCriteria searchCriteria, @Nonnull String fieldName, Function<String, SearchFilter> replacer);

    /**
     * Searches for UuidFilters which have an equals condition referencing the passed fieldName
     * and replaces them with a more effective filter.
     *
     * @param searchCriteria
     * @param fieldName
     * @param replacer
     */
    void optimizeSearchFiltersUuid(@Nonnull SearchCriteria searchCriteria, String fieldName, Function<UUID, SearchFilter> replacer);

    /**
     * Replaces all FieldFilters in the tree by the result of applying the replacer function.
     * Returns a newly constructed tree (the passed in tree can be frozen).
     *
     * @param searchFilter   the input tree
     * @param replacer       the function to replace the FieldFilter by its substitute (the function may return the input parameter)
     * @return               the new tree, with replaced filters (if any)
     */
    SearchFilter replaceFieldFilters(@Nullable SearchFilter searchFilter, @Nonnull Function<FieldFilter, FieldFilter> replacer);
}
