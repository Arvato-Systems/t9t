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
package com.arvatosystems.t9t.zkui.inputelements;

import org.zkoss.zk.ui.WrongValueException;

import de.jpaw.fixedpoint.types.MicroUnits;

public class MicroUnitsBox extends Fixedpointbox<MicroUnits, MicroUnitsBox> {
    private static final long serialVersionUID = 437573760456243476L;

    public MicroUnitsBox() {
        super(s -> MicroUnits.valueOf(s));
    }

    public MicroUnitsBox(MicroUnits value) throws WrongValueException {
        this();
        setValue(value);
    }
}
