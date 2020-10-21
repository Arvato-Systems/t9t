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

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.apiw.Ref;

/** Describes a factory for a Dropdown Combobox which is populated via a DB query.
 * By convention, LeanSearchRequests are used.
 *
 * @since 3.0.2
 */
public interface IDropdown28DbFactory<REF extends BonaPortable> extends IDropdown28BasicFactory<Dropdown28Db<REF>> {

    /** Returns an instance of the search request which queries data. */
    LeanSearchRequest getSearchRequest();

    /** Creates a type safe Ref object which contains the Ref. */
    REF createRef(Long ref);

    /** Creates a type safe Ref (Key) object by alphanumeric identifier. */
    REF createKey(String id);

    /** Creates a type safe Ref (Key) object by description. Sets ID as well as reference. */
    default REF createKey(Description desc) {
        if (desc == null) {
            return null;
        } else {
            final REF ref = createKey(desc.getId());
            if (ref instanceof Ref) {
                ((Ref) ref).setObjectRef(desc.getObjectRef());
                return ref;
            } else {
                // in these cases, the method must be overwritten
                throw new IllegalArgumentException("Must implement createKey for type " + ref.getClass());
            }
        }
    }

    /** Returns the alphanumeric identifier of a data type reference. */
    default String getIdFromData(REF data, Dropdown28Db<REF> instance) {
        if (data instanceof Ref) {
            Description desc = instance.lookupByRef(((Ref)data).getObjectRef());
            return desc == null ? null : desc.getId();
        } else {
            // in these cases, the method must be overwritten
            throw new IllegalArgumentException("Must implement getIdFromData for type " + data.getClass());
        }
    }

    /** Returns the identifier if the REF is a KEY. */
    String getIdFromKey(REF key);
}
