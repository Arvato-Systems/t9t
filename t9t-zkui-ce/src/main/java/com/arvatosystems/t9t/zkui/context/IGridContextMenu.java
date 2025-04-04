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
package com.arvatosystems.t9t.zkui.context;

import com.arvatosystems.t9t.zkui.components.basic.Grid28;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;

/** Implementations of this interface are obtained as Singletons via Jdp,
 * using a qualifier which is the id of the context menu plus "#" plus the option name.
 */
public interface IGridContextMenu<DTO extends BonaPortable> {
    /** Invoked on onOpen() of the context menu, determines if the option is available for this row or not (greying out the option). */
    default boolean isEnabled(final DataWithTracking<DTO, TrackingBase> dwt) {
        return true;
    }

    /** Invoked on onOpen() of the context menu, determines if the option is available for this row or not (greying out the option). */
    default boolean isEnabled(final DataWithTracking<DTO, TrackingBase> dwt, final Permissionset perms) {
        return isEnabled(dwt);
    }

    /** Invoked when the user selects the listbox entry. The method should return true if
     * the list item needs a refresh after the operation (modifying DB operation).
     * That will also cause the "selected" event to be triggered again.
     * @param lb
     * @param li
     * @param dwt
     */
    default void selected(final Grid28 lb, final DataWithTracking<DTO, TrackingBase> dwt, final Permissionset perms) {
        selected(lb, dwt);
    }

    default void selected(final Grid28 lb, final DataWithTracking<DTO, TrackingBase> dwt) {
        throw new UnsupportedOperationException();
    }
}
