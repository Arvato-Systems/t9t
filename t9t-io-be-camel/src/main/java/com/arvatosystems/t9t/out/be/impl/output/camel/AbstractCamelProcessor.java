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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IFileUtil;

import de.jpaw.dp.Jdp;

/**
 * Base class for camel processor implementations. Holds common logic for resolving endpoint tags into URIs.
 *
 * @author greg
 *
 */
public abstract class AbstractCamelProcessor {

    private static final String CAMEL_ENDPOINT_CONFIG_PATH = "camel/camelEndpoints.properties";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCamelProcessor.class);

//  @Inject
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    private static Properties camelProps = new Properties();
    private static boolean camelPropsInitiated = false;

    private void initIfRequired() {
        if (!camelPropsInitiated) {
            loadCamelConfig(fileUtil.getAbsolutePath(CAMEL_ENDPOINT_CONFIG_PATH));
        }
    }

    private static synchronized void loadCamelConfig(String propsPath) {
        if (camelPropsInitiated) {
            return;
        }

        LOGGER.info("Initialize camel config.");
        try (InputStream inputStream = new FileInputStream(propsPath)) {
            camelProps.load(inputStream);
        } catch (IOException e) {
            LOGGER.error("Failed to read camel config from: {}", propsPath);
            LOGGER.error("Camel config load error.", e);
            throw new T9tException(T9tException.LOAD_CAMEL_CONFIG_ERROR);

            // no longer required due to try with resources
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    LOGGER.error("Failed to close camelEndpoints input stream.", e);
//                    throw new T9tException(T9tException.LOAD_CAMEL_CONFIG_ERROR);
//                }
//            }
        }

        camelPropsInitiated = true;
    }

    protected String getEndporintURI(String endpointTag) {
        initIfRequired();
        return camelProps.getProperty(endpointTag);
    }

    public static synchronized void flushConfig() {
        camelPropsInitiated = false;
        camelProps.clear();
    }

}
