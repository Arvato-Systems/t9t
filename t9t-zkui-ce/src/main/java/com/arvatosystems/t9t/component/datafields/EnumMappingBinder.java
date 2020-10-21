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
package com.arvatosystems.t9t.component.datafields;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.zkoss.bind.Converter;
import org.zkoss.bind.DefaultBinder;

import com.arvatosystems.t9t.converters.EnumAlphaConverter;

public class EnumMappingBinder extends DefaultBinder {
    private static final long serialVersionUID = -2584640299999716814L;
    public static final Map<String, Converter> converterRegistry = new ConcurrentHashMap<String, Converter>();
    static {
        converterRegistry.put("enumAlpha", new EnumAlphaConverter());
    }
    @Override
    public Converter getConverter(String name) {
        Converter myConverter = converterRegistry.get(name);
        if (myConverter != null)
            return myConverter;
        return super.getConverter(name);
    }
}
