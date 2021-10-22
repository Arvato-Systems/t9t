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
package com.arvatosystems.t9t.in.be.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.meta.DataCategory;
import de.jpaw.bonaparte.pojos.meta.IndexType;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;
import de.jpaw.bonaparte.pojos.meta.Visibility;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.json.JsonParser;

/**
 * JSON format converter which allows import of arbitrary data in JSON format, using the Bonaparte encoder.
 * This implementation is intended for smaller input, because it converts the whole file in memory (JAXB like),
 * instead of the preferred Stax-like approach (one object at a time).
 * It is up to the configured IInputDataTransformer to create a valid request. (e.g. an file upload request.)
 *
 * The implementation expects enums encoded as ordinals and tokens by default.
 * enums are parsed using instance names if SinkCfgDTo.genericParameter2 = "1"
 */
@Dependent
@Named("JSON")
public class JsonFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonFormatConverter.class);
    protected ObjectReference metaDataForOuter;
    protected boolean useOrdinals = true;
    protected boolean useTokens = true;

    @Override
    public void open(IInputSession inputSession, Map<String, Object> params, BonaPortableClass<?> baseBClass) {
        metaDataForOuter = new ObjectReference(Visibility.PRIVATE, false, "",
                Multiplicity.SCALAR, IndexType.NONE, 0, 0, DataCategory.OBJECT, "json", "Map", false, false, null, true, "Map",
                baseBClass.getMetaData(), null, null);
        if ("1".equals(inputSession.getDataSinkDTO().getGenericParameter2())) {
            useOrdinals = false;
            useTokens = false;
        }
        super.open(inputSession, params, baseBClass);
    }

    protected void processBonaPortable(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            inputSession.process((BonaPortable)null);
        } else {
            final BonaPortable obj = MapParser.allocObject(map, metaDataForOuter);
            obj.deserialize(new MapParser(map, false, useOrdinals, useTokens));
            inputSession.process(obj);
        }
    }

    @Override
    public void process(InputStream is) {
        final Charset encoding = inputSession.getDataSinkDTO().getOutputEncoding() == null ? Charsets.UTF_8 : Charset.forName(inputSession.getDataSinkDTO().getOutputEncoding());
        final String inputData;
        try {
            inputData = CharStreams.toString(new InputStreamReader(is, encoding));
        } catch (IOException e) {
            LOGGER.error("Input read error", e);
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        }
        JsonParser p = new JsonParser(inputData, true);
        p.parseObjectOrListOfObjects((m) -> processBonaPortable(m));
    }
}
