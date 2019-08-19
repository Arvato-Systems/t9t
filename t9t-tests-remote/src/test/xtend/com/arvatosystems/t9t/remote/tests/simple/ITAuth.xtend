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
package com.arvatosystems.t9t.remote.tests.simple

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO
import com.arvatosystems.t9t.auth.T9tAuthException
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.PasswordAuthentication
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.remote.connect.Connection
import java.util.UUID
import org.junit.Test

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*
import com.arvatosystems.t9t.authc.api.ResetPasswordRequest
import com.arvatosystems.t9t.doc.DocConfigDTO
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.bonaparte.pojos.api.media.MediaType
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.base.ITestConnection
import java.util.List

class ITAuth {
    val String testUU1D = "896d22d1-a1b2-488e-b2f8-2a539c29b8ca"
    val String testUUID = "896d22d1-a332-438e-b2f8-2a539c29b8ca"

    @Test
    def void pingTest() {
        val dlg = new Connection
        dlg.authentication = "API-Key " + testUUID
        dlg.okIO(new PingRequest)
    }
    @Test
    def void ping2Test() {
        val dlg = new Connection(UUID.fromString(testUUID))

        dlg.okIO(new PingRequest)
    }
    @Test
    def void createNewKeyTest() {
        val dlg = new Connection(UUID.fromString(testUUID))

        dlg.okIO(new PingRequest)
        val setup = new SetupUserTenantRole(dlg)
        setup.createApiKey("DUMMY", UUID.fromString(testUU1D))
    }

    @Test
    def void changePasswordFirstScenario() {
        val dlg = new Connection("userId", "changeMe", "changeMe2")
        dlg.changePassword("userId", "changeMe2", "changeMe3")
        dlg.changePassword("userId", "changeMe3", "changeMe")
    }

    @Test
    def void changePasswordSecondScenario() {
        val dlg = new Connection("userId", "changeMe")

        new AuthModuleCfgDTO => [
            passwordBlockingPeriod = 1
            passwordDifferPreviousN = 2
            passwordExpirationInDays = 60
            passwordMinimumLength = 5
            passwordThrottlingAfterX = 3
            merge(dlg)
        ]

        // checking length validation error
        dlg.errIO(authWithError("userId", "changeMe", "err"), T9tAuthException.PASSWORD_VALIDATION_FAILED)

        //checking password differ validation error
        dlg.errIO(authWithError("userId", "changeMe", "changeMe"), T9tAuthException.PASSWORD_VALIDATION_FAILED)

        new AuthModuleCfgDTO => [
            passwordBlockingPeriod = 0
            passwordDifferPreviousN = 2
            passwordExpirationInDays = 60
            passwordMinimumLength = 5
            passwordThrottlingAfterX = 3
            merge(dlg)
        ]

        // change back to the normal password
        dlg.changePassword("userId", "changeMe", "changeMe2")
        dlg.changePassword("userId", "changeMe2", "changeMe3")
        dlg.changePassword("userId", "changeMe3", "changeMe")
    }

    @Test
    def void resetPasswordTest() {
        val dlg = new Connection("userId", "changeMe")

        new AuthModuleCfgDTO => [
            passwordBlockingPeriod = 1
            passwordDifferPreviousN = 2
            passwordExpirationInDays = 60
            passwordMinimumLength = 5
            passwordThrottlingAfterX = 3
            merge(dlg)
        ]

        new DocConfigDTO => [
            documentId = "passwordReset"
            communicationFormat = MediaXType.of(MediaType.HTML)
            description = "forget password test configuration"
            useCids = false
            emailConfigPerSelector = false
            emailSettings = new DocEmailReceiverDTO => [
                sendSpooled = true
            ]
            merge(dlg)
        ]

        loadTemplates(newImmutableList("passwordReset"), dlg)

        val request = new ResetPasswordRequest => [
            userId = "userId"
        ]
        dlg.okIO(request)
    }

    static final UUID PWR_UUID = UUID.fromString("896d22d1-9999-7777-b2f8-2a539c29b8ca")

    @Test
    def void resetPasswordInSeparateClientTest() {
        val dlg = new Connection

        val setup = new SetupUserTenantRole(dlg)
        setup.createUserTenantRole("testPwr", PWR_UUID, true)

        new AuthModuleCfgDTO => [
            passwordBlockingPeriod = 1
            passwordDifferPreviousN = 2
            passwordExpirationInDays = 60
            passwordMinimumLength = 5
            passwordThrottlingAfterX = 3
            merge(dlg)
        ]

        new DocConfigDTO => [
            documentId = "passwordReset"
            communicationFormat = MediaXType.of(MediaType.HTML)
            description = "forget password test configuration"
            useCids = false
            emailConfigPerSelector = false
            emailSettings = new DocEmailReceiverDTO => [
                sendSpooled = true
                storeEmail  = true
            ]
            merge(dlg)
        ]

        loadTemplates(newImmutableList("passwordReset"), dlg)

        val request = new ResetPasswordRequest => [
            userId = "testPwr"
        ]
        dlg.okIO(request)
    }

    def authWithError(String userId, String password, String newPassword) {
        new AuthenticationRequest => [
            authenticationParameters = new PasswordAuthentication => [
                it.userId = userId
                it.password = password
                it.newPassword = newPassword
            ]
            sessionParameters = Connection.SESSION_PARAMETERS
        ]

    }

    def void loadTemplates(List<String> templateNames, ITestConnection dlg) {

        templateNames.forEach [ name |
            val templateData = ("doc/template/" + name + ".ftl").resourceAsHTML
            // store the templates as global defaults
            new DocTemplateDTO => [
                defaultKey
                documentId      = name
                mediaType       = templateData.mediaType
                template        = templateData.text
                it.name         = "test template " + name
                merge(dlg)
            ]
                   ]
    }

}
