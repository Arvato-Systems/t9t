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
package com.arvatosystems.t9t.context;

import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.components.Grid28;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
@Any
public class MissingContextMenuHandler implements IGridContextMenu<BonaPortable> {

    @Override
    public void selected(Grid28 lb, DataWithTracking<BonaPortable, TrackingBase> dwt) {
        Messagebox.show("Not available", "Error", Messagebox.CANCEL, Messagebox.EXCLAMATION);
    }
}
