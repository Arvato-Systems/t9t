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
package com.arvatosystems.t9t.tfi.component;

import java.io.IOException;
import java.util.Map;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.Converter;
import org.zkoss.zk.ui.Component;

import de.jpaw.json.BaseJsonComposer;
import de.jpaw.json.JsonException;
import de.jpaw.json.JsonParser;

/**
 * Customer converter implementation to convert Json String to Map of String and Object pairs and vice versa.
 *
 * @author michaellow
 */
public class JsonConverter implements Converter<String, Map<String, Object>, Component> {

    private static final String EMPTY_BRACKET = "{}";

    @Override
    public String coerceToUi(Map<String, Object> jsonMap, Component component, BindContext bindContext) {
        StringBuilder stringBuilder = new StringBuilder();
        if (jsonMap != null) {
            BaseJsonComposer baseJsonComposer = new BaseJsonComposer(stringBuilder);
            try {
                baseJsonComposer.outputJsonObject(jsonMap);
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to convert map data to json string format", e);
            }
        }
        return stringBuilder.toString().equals(EMPTY_BRACKET) ? "" : stringBuilder.toString();
    }

    @Override
    public Map<String, Object> coerceToBean(String jsonString, Component component, BindContext bindContext) {
        if (jsonString != null && !jsonString.equals("")) {
            JsonParser jsonParser = new JsonParser(jsonString, true);
            try {
                return jsonParser.parseObject();
            } catch (JsonException e) {
                throw new IllegalArgumentException("Failed to parse the given json string", e);
            }
        } else {
            return null;
        }
    }
}
