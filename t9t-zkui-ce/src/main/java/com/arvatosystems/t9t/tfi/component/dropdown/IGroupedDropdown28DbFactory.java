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
package com.arvatosystems.t9t.tfi.component.dropdown;

import com.arvatosystems.t9t.base.search.LeanGroupedSearchRequest;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;

import de.jpaw.bonaparte.pojos.apiw.Ref;

/** Describes a factory for a Grouped Dropdown Combobox which is populated via a DB query.
 * By convention, LeanSearchRequests are used.
 *
 * @since 4.7
 */
public interface IGroupedDropdown28DbFactory<REF extends Ref> extends IDropdown28BasicFactory<GroupedDropdown28Db<REF>> {

    /** Returns an instance of the search request which queries data. */
    LeanGroupedSearchRequest getSearchRequest();

    Long getGroup();

    /** Creates a type safe Ref object which contains the Ref. */
    REF createRef(Long ref);

    /** Creates a type safe Ref (Key) object by alphanumeric identifier. */
    REF createKey(String id);

    /** Returns the identifier if the REF is a KEY. */
    String getIdFromKey(REF key);
}
