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
package com.arvatosystems.t9t.stdsetup

import com.arvatosystems.t9t.auth.ApiKeyDTO
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.api.TemplateType
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import java.util.UUID
import org.eclipse.xtend.lib.annotations.Data

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*

@AddLogger
@Data
class T9tEmailSetup {
    ITestConnection dlg

    private final Permissionset onlyExecPermission = Permissionset.ofTokens(OperationType.EXECUTE)
    private final Permissionset execReadPermission = Permissionset.ofTokens(OperationType.EXECUTE, OperationType.READ)



    def void loadDocConfigs(String mySubject, String myEmailAddress) {
        LOGGER.info("Loading standard doc configs")

        new DocConfigDTO => [
            documentId      = 'passwordReset'
            mappedId        = 'passwordReset'
            description     = 'Password Reset Mail'
            communicationFormat = MediaXType.of(MediaType.HTML)
            emailBodyTemplateId = 'passwordReset'
            emailSettings = new DocEmailReceiverDTO => [
                emailSubject     = mySubject ?: 'Your new password'
                defaultFrom      = myEmailAddress
                defaultReplyTo   = myEmailAddress
                subjectType      = TemplateType.INLINE
            ]
            merge(dlg)
        ]
    }

    def void loadTemplates() {
        LOGGER.info("Loading standard templates")

        #[ 'passwordReset' ].forEach [ name |
            LOGGER.info("Loading template resource {}", name)
            val templateData = ("doc/template/standard/" + name + ".ftl").resourceAsHTML
            // store the templates as global defaults
            new DocTemplateDTO => [
                defaultKey
                documentId      = name
                mediaType       = templateData.mediaType
                template        = templateData.text
                it.name         = "General " + name
                merge(dlg)
            ]
        ]
    }

    def void createPWUser(String myEmailAddress, UUID resetPasswordApiKey) {
        LOGGER.info("Create API key and user for forgot-my-password feature")

        new UserDTO => [
            userId         = "forgotPW"
            name           = "technical user for forgotPW"
            isActive       = true
            isTechnical    = true
            emailAddress   = myEmailAddress
            merge(dlg)
        ]
        new ApiKeyDTO => [
            apiKey         = resetPasswordApiKey
            userRef        = new UserKey("forgotPW")
            name           = "API key for forgotPW"
            isActive       = true
            permissions    = new PermissionsDTO => [
                minPermissions      = onlyExecPermission
                maxPermissions      = onlyExecPermission
                resourceRestriction = "B.t9t.authc.api.GetTenants,B.t9t.authc.api.ResetPassword"
                resourceIsWildcard  = Boolean.TRUE
            ]
            merge(dlg)
        ]
    }
}
