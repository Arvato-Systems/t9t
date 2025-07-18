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
package com.arvatosystems.t9t.embedded.tests.user

import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

class PasswordTest {

    static ITestConnection dlg

    @BeforeAll
    def static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def void createUserAndSetInitialPasswordTest() {
        val userDTO = new UserDTO => [
            userId      = "testUser"
            name        = "John Doe"
            isActive    = true
            validate
        ]
        val rq = new UserCrudAndSetPasswordRequest => [
            crud       = OperationType.MERGE
            data       = userDTO
            naturalKey = new UserKey(userDTO.userId)
            password   = "predefined"
            validate
        ]
        dlg.okIO(rq)

        //dlg.okIO(new ResetPasswordRequest)
    }

    @Test
    def void createUserAndChangeExistingPasswordTest() {
        val userDTO = new UserDTO => [
            userId      = "testUser"
            name        = "John Doe"
            isActive    = true
            validate
        ]
        val rq = new UserCrudAndSetPasswordRequest => [
            crud       = OperationType.MERGE
            data       = userDTO
            naturalKey = new UserKey(userDTO.userId)
            password   = SetupUserTenantRole.createRandomSimplePWForTests();
            validate
        ]
//      dlg.okIO(new ResetPasswordRequest => [ userId = ])
//      dlg.okIO(rq)
//      dlg.okIO(new ResetPasswordRequest)
    }
}
