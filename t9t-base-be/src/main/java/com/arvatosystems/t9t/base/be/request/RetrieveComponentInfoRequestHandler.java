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
package com.arvatosystems.t9t.base.be.request;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.request.ComponentInfoDTO;
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoRequest;
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;

public class RetrieveComponentInfoRequestHandler extends AbstractRequestHandler<RetrieveComponentInfoRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrieveComponentInfoRequestHandler.class);

    private final List<ComponentInfoDTO> componentInfos;

    public RetrieveComponentInfoRequestHandler() {
        final Reflections reflections = new Reflections("META-INF/maven", new ResourcesScanner());

        LOGGER.info("scanning classpath to detect component infos");
        componentInfos = reflections.getResources(Pattern.compile("pom\\.properties"))
                                    .stream()
                                    .filter((r) -> (r.startsWith("META-INF/maven/")))
                                    .map((r) -> (readComponentInfo(r)))
                                    .filter((c) -> (c != null))
                                    .collect(toList());
    }

    @Override
    public RetrieveComponentInfoResponse execute(RetrieveComponentInfoRequest request) {
        return new RetrieveComponentInfoResponse(0, componentInfos);
    }

    private ComponentInfoDTO readComponentInfo(String pomPropertiesRes) {
        try (InputStream in = RetrieveComponentInfoRequestHandler.class.getResourceAsStream("/" + pomPropertiesRes)) {
            if (in != null) {
                final Properties p = new Properties();
                p.load(in);

                final String versionString = p.getProperty("version");
                final String groupId = p.getProperty("groupId");
                final String artifactId = p.getProperty("artifactId");

                final ComponentInfoDTO result = new ComponentInfoDTO(groupId, artifactId, versionString);
                return result;
            } else {
                throw new IOException("Resource not found: " + pomPropertiesRes);
            }
        } catch (IOException e) {
            LOGGER.warn("Failed to read component info from pom.properties: " + pomPropertiesRes, e);
            return null;
        }
    }

}
