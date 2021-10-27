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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.util.concurrent.atomic.AtomicReference;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Singleton
public class StandardT9tNamespaceWriter implements IStandardNamespaceWriter {
    private static final Logger LOGGER = LoggerFactory.getLogger(StandardT9tNamespaceWriter.class);
    private final Object lock = new Object();
    private final AtomicReference<JAXBContext> context = new AtomicReference<>();

    public static final String T9T_JAXB_PATH = "com.arvatosystems.t9t.xml:com.arvatosystems.t9t.xml.auth";
    public static final String T9T_NAMESPACES = "xmlns:bon=\"http://www.jpaw.de/schema/bonaparte.xsd\""
            + " xmlns:t9t_xml=\"http://arvatosystems.com/schema/t9t_xml.xsd\""
            + " xmlns:t9t_xml_auth=\"http://arvatosystems.com/schema/t9t_xml_auth.xsd\"";

    @Override
    public void writeApplicationNamespaces(final XMLStreamWriter writer) throws XMLStreamException {
        writer.writeNamespace("bon",          "http://www.jpaw.de/schema/bonaparte.xsd");
        writer.writeNamespace("t9t_xml",      "http://arvatosystems.com/schema/t9t_xml.xsd");
        writer.writeNamespace("t9t_xml_auth", "http://arvatosystems.com/schema/t9t_xml_auth.xsd");
    }

    @Override
    public String getStandardJAXBPath() {
        return T9T_JAXB_PATH;
    }

    @Override
    public JAXBContext getStandardJAXBContext() {
        JAXBContext currentContext = context.get();
        if (currentContext != null)
            return currentContext;
        // not available, create a new one
        synchronized (lock) {
            // get the atomic again to avoid race conditions
            currentContext = context.get();
            if (currentContext != null)
                return currentContext;
            final String path = getStandardJAXBPath();
            LOGGER.info("Initializing JAXB Context for path {}", path);
            try {
                currentContext = JAXBContext.newInstance(path);
            } catch (final JAXBException e) {
                LOGGER.error("Severe error initializing the JAXBContext: {} ", e);
                return null;
            }
            context.set(currentContext);
        }
        return currentContext;
    }
}
