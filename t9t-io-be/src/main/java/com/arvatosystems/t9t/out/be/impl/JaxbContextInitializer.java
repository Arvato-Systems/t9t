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
package com.arvatosystems.t9t.out.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.out.be.IStandardNamespaceWriter;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(30057)
public class JaxbContextInitializer implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxbContextInitializer.class);

    @Override
    public void onStartup() {
        LOGGER.info("Creating initial JAXB context");
        // get the standard namespace and initialize a first JaxbContext
        // This is done because an initialization after SOAP has been used has caused issues.
        final IStandardNamespaceWriter namespaceWriter = Jdp.getRequired(IStandardNamespaceWriter.class);
        namespaceWriter.getStandardJAXBContext();
    }
}
