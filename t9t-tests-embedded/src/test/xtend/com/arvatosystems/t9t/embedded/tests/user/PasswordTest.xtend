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
package com.arvatosystems.t9t.embedded.tests.user

import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import org.junit.BeforeClass
import org.junit.Test

class PasswordTest {

    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    @Test
    def public void createUserAndSetInitialPasswordTest() {
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
    def public void createUserAndChangeExistingPasswordTest() {
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
//      dlg.okIO(new ResetPasswordRequest => [ userId = ])
//      dlg.okIO(rq)
//      dlg.okIO(new ResetPasswordRequest)
    }
}
