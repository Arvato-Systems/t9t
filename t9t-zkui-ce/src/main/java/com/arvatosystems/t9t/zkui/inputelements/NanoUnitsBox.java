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
package com.arvatosystems.t9t.zkui.inputelements;

import org.zkoss.zk.ui.WrongValueException;

import de.jpaw.fixedpoint.types.NanoUnits;

public class NanoUnitsBox extends Fixedpointbox<NanoUnits, NanoUnitsBox> {
    private static final long serialVersionUID = 437573760456243479L;

    public NanoUnitsBox() {
        super(s -> NanoUnits.valueOf(s));
    }

    public NanoUnitsBox(NanoUnits value) throws WrongValueException {
        this();
        setValue(value);
    }
}
