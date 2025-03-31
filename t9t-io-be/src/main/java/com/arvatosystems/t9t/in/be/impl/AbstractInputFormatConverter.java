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
package com.arvatosystems.t9t.in.be.impl;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.in.services.IInputFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortableClass;

/**
 * Superclass for IInputFormatConverter, i.e. implementations of the first step of conversion, which transform raw input
 * into BonaPortables. The class stores the parameters to open() in the instance and provides the encoding as Java object of type <code>Charset</code>.
 */
public abstract class AbstractInputFormatConverter implements IInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractInputFormatConverter.class);

    protected IInputSession inputSession;
    protected Map<String, Object> params;
    protected BonaPortableClass<?> baseBClass;
    protected DataSinkDTO importDataSinkDTO;
    protected Charset importCharset = StandardCharsets.UTF_8;

    @Override
    public void open(final IInputSession newInputSession, final Map<String, Object> newParams, final BonaPortableClass<?> newBaseBClass) {
        this.inputSession = newInputSession;
        this.params = newParams != null ? newParams : Collections.emptyMap();
        this.baseBClass = newBaseBClass;
        this.importDataSinkDTO = inputSession.getDataSinkDTO();
        final String charset = importDataSinkDTO.getOutputEncoding();
        if (charset != null) {
            try {
                this.importCharset = Charset.forName(charset);
            } catch (final Exception e) { //  UnsupportedEncodingException or IllegalCharsetNameException
                LOGGER.error("Cannot initialize encoding {} for data sink {}: Fallback to UTF-8", charset, importDataSinkDTO.getDataSinkId());
            }
        }
    }
}
