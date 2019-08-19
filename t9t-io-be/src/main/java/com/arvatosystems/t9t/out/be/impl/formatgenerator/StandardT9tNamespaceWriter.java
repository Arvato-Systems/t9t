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
    private final AtomicReference<JAXBContext> context = new AtomicReference<JAXBContext>();

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
            String path = getStandardJAXBPath();
            LOGGER.info("Initializing JAXB Context for path {}", path);
            try {
                currentContext = JAXBContext.newInstance(path);
            } catch (JAXBException e) {
                LOGGER.error("Severe error initializing the JAXBContext: {} ", e);
                return null;
            }
            context.set(currentContext);
        }
        return currentContext;
    }

    @Override
    public void writeApplicationNamespaces(XMLStreamWriter writer) throws XMLStreamException {
        // no additional namespaces used in the framework itself
    }

    @Override
    public String getStandardJAXBPath() {
        return "";
    }
}
