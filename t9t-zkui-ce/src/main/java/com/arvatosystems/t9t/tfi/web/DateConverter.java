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
package com.arvatosystems.t9t.tfi.web;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

/**
 *
 * @author INCI02
 *
 */
public class DateConverter implements Converter<Object, Object, Component> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DateConverter.class);

    /**
     * Coerces a value to another value to load to a component.
     * @param val the bean value
     * @param comp the component to be loaded the value
     * @param ctx the bind context
     * @return the value to load to a component
     */
    @Override
    public Object coerceToUi(Object val, Component comp, BindContext ctx) {
        if (val == null) {
            return null; // do nothing
        }

        if (val instanceof LocalDateTime) {
            val = ((LocalDateTime)val).toDate();
        } else if (val instanceof LocalDate) {
            val = ((LocalDate)val).toDate();
        } else if (val instanceof Instant) {
            val = ((Instant) val).toDate();
        }
        // if "format" (@converter(... format='com.date.format'...)) is not given, pass the original value back -> nothing to do
        String format = (String) ctx.getConverterArg("format");
        if (format == null) {
            return val;
        }

        // check if format-rule is configured
        final String formatPattern = ZulUtils.i18nLabel(format);
        if (formatPattern == null) {
            throw new NullPointerException("format pattern attribute not found in zk-lable.properties");
        }

        // return object must be specified
        final String returnObject = (String) ctx.getConverterArg("returnObject");
        if (returnObject == null) {
            throw new NullPointerException("incomingObject attribute not found");
        }

        // if input is a Date and returnObject is String
        // convert Date to String by formatPattern
        if (val instanceof Date) {
            if (returnObject.equalsIgnoreCase("String")) {
                String dateString;
                dateString = new SimpleDateFormat(formatPattern).format((val));
                LOGGER.trace("#coerceToUi: Component: {}/{} Date:raw:{} conv:{}", new Object[]{comp.getWidgetClass(), comp.getId(), val, dateString});
                return dateString;
            } else {
                throw new UnsupportedOperationException("Retrun object "+ returnObject +" is not supported (Component: "+comp.getWidgetClass()+"/"+comp.getId()+"/"+val+")");
            }
        } else {
            throw new UnsupportedOperationException("Instance "+ val.getClass().getName() +" is not supported (Component: "+comp.getWidgetClass()+"/"+comp.getId()+"/"+val+")");
        }
    }

    /**
     * Coerces a value to bean value to save to a bean.
     * @param val the value of component attribute.
     * @param comp the component provides the value
     * @param ctx the bind context
     * @return the value to save to a bean
     */
    @Override
    public final Object coerceToBean(Object val, Component comp, BindContext ctx) {
        if (val==null)
        {
            return null; // do nothing
        }

        // return object must be specified
        final String returnObject = (String) ctx.getConverterArg("returnObject");
        if (returnObject == null) {
            throw new NullPointerException("incomingObject attribute not found");
        }

        // if bind is a Date and returnObject is LocalDateTime
        // convert Date to LocalDateTime
        if (val instanceof Date) {
            if (returnObject.equalsIgnoreCase("LocalDateTime")) {
                Date date= (Date)val;
                final LocalDateTime localDateTime = new LocalDateTime(date.getTime());
                LOGGER.trace("#coerceToBean: Component: {}/{} Date:raw:{} conv:{}", new Object[]{comp.getWidgetClass(), comp.getId(), val, localDateTime});
                return localDateTime;
            } else if (returnObject.equalsIgnoreCase("LocalDate")) {
                Date date= (Date)val;
                final LocalDate localDate = new LocalDate(date.getTime());
                LOGGER.trace("#coerceToBean: Component: {}/{} Date:raw:{} conv:{}", new Object[]{comp.getWidgetClass(), comp.getId(), val, localDate});
                return localDate;
            }  else if (returnObject.equalsIgnoreCase("LocalTime")) {
                Date date=(Date)val;
                final LocalTime localTime=new LocalTime(date.getTime());
                LOGGER.trace("#coerceToBean: Component: {}/{} Date:raw:{} conv:{}", new Object[]{comp.getWidgetClass(), comp.getId(), val, localTime});
                return localTime;
            }
            else if (returnObject.equalsIgnoreCase("Instant")) {
                Date date = (Date) val;
                final Instant localDate = new Instant(date.getTime());
                LOGGER.trace("#coerceToBean: Component: {}/{} Date:raw:{} conv:{}", new Object[] { comp.getWidgetClass(), comp.getId(), val, localDate });
                return localDate;
            } else {
                throw new UnsupportedOperationException("Retrun object "+ returnObject +" is not supported");
            }
        } else {
            throw new UnsupportedOperationException("Instance "+ val.getClass().getName() +" is not supported");
        }

    }
}
