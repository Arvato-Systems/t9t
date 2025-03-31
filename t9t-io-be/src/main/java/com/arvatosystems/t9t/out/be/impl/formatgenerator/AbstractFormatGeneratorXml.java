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
package com.arvatosystems.t9t.out.be.impl.formatgenerator;

import static java.util.Collections.emptyMap;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.xml.namespaces.IStandardNamespaceWriter;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.PropertyException;

public class AbstractFormatGeneratorXml extends AbstractFormatGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFormatGeneratorXml.class);
    protected final IStandardNamespaceWriter namespaceWriter = Jdp.getRequired(IStandardNamespaceWriter.class);
    protected static ConcurrentHashMap<String, JAXBContext> jaxbContexts = new ConcurrentHashMap<>(16);

    protected JAXBContext context = null;
    protected Marshaller m = null;
    protected XMLStreamWriter writer = null;

    protected String xmlDefaultNamespace;
    protected String xmlNamespacePrefix;
    protected String xmlRecordName;
    protected String xmlRootElementName;
    protected Map<String, String> xmlNamespaceMapping = emptyMap();
    protected Boolean writeTenantId;

    protected QName getQname(final String id) {
        if (xmlNamespacePrefix == null)
            return new QName(xmlDefaultNamespace, id);
        else
            return new QName(xmlDefaultNamespace, id, xmlNamespacePrefix);
    }

    // writes the default namespace
    protected void setDefaultNamespace() throws XMLStreamException {
        if (xmlDefaultNamespace != null) {
            writer.setDefaultNamespace(xmlDefaultNamespace);
        }
    }

    // creates the xml writer
    protected void createWriter() throws XMLStreamException {
        final XMLOutputFactory factory = XMLOutputFactory.newFactory();
        factory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);

        writer = factory.createXMLStreamWriter(outputResource.getOutputStream(), encoding.name());
        setDefaultNamespace();
    }

    protected void doWriteTenantId() {
        storeCustomElement("tenantId", String.class, tenantId);
        try {
            nl();
        } catch (XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public <T> void storeCustomElement(String name, Class<T> valueClass, Object value) {
        try {
            m.marshal(new JAXBElement<>(getQname(name), valueClass, (T)value), writer);
        } catch (final JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    protected void writeCustomElement(final String id) throws XMLStreamException {
        final Map<String, Object> map = outputSessionParameters.getAdditionalParameters();
        if (map != null) {
            final Object value = map.get(id);
            if (value != null) {
                storeCustomElement(id, value.getClass(), value);
                nl();
            }
        }
    }

    protected void setNamespaceContext() throws XMLStreamException {
        // do nothing
    }

    protected void writeNamespaces() throws XMLStreamException {
        writer.setPrefix("bon", "http://www.jpaw.de/schema/bonaparte.xsd");

        if (xmlNamespaceMapping.isEmpty()) {
            LOGGER.debug("Define namespace prefix using namespace writer {}", writer);
            namespaceWriter.writeApplicationNamespaces(writer);
        } else {
            for (final Entry<String, String> nsMapping : xmlNamespaceMapping.entrySet()) {
                LOGGER.debug("Define namespace prefix from data sink config: {}={}", nsMapping.getKey(), nsMapping.getValue());
                writer.setPrefix(nsMapping.getKey(), nsMapping.getValue());
            }
        }
    }

    protected void writeCustomElements(final String tagList) throws XMLStreamException, JAXBException {
        if (tagList == null || tagList.isEmpty() || outputSessionParameters.getAdditionalParameters() == null) {
            return; // nothing to do
        }
        // split the tagList into separate tags
        final String[] tags = tagList.split(",");
        for (final String tag: tags) {
            if (tag != null && !tag.isEmpty()) {
                writeCustomElement(tag);
            }
        }
    }

    protected void nl() throws XMLStreamException {
        writer.writeCharacters("\n");
    }

    @Override
    protected void openHook() throws IOException {
        super.openHook();
        // get the context
        final String path   = sinkCfg.getJaxbContextPath();
        xmlDefaultNamespace = sinkCfg.getXmlDefaultNamespace();         // http:something
        xmlNamespacePrefix  = sinkCfg.getXmlNamespacePrefix();
        xmlNamespaceMapping = parseNamespaceMapping(sinkCfg.getXmlNamespaceMappings());
        xmlRecordName       = sinkCfg.getXmlRecordName();               // records, instance field name or List element
        xmlRootElementName  = sinkCfg.getXmlRootElementName();          // simple name of xml root element class
        writeTenantId       = sinkCfg.getWriteTenantId();               // write the tenantId field?

        // repair default namespace
        xmlDefaultNamespace = xmlDefaultNamespace == null ? "" : xmlDefaultNamespace.trim();

        context = path == null ? namespaceWriter.getStandardJAXBContext() : jaxbContexts.get(path);
        try {
            if (context == null) {
                context = JAXBContext.newInstance(path);
                jaxbContexts.putIfAbsent(path, context);
            }
            m = context.createMarshaller();
        } catch (final JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_SETUP_ERROR, ExceptionUtil.causeChain(e));
        }
        try {
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);      // does not work with the XMLStreamWriter without a separate indenting writer
            m.setProperty(Marshaller.JAXB_ENCODING, encoding.name());
            m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        } catch (final PropertyException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_SET_PROPERTY_ERROR, ExceptionUtil.causeChain(e));
        }
        try {
            createWriter();

            if (sinkCfg.getOutputEncoding() == null) {
                writer.writeStartDocument();
            } else {
                writer.writeStartDocument(encoding.name(), "1.0");
            }
            nl();
            setNamespaceContext();
            writer.writeStartElement(xmlDefaultNamespace, sinkCfg.getXmlRootElementName());
            writeNamespaces();
            nl();
            if (Boolean.TRUE.equals(writeTenantId)) {
                doWriteTenantId();
            }
            writeCustomElements(sinkCfg.getXmlHeaderElements());
        } catch (XMLStreamException | FactoryConfigurationError | JAXBException e1) {
            LOGGER.error(e1.getMessage(), e1);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR, e1.getClass().getSimpleName() + ": " + e1.getMessage());
        }
    }

    @Override
    public void generateData(final int recordNo, final int mappedRecordNo, final long recordId, final String partitionKey, final String recordKey,
      final BonaPortable record) throws IOException {
        try {
            final JAXBElement<BonaPortable> element = new JAXBElement<>(getQname(xmlRecordName), (Class<BonaPortable>) record.getClass(), record);
            m.marshal(element, writer);
            nl();
        } catch (JAXBException | XMLStreamException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public void close() throws IOException {
        try {
            writeCustomElements(sinkCfg.getXmlFooterElements());
            writer.writeEndDocument();
            writer.close();
        } catch (XMLStreamException | JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_MARSHALLING_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    private static Map<String, String> parseNamespaceMapping(final String mappingString) {
        final Map<String, String> result = new HashMap<>();

        if (mappingString != null) {
            int counter = 0;
            for (final String namespaceEntry : mappingString.split(";\n\r")) {
                if (T9tUtil.isBlank(namespaceEntry)) {
                    continue;
                }

                final int firstDelimiter = namespaceEntry.indexOf('=');
                String key;
                String value;

                if (firstDelimiter > 0) {
                    key = T9tUtil.trimToNull(namespaceEntry.substring(0, firstDelimiter));
                    value = T9tUtil.trimToNull(namespaceEntry.substring(firstDelimiter + 1));
                } else if (firstDelimiter == 0) {
                    key = "ns" + counter++;
                    value = T9tUtil.trimToNull(namespaceEntry.substring(firstDelimiter + 1));
                } else {
                    key = "ns" + counter++;
                    value = T9tUtil.trimToNull(namespaceEntry);
                }

                result.put(key, value);
            }
        }

        return result;
    }
}
