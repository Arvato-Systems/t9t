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
package com.arvatosystems.t9t.embedded.tests.updater;

import static com.arvatosystems.t9t.misc.extensions.MiscExtensions.createCannedRequestWithParameters;
import static com.arvatosystems.t9t.misc.extensions.UpdaterExtensions.patch;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.annotations.IsLogicallyFinal;
import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.request.LogMessageRequest;
import com.arvatosystems.t9t.base.request.PingRequest;
import com.arvatosystems.t9t.base.updater.AidDataRequest;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestKey;
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;

public class UpdaterTest {
    private static final CannedRequestKey KEY1 = new CannedRequestKey("ping12");
    private static final CannedRequestKey KEY2 = new CannedRequestKey("ping13");

    @IsLogicallyFinal  // keep checkstyle happy
    private static ITestConnection dlg;

    @BeforeAll
    public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection();

        createCannedRequestWithParameters(dlg, "ping12", "ping", new PingRequest());
        createCannedRequestWithParameters(dlg, "ping13", "pong", new PingRequest());
    }

    @AfterAll
    public static void done() {
        serverLog("tests DONE");
    }

    private static void serverLog(final String message) {
        dlg.okIO(new LogMessageRequest("============ " + message + " ==========="));
    }

    @Test
    public void testBadDtoClass() throws Exception {
        serverLog("testBadDtoClass");
        assertThrows(Exception.class, () -> {
            patch(dlg, CannedRequestCrudRequest.class, KEY1, dto -> { });
        });
    }

    @Test
    public void testPatchUpdateName() throws Exception {
        serverLog("testPatchUpdateName");
        patch(dlg, CannedRequestDTO.class, KEY1, dto -> {
            dto.setName("Patched ping!");
        });
    }

    @Test
    public void testPatchMultiple() throws Exception {
        serverLog("testPatchMultiple");
        final UnicodeFilter filter = new UnicodeFilter("requestId");
        filter.setLikeValue("ping1%");
        final int numCandidates = patch(dlg, CannedRequestDTO.class, filter, dto -> {
            if (dto.getRequestId().equals(KEY2.getRequestId())) {
                dto.setName("Super pong!");
                return KEY2;  // update!
            } else {
                return null;  // skip
            }
        });
        Assertions.assertEquals(2,  numCandidates, "Number of patch candidates");
    }

    @Test
    public void testActivate() throws Exception {
        serverLog("testActivate");
        final AidDataRequest aidRq = new AidDataRequest();
        aidRq.setDtoClassCanonicalName(CannedRequestDTO.class.getCanonicalName());
        aidRq.setKey(KEY1);
        aidRq.setOperation(OperationType.ACTIVATE);
        dlg.okIO(aidRq);
    }
}
