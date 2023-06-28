/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.converters;

import de.jpaw.fixedpoint.types.MilliUnits;
import java.math.BigDecimal;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Decimalbox;

/**
 * This is ZK Data binding converter that is ONLY needed by custom decimal field that required on a custom page.
 * Regular MilliUnits fields that render by field28, cell28 will never use it.
 */
public class MilliUnitsConverter implements Converter<BigDecimal, MilliUnits, Decimalbox> {

    @Override
    public BigDecimal coerceToUi(MilliUnits milliUnits, Decimalbox component, BindContext ctx) {
        return milliUnits == null ? null : milliUnits.toBigDecimal();
    }

    @Override
    public MilliUnits coerceToBean(BigDecimal value, Decimalbox component, BindContext ctx) {
        return value == null ? null :  MilliUnits.valueOf(value);
    }
}
