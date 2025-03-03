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
