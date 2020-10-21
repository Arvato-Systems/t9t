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
package com.arvatosystems.t9t.events;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;

/** Event structure sent by Grid28 when a row has been selected.
 * Can be processed by child grids to update detail views.
 */
public class RowSelectedEventData {
    String contextMenuId;             // if a context menu has been clicked, the ID of the menu entry
    Long key;                         // the key to the row, if the data has a surrogate key
    DataWithTracking<?,?> dwt;
}
