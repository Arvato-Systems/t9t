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
package com.arvatosystems.t9t.embedded.tests.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.jpaw.bonaparte.pojos.meta.ClassDefinition;

import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.entities.SessionTracking;
import com.arvatosystems.t9t.base.request.GetClassInformationRequest;
import com.arvatosystems.t9t.base.request.GetClassInformationResponse;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;

public class GetClassMetadataTest {
    private static ITestConnection dlg;

    @BeforeAll
    public static void createConnection() {
        dlg = new InMemoryConnection();
    }

    @Test
    public void getClassMetadata() throws Exception {
        final ClassDefinition cd = dlg.typeIO(new GetClassInformationRequest(SessionTracking.my$PQON), GetClassInformationResponse.class).getClassDefinition();
        Assertions.assertNotNull(cd.getFields(), "Expected fields in SessionTracking");
        Assertions.assertEquals(2, cd.getFields().size(), "Expected 2 fields in SessionTracking");
        Assertions.assertEquals("cTimestamp", cd.getFields().get(0).getName());
        Assertions.assertEquals("mTimestamp", cd.getFields().get(1).getName());
        final ClassDefinition cd2 = cd.getParentMeta();
        Assertions.assertNotNull(cd2, "Expected SessionTracking to have a parent class");
        Assertions.assertEquals("api.TrackingBase", cd2.getName(), "Parent class of SessionTracking should be TrackingBase");
        Assertions.assertNotNull(cd2.getFields(), "Expected fields in TrackingBase");
        Assertions.assertEquals(0, cd2.getFields().size(), "Expected 0 fields in TrackingBase");
    }
}
