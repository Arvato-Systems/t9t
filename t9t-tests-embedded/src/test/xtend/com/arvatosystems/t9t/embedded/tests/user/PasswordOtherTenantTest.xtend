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
import de.jpaw.util.ApplicationException
import de.jpaw.util.ExceptionUtil
import java.util.UUID
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*
import com.arvatosystems.t9t.base.T9tException
import de.jpaw.annotations.AddLogger

@AddLogger
class PasswordOtherTenantTest {
    static private ITestConnection dlg

    static private final String TEST_USER_ID = "userOT"
    static private final String TEST_EMAIL = "test@nowhere.com"

    @BeforeAll
    def public static void createConnection() {
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
            user.merge(dlg)
            // do some resets
            dlg.okIO(new ResetPasswordRequest(TEST_USER_ID, TEST_EMAIL))
            dlg.okIO(new ResetPasswordRequest(TEST_USER_ID, TEST_EMAIL))
            // set the password to a defined value
            val rq = new UserCrudAndSetPasswordRequest => [
                crud       = OperationType.MERGE
                data       = user
                naturalKey = new UserKey(TEST_USER_ID)
                password   = "secret12345"
                validate
            ]
            dlg.okIO(rq)

        } catch (Exception e) {
            println("Exception during init: " + ExceptionUtil.causeChain(e))
        }
    }

    @Test
    def public void loginViaPresetPasswordTest() {
        new InMemoryConnection(TEST_USER_ID, "secret12345")
    }

    @Test
    def public void loginViaBadPasswordTest() {
        try {
            new InMemoryConnection(TEST_USER_ID, "secret12344")
            throw new RuntimeException("exception expected")
        } catch (ApplicationException e) {
            if (e.errorCode != T9tException.T9T_ACCESS_DENIED) {
                LOGGER.error("Expected exception {}, but got {}", T9tException.T9T_ACCESS_DENIED, e.errorCode)
                throw e
            }
            // else OK, expected this exception
        }
    }

    @Test
    def public void loginViaBadUserTest() {
        try {
            new InMemoryConnection("NoRealUser", "secret12344")
            throw new RuntimeException("exception expected")
        } catch (ApplicationException e) {
            if (e.errorCode != T9tException.USER_NOT_FOUND) {
                LOGGER.error("Expected exception {}, but got {}", T9tException.USER_NOT_FOUND, e.errorCode)  // TODO: check if this should be replaced by a generic error code
                throw e
            }
            // else OK, expected this exception
        }
    }
}
