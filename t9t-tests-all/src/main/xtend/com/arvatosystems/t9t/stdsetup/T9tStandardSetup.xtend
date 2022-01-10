/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.voice.VoiceApplicationDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.bonaparte.pojos.api.media.MediaType
import java.util.UUID
import org.eclipse.xtend.lib.annotations.Data

import static extension com.arvatosystems.t9t.auth.extensions.AuthExtensions.*
import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*
import static extension com.arvatosystems.t9t.misc.extensions.VoiceExtensions.*
import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.DocConstants
import de.jpaw.bonaparte.pojos.api.media.MediaXType

@AddLogger
@Data
class T9tStandardSetup {
    ITestConnection dlg
    val USER_ID = "voiceBase"

    def void setupUIDownloadDataSink() {
        new DataSinkDTO => [
            dataSinkId              = T9tConstants.DATA_SINK_ID_UI_EXPORT
            isActive                = true
            description             = "Data sink for UI triggered user downloads"
            commFormatType          = MediaType.UNDEFINED
            commTargetChannelType   = CommunicationTargetChannelType.FILE
            fileOrQueueNamePattern  = "UIExports/${gridId}-${userId}-${asOf}.${fileExt}"
            category                = DataSinkCategoryType.USER_DATA
            merge(dlg)
        ]

        new DocConfigDTO => [
            documentId              = T9tConstants.DOCUMENT_ID_UI_EXPORT
            mappedId                = T9tConstants.DOCUMENT_ID_UI_EXPORT
            emailSettings           = new DocEmailReceiverDTO => [
                emailSubject        = "Your data export"
                subjectType         = TemplateType.INLINE;
                defaultFrom         = "ZK admin UI"
            ]
            description             = "Document ID for emailed data exports"
            merge(dlg)
        ]

        new DocTemplateDTO => [
            name                    = "Document template for emailed data exports"
            documentId              = T9tConstants.DOCUMENT_ID_UI_EXPORT
            entityId                = DocConstants.DEFAULT_ENTITY_ID
            languageCode            = DocConstants.DEFAULT_LANGUAGE_CODE
            countryCode             = DocConstants.DEFAULT_COUNTRY_CODE
            currencyCode            = DocConstants.DEFAULT_CURRENCY_CODE
            prio                    = 100
            mediaType               = MediaXType.of(MediaType.TEXT)
            template                = '''
                  Dear ${d.u.name},
                  please find attached your data export.
            '''
            merge(dlg)
        ]
    }

    /** Creates a user and API key for the global tenant. */
    def void setupVoiceBaseUser(UUID uuid) {
        new UserDTO => [
            userId                  = USER_ID
            name                    = "voice access root user"
            isTechnical             = true
            isActive                = true
            emailAddress            = "aro_3rdLevel@Bertelsmann.de"  // unused
            permissions             = new PermissionsDTO => [
                minPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                maxPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
            ]
            merge(dlg)
        ]
        new ApiKeyDTO => [
            apiKey                  = uuid
            name                    = "API-Key for voice access root user"
            isActive                = true
            userRef                 = new UserKey(USER_ID)
            permissions             = new PermissionsDTO => [
                minPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                maxPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
                resourceIsWildcard  = true
                resourceRestriction = "B.t9t.voice.api.ProvideSession"
            ]
            merge(dlg)
        ]
    }

    /** Creates an application and a user for the current tenant. The user is named as the applicationId */
    def void setupVoiceApplication(VoiceApplicationDTO application, String resources) {
        new UserDTO => [
            userId                  = application.applicationId
            name                    = application.name
            isTechnical             = true
            isActive                = application.isActive
            emailAddress            = "aro_3rdLevel@Bertelsmann.de"  // unused
            permissions             = new PermissionsDTO => [
                minPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                maxPermissions      = SetupUserTenantRole.ALL_PERMISSIONS
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
            ]
            merge(dlg)
        ]
        new ApiKeyDTO => [
            apiKey                  = application.apiKey
            name                    = "API-Key for voice application " + application.name
            isActive                = true
            userRef                 = new UserKey(application.applicationId)
            permissions             = new PermissionsDTO => [
                minPermissions      = Permissionset.ofTokens(OperationType.EXECUTE)
                maxPermissions      = SetupUserTenantRole.ALL_PERMISSIONS
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
                resourceIsWildcard  = true
                resourceRestriction = resources
            ]
            merge(dlg)
        ]

        // finally, create the application itself
        application.merge(dlg)
    }

    /** Installs the basic configuration in the @ tenant. */
    def void setupGlobalTenant(boolean withCamelConfig, String theSupportEmail, UUID passwordResetApiKey) {
        setupUIDownloadDataSink()
        val repSetup = new T9tRepSetup(dlg, true)
        repSetup.setupReportDataSinks
        repSetup.setupReportConfigs
        val emailSetup = new T9tEmailSetup(dlg)
        emailSetup.loadTemplates
        emailSetup.createPWUser(theSupportEmail, passwordResetApiKey)
    }
}
