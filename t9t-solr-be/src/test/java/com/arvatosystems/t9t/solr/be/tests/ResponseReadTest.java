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
package com.arvatosystems.t9t.solr.be.tests;

import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.solr.be.impl.response.QueryResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

public class ResponseReadTest {

    @Test
    public void testReadSolrResponse() throws Exception {
        ObjectMapper objectMapper = JsonMapper.builder().build();
        String body = "{\n"
                + "  \"responseHeader\":{\n"
                + "    \"status\":0,\n"
                + "    \"QTime\":1,\n"
                + "    \"params\":{\n"
                + "      \"json\":\"{\\r\\n  \\\"query\\\" : \\\"+billToFirstName:Jan\\\",\\r\\n"
                + " \\\"filter\\\" : \\\"tenantId:MARS\\\",\\r\\n  \\\"limit\\\": 10,\\r\\n  \\\"offset\\\": 0,\\r\\n  "
                + " \\\"sort\\\": \\\"orderDate_sort asc,\\\"\\r\\n}\"}},\n"
                + "  \"response\":{\"numFound\":19,\"start\":0,\"numFoundExact\":true,\"docs\":[\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":5352411,\n"
                + "        \"_version_\":1746569148237873152},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":5362411,\n"
                + "        \"_version_\":1746569148222144512},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":5372411,\n"
                + "        \"_version_\":1746569148224241664},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10002411,\n"
                + "        \"_version_\":1746569148239970304},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10232411,\n"
                + "        \"_version_\":1746569148280864768},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10072411,\n"
                + "        \"_version_\":1746569148221095936},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10172411,\n"
                + "        \"_version_\":1746569148214804480},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10182411,\n"
                + "        \"_version_\":1746569148215853056},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10242411,\n"
                + "        \"_version_\":1746569148212707328},\n"
                + "      {\n"
                + "        \"deliveryOrderRef\":10252411,\n"
                + "        \"_version_\":1746569148211658752}]\n"
                + "  }}\n"
                + "";
        QueryResponse queryResponse = objectMapper.readValue(body, QueryResponse.class);
    }
}
