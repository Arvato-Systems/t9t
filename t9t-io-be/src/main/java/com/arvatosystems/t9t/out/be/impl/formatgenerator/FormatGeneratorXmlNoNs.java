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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import com.arvatosystems.t9t.out.be.impl.formatgenerator.AbstractFormatGeneratorXml;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/** Format generator for XML without namespaces. */
@Dependent
@Named("XMLnoNS")
public class FormatGeneratorXmlNoNs extends AbstractFormatGeneratorXml {

    @Override
    protected void createWriter() throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newFactory();
        factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.FALSE);

        writer = factory.createXMLStreamWriter(outputResource.getOutputStream());
        writer.setDefaultNamespace("");
    }

    @Override
    protected void writeNamespaces() throws XMLStreamException {
        // do not write namespace information
    }
}
