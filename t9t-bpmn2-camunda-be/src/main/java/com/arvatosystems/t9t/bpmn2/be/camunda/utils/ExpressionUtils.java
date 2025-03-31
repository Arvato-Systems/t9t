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
package com.arvatosystems.t9t.bpmn2.be.camunda.utils;

import java.util.Objects;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.Expression;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.impl.MessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.util.ByteArray;

public abstract class ExpressionUtils {

    private static final IMessageCoderFactory<BonaPortable, BonaPortable, byte[]> coderFactory = new MessageCoderFactory<>(BonaPortable.class,
                                                                                                                           BonaPortable.class);

    public static String getValueAsString(Expression expression, DelegateExecution variableScope, String defaultValue) {
        if (expression == null) {
            return defaultValue;
        }

        return Objects.toString(expression.getValue(variableScope), defaultValue);
    }

    public static <T extends BonaPortable> T getValueAsBonaPortable(Expression expression, DelegateExecution variableScope, T defaultValue) {
        final String json = getValueAsString(expression, variableScope, null);

        if (json == null) {
            return defaultValue;
        }

        return unmarshalJson(json);
    }

    public static <T extends BonaPortable> T getValueAsBonaPortable(DelegateExecution variableScope, String variableName, T defaultValue) {
        final String json = Objects.toString(variableScope.getVariable(variableName), null);

        if (json == null) {
            return defaultValue;
        }

        return unmarshalJson(json);
    }

    public static <T extends BonaPortable> T unmarshalJson(String json) {
        return (T) coderFactory.getDecoderInstance(MimeTypes.MIME_TYPE_JSON)
                               .decode(json.getBytes(ByteArray.CHARSET_UTF8), StaticMeta.OUTER_BONAPORTABLE_FOR_JSON);
    }
}
