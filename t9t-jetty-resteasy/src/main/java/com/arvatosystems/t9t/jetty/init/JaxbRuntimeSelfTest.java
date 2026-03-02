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
package com.arvatosystems.t9t.jetty.init;

import jakarta.xml.bind.JAXBContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quick runtime check for JAXB implementation visibility.
 *
 * This is intentionally tiny and only logs what it finds.
 */
public final class JaxbRuntimeSelfTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JaxbRuntimeSelfTest.class);

    private JaxbRuntimeSelfTest() {
    }

    public static void run() {
        try {
            final ClassLoader tccl = Thread.currentThread().getContextClassLoader();
            final ClassLoader jettyCl = JettyServer.class.getClassLoader();
            LOGGER.info("JAXB self-test: TCCL is {}", tccl);
            LOGGER.info("JAXB self-test: JettyServer.class.getClassLoader() is {}", jettyCl);

            // show potential factory overrides
            LOGGER.info("JAXB self-test: sysprop jakarta.xml.bind.JAXBContextFactory = {}", System.getProperty("jakarta.xml.bind.JAXBContextFactory"));
            LOGGER.info("JAXB self-test: sysprop jakarta.xml.bind.JAXBContext = {}", System.getProperty("jakarta.xml.bind.JAXBContext"));
            LOGGER.info("JAXB self-test: sysprop javax.xml.bind.context.factory = {}", System.getProperty("javax.xml.bind.context.factory"));

            // direct lookup should succeed if jaxb-runtime is visible
            final Class<?> ctxFactory = Class.forName("org.glassfish.jaxb.runtime.v2.ContextFactory", false, tccl);
            LOGGER.info("JAXB self-test: found ContextFactory via TCCL: {} from {}", ctxFactory.getName(),
                    ctxFactory.getProtectionDomain() != null ? ctxFactory.getProtectionDomain().getCodeSource() : null);

            // also try to create any JAXBContext to trigger ContextFinder / ServiceLoader resolution
            final JAXBContext jc = JAXBContext.newInstance(Object.class);
            LOGGER.info("JAXB self-test: JAXBContext impl is {} from {}", jc.getClass().getName(),
                    jc.getClass().getProtectionDomain() != null ? jc.getClass().getProtectionDomain().getCodeSource() : null);

        } catch (final Throwable t) {
            LOGGER.error("JAXB self-test failed: {}: {}", t.getClass().getName(), t.getMessage(), t);
        }
    }
}
