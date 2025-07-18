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

import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.util.ExceptionUtil
import java.util.UUID
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*

class CustomZFieldTest {
    static ITestConnection dlg

    static final String TEST_USER_ID = "testZ"
    static final String TEST_EMAIL = "test@nowhere.com"
    static final String TEST_PW = SetupUserTenantRole.createRandomSimplePWForTests

    @BeforeAll
    def static void createConnection() {
        try {
            // use a single connection for all tests (faster)
            dlg = new InMemoryConnection

            // ignore any password reset emails...
            new DocConfigDTO => [
                documentId            = "passwordReset"
                description           = "reset password document"
                emailSettings         = new DocEmailReceiverDTO => [
                    storeEmail        = true
                ]
                merge(dlg)
            ]

            val setup = new SetupUserTenantRole(dlg)
            setup.createUserTenantRole(TEST_USER_ID, UUID.randomUUID, false)
            val user = new UserKey(TEST_USER_ID).read(dlg)
            user.emailAddress  = TEST_EMAIL
            user.z = #{ "userField" -> "test1" }
            user.merge(dlg)
            // do some resets
            dlg.okIO(new ResetPasswordRequest(TEST_USER_ID, TEST_EMAIL))
            dlg.okIO(new ResetPasswordRequest(TEST_USER_ID, TEST_EMAIL))

            // set the password to a defined value
            val rq = new UserCrudAndSetPasswordRequest => [
                crud       = OperationType.MERGE
                data       = user
                naturalKey = new UserKey(TEST_USER_ID)
                password   = TEST_PW
                validate
            ]
            dlg.okIO(rq)
        } catch (Exception e) {
            println("Exception during init: " + ExceptionUtil.causeChain(e))
        }
    }

    @Test
    def void loginAndCheckZFieldsOfJWTTest() {
        val tenant = TEST_USER_ID.read(dlg)
        tenant.z = #{ "tenantField" -> "test2" }
        tenant.merge(dlg)

        val dlgx = new InMemoryConnection(TEST_USER_ID, TEST_PW)
        val info = dlgx.lastJwtInfo
        println('''The z field of the login is «info.z»''')
        Assertions.assertNotNull(info.z, "There should be a z field in the JWT")
        Assertions.assertEquals(2, info.z.size, "There should be 2 entries in the z field")
        Assertions.assertEquals(info.z.get("userField"), "test1")
        Assertions.assertEquals(info.z.get("tenantField"), "test2")
    }
}
