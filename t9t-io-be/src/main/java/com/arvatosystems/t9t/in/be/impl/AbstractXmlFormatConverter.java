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
package com.arvatosystems.t9t.in.be.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.xml.namespaces.IStandardNamespaceWriter;

import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractXmlFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractXmlFormatConverter.class);
    protected static final ConcurrentHashMap<String, JAXBContext> JAXB_CONTEXTS = new ConcurrentHashMap<String, JAXBContext>(16);

    // cache for the contexts, to avoid iterative creation of them
    protected final IStandardNamespaceWriter namespaceWriter = Jdp.getRequired(IStandardNamespaceWriter.class);
    protected JAXBContext context = null;
    protected Unmarshaller m = null;
    protected XMLStreamReader writer = null;
    protected final boolean formatted = true;
    protected final boolean writeTenantId = true;
    protected String defaultNamespace;

    @Override
    public void open(final IInputSession inputSession, final Map<String, Object> params, final BonaPortableClass<?> baseBClass) {
        super.open(inputSession, params, baseBClass);
        final String path = inputSession.getDataSinkDTO().getJaxbContextPath();
        defaultNamespace = inputSession.getDataSinkDTO().getXmlDefaultNamespace();

        if (path == null) {
            // use the default path
            context = namespaceWriter.getStandardJAXBContext();
        } else {
            context = JAXB_CONTEXTS.get(path);
        }
        try {
            if (context == null) {
                context = JAXBContext.newInstance(path);
                JAXB_CONTEXTS.putIfAbsent(path, context);
            }
            m = context.createUnmarshaller();
        } catch (final JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_SETUP_ERROR, ExceptionUtil.causeChain(e));
        }
    }
}
