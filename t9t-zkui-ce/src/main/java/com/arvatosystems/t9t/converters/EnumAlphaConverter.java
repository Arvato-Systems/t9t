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
package com.arvatosystems.t9t.converters;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zul.Combobox;

public class EnumAlphaConverter implements Converter<String, Enum<?>, Combobox> {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Enum coerceToBean(String value, Combobox arg1, BindContext ctx) {
        Class enumClass = (Class) ctx.getAttribute("enumClass");
        return Enum.valueOf(enumClass, value);
    }

    @Override
    public String coerceToUi(Enum<?> myEnum, Combobox arg1, BindContext ctx) {
        return myEnum.name();
    }
}
