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
package com.arvatosystems.t9t.httppool.be.tests;

import org.junit.Test;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.httppool.be.HttpClientPool;

import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte;

public class ITHttpPool {

    // fails with Received response HTTP/1.1 405 Method Not Allowed
    @Test
    public void testGoogle() throws Exception {
        HttpClientPool pool = new HttpClientPool("www.google.com", 80, 10, new RecordMarshallerBonaparte());
        ServiceResponse resp = pool.executeRequest(new PingRequest(), "/", null, null);
    }

    // fails with Received response HTTP/1.1 500 Internal Server Error
    @Test
    public void testDev42() throws Exception {
        HttpClientPool pool = new HttpClientPool("degtlun2952.server.arvato-systems.de", 8880, 10, new RecordMarshallerBonaparte());
        ServiceResponse resp = pool.executeRequest(new PingRequest(), "/dev/rest/fortytwoslbon", null, null);
    }

    // fails with Received response HTTP/1.1 401 No or too short Authorization http Header
    @Test
    public void testDev28() throws Exception {
        HttpClientPool pool = new HttpClientPool("degtlun2952.server.arvato-systems.de", 8825, 10, new RecordMarshallerBonaparte());
        ServiceResponse resp = pool.executeRequest(new PingRequest(), "/rpc", null, null);
    }
}
