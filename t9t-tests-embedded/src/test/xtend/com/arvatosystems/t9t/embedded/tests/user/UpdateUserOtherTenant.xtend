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
import com.arvatosystems.t9t.auth.request.UserCrudRequest
import com.arvatosystems.t9t.auth.request.UserSearchRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import de.jpaw.util.ExceptionUtil
import java.util.UUID
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import de.jpaw.annotations.AddLogger

@AddLogger
class UpdateUserOtherTenant {
    static private ITestConnection dlg

    static private final String TEST_USER_ID = "userOT2"
    static private final String TEST_EMAIL = "test@nowhere.com"
    static private UUID otherTenantUUID;

    @BeforeClass
    def public static void createConnection() {
        try {
            // use a single connection for all tests (faster)
            dlg = new InMemoryConnection

            otherTenantUUID = UUID.randomUUID
            val setup = new SetupUserTenantRole(dlg)
            setup.createUserTenantRole(TEST_USER_ID, otherTenantUUID, false)
        } catch (Exception e) {
            LOGGER.error("Exception during init: " + ExceptionUtil.causeChain(e))
        }
    }

    def void readTenantOfUser() {
        val users = dlg.typeIO(new UserSearchRequest => [
            searchFilter     = new AsciiFilter => [
                fieldName    = "userId"
                equalsValue  = TEST_USER_ID
            ]
        ], ReadAllResponse).dataList
        Assert.assertEquals(1, users.size)

        val myUser = users.get(0) as DataWithTrackingW<UserDTO, FullTrackingWithVersion>
        LOGGER.info("TenantRef is {}", myUser.tenantRef)
    }

    @Test
    def public void attemptToUpdateUserOtherTenantTest() {
        dlg.lastJwtInfo => [
            LOGGER.info("I am tenant {} (ref {}), user ID {} (ref {})", tenantId, tenantRef, userId, userRef)
        ]
        readTenantOfUser

        val user = new UserKey(TEST_USER_ID).read(dlg)
        user.emailAddress  = TEST_EMAIL
        user.merge(dlg)

        readTenantOfUser
        val user2 = new UserKey(TEST_USER_ID).read(dlg)

        user2.office = "New office"
        dlg.okIO(new UserCrudRequest => [
            data     = user2
            key      = user2.objectRef
            crud     = OperationType.UPDATE
            validate
        ])
        readTenantOfUser
        val user3 = new UserKey(TEST_USER_ID).read(dlg)
    }
}
