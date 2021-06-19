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
package com.arvatosystems.t9t.embedded.tests.user

import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import org.junit.jupiter.api.Test

@AddLogger
class CreateUserTwiceTest {
    public static val ALL_PERMISSIONS = new Permissionset(0xfffff)  // .fromStringMap("XSLCRUDIAVMP")

    def getPermissionDTO() {
        return new PermissionsDTO => [
            logLevel            = UserLogLevelType.REQUESTS
            logLevelErrors      = UserLogLevelType.REQUESTS
            minPermissions      = Permissionset.ofTokens(OperationType.LOOKUP)
            maxPermissions      = ALL_PERMISSIONS
            resourceIsWildcard  = Boolean.TRUE
            resourceRestriction = "B.,"
        ]
    }

    def makeRq() {
        val myUserId = "demouser"
        // set the password to a defined value
        return new UserCrudAndSetPasswordRequest => [
            crud       = OperationType.MERGE
            data       = new UserDTO => [
                userId       = myUserId
                isActive     = true
                emailAddress = "Michael.Bischoff@Bertelsmann.de"
                permissions  = permissionDTO
            ]
            naturalKey = new UserKey(myUserId)
            password   = "secret1234"
            validate
        ]
    }

    def void process(ITestConnection dlg) {
        val resp = dlg.typeIO(makeRq, CrudSurrogateKeyResponse)
        LOGGER.info("Returned objectRef {}", resp.key)
    }

    @Test
    def void createUserTest() {
        val dlg = new InMemoryConnection;
        // send request once!
        LOGGER.debug("Create user - first time")
        dlg.process
        // and again!
        LOGGER.debug("Create user - second time")
        dlg.process
        // and again!
        LOGGER.debug("Create user - third time")
        dlg.process
    }
}
