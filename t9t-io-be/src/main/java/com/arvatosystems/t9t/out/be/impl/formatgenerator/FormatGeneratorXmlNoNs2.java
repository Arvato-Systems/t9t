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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import java.util.Iterator;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/** Format generator for XML without namespaces. */
@Dependent
@Named("XMLnoNS2")
public class FormatGeneratorXmlNoNs2 extends AbstractFormatGeneratorXml {
    private static final Logger LOGGER = LoggerFactory.getLogger(FormatGeneratorXmlNoNs2.class);

    public FormatGeneratorXmlNoNs2() {
        LOGGER.debug("Creating instance of special XML writer (writing without namespaces)");
    }

    @Override
    protected void setDefaultNamespace() throws XMLStreamException {
    }

    @Override
    protected QName getQname(final String id) {
        LOGGER.trace("Getting QNAME for {}", id);
        if (xmlNamespacePrefix == null || xmlNamespacePrefix.trim().length() == 0)
            return new QName(xmlDefaultNamespace, id);
        else
            return new QName(xmlDefaultNamespace, id, xmlNamespacePrefix);
    }

    @Override
    protected void writeNamespaces() throws XMLStreamException {
        // do not write namespace information
    }

    @Override
    protected void setNamespaceContext() throws XMLStreamException {
        LOGGER.debug("Setting namespace context");
        // do not write namespace information
        // https://stackoverflow.com/questions/2816176/how-to-marshal-without-a-namespace
        writer.setNamespaceContext(new NamespaceContext() {
            @Override
            public Iterator getPrefixes(final String namespaceURI) {
                return null;
            }

            @Override
            public String getPrefix(final String namespaceURI) {
                return "";
            }

            @Override
            public String getNamespaceURI(final String prefix) {
                return null;
            }
        });
    }
}
