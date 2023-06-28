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

import de.jpaw.fixedpoint.types.NanoUnits;
import java.math.BigDecimal;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Decimalbox;

/**
 * This is ZK Data binding converter that is ONLY needed by custom decimal field that required on a custom page.
 * Regular NanoUnits fields that render by field28, cell28 will never use it.
 */
public class NanoUnitsConverter implements Converter<BigDecimal, NanoUnits, Decimalbox> {

    @Override
    public BigDecimal coerceToUi(NanoUnits microUnits, Decimalbox component, BindContext ctx) {
        return microUnits == null ? null : microUnits.toBigDecimal();
    }

    @Override
    public NanoUnits coerceToBean(BigDecimal value, Decimalbox component, BindContext ctx) {
        return value == null ? null :  NanoUnits.valueOf(value);
    }
}
