/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.camunda.startup.impl;

import org.camunda.bpm.engine.impl.variable.serializer.AbstractObjectValueSerializer;

import de.jpaw.bonaparte.api.codecs.IMessageCoderFactory;
import de.jpaw.bonaparte.api.codecs.impl.MessageCoderFactory;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MimeTypes;
import de.jpaw.bonaparte.core.StaticMeta;

public class BonaparteTypeValueSerializer extends AbstractObjectValueSerializer {

    private final IMessageCoderFactory<BonaPortable, BonaPortable, byte[]> coderFactory = new MessageCoderFactory<>(BonaPortable.class, BonaPortable.class);

    public static final String DATA_FORMAT = "application/bonaparte";

    public BonaparteTypeValueSerializer() {
        super(DATA_FORMAT);
    }

    @Override
    public String getName() {
        return "bonaparte";
    }

    @Override
    protected String getTypeNameForDeserialized(Object deserializedObject) {
        return ((BonaPortable) deserializedObject).ret$PQON();
    }

    @Override
    protected byte[] serializeToByteArray(Object deserializedObject) throws Exception {
        final BonaPortable bp = (BonaPortable) deserializedObject;

        return coderFactory.getEncoderInstance(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE)
                           .encode(bp, StaticMeta.OUTER_BONAPORTABLE);
    }

    @Override
    protected Object deserializeFromByteArray(byte[] object, String objectTypeName) throws Exception {
        return coderFactory.getDecoderInstance(MimeTypes.MIME_TYPE_COMPACT_BONAPARTE)
                           .decode(object, StaticMeta.OUTER_BONAPORTABLE);
    }

    @Override
    protected boolean isSerializationTextBased() {
        return false;
    }

    @Override
    protected boolean canSerializeValue(Object value) {
        return value instanceof BonaPortable;
    }

}
