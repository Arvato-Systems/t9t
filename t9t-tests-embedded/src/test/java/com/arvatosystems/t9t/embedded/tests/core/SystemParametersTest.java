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
package com.arvatosystems.t9t.embedded.tests.core;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsRequest;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SystemParametersTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(SystemParametersTest.class);
    private static ITestConnection dlg;

    @BeforeAll
    public static void createConnection() {
        dlg = new InMemoryConnection();
    }

    @Test
    public void getSystemParams() throws JsonProcessingException {
        final QuerySystemParamsResponse resp = dlg.typeIO(new QuerySystemParamsRequest(), QuerySystemParamsResponse.class);
        final ObjectMapper om = JacksonTools.createObjectMapper();
        LOGGER.info("Result is {}", JacksonTools.prettyPrint(om, resp));
    }
}
