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
package com.arvatosystems.t9t.client.tests

import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.client.TestSession
import io.vertx.ext.unit.TestContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

// @RunWith(VertxUnitRunner)
public class TestClientTest extends TestSession {

    @BeforeEach
    override public void setUp(TestContext context) {
        super.setUp(context)
    }

    @AfterEach
    override public void tearDown(TestContext context) {
        super.tearDown(context)
    }

    @Test
    def public void testGetFavicon(TestContext context) {
        val async = context.async
        exec(new PauseRequest)
        async.complete
    }
}
