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
package com.arvatosystems.t9t.in.be.impl

import com.arvatosystems.t9t.in.services.IInputSession
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.T9tIOException
import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter
import com.arvatosystems.t9t.server.services.IStatefulServiceSession
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortableClass
import de.jpaw.dp.Inject
import de.jpaw.util.ExceptionUtil
import java.util.Map
import java.util.concurrent.ConcurrentHashMap
import javax.xml.bind.JAXBContext
import javax.xml.bind.JAXBException
import javax.xml.bind.Unmarshaller
import javax.xml.stream.XMLStreamReader
import com.arvatosystems.t9t.base.T9tException

@AddLogger
abstract class AbstractXmlFormatConverter extends AbstractInputFormatConverter {

    // cache for the contexts, to avoid iterative creation of them
    @Inject
    protected IStandardNamespaceWriter namespaceWriter
    protected static ConcurrentHashMap<String, JAXBContext> jaxbContexts = new ConcurrentHashMap<String, JAXBContext>(16);
    protected JAXBContext context = null;
    protected Unmarshaller m = null;
    protected XMLStreamReader writer = null;

    protected final boolean formatted = true;
    protected final boolean writeTenantId = true;
    protected String defaultNamespace;

    override open(IInputSession inputSession, DataSinkDTO sinkCfg, IStatefulServiceSession session, Map<String, Object> params, BonaPortableClass<?> baseBClass) {
        super.open(inputSession, sinkCfg, session, params, baseBClass)
        val path             = sinkCfg.jaxbContextPath
        defaultNamespace     = sinkCfg.xmlDefaultNamespace

        context = if (path === null) {
            // use the default path
            namespaceWriter.standardJAXBContext
        } else {
            jaxbContexts.get(path);
        }
        try {
            if (context === null) {
                context = JAXBContext.newInstance(path);
                jaxbContexts.putIfAbsent(path, context);
            }
            m = context.createUnmarshaller();
        } catch (JAXBException e) {
            LOGGER.error(e.getMessage(), e);
            throw new T9tException(T9tIOException.XML_SETUP_ERROR, ExceptionUtil.causeChain(e));
        }
    }
}
