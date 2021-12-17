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
package com.arvatosystems.t9t.zkui.converters;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 * This is ZK Data binding converter that is ONLY needed by custom decimal field that required on a custom page.
 * Regular LocalDateTime fields that render by field28, cell28 will never use it.
 */
public class LocalDateTimeConverter implements Converter<Date, LocalDateTime, Component> {

    /**
     * Coerces a value to another value to load to a component.
     * @param val the bean value
     * @param comp the component to be loaded the value
     * @param ctx the bind context
     * @return the value to load to a component
     */
    @Override
    public Date coerceToUi(LocalDateTime val, Component comp, BindContext ctx) {
        if (val == null) {
            return null; // do nothing
        }
        return  Date.from(val.atZone(ZoneOffset.UTC).toInstant());
    }

    /**
     * Coerces a value to bean value to save to a bean.
     * @param val the value of component attribute.
     * @param comp the component provides the value
     * @param ctx the bind context
     * @return the value to save to a bean
     */
    @Override
    public final LocalDateTime coerceToBean(Date val, Component comp, BindContext ctx) {
        if (val == null) {
            return null; // do nothing
        }
        return LocalDateTime.ofInstant(val.toInstant(), ZoneOffset.UTC);
    }
}
