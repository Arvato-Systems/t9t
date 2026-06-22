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
package com.arvatosystems.t9t.stdsetup

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.doc.DocConfigDTO
import com.arvatosystems.t9t.doc.DocConstants
import com.arvatosystems.t9t.doc.DocEmailReceiverDTO
import com.arvatosystems.t9t.doc.DocTemplateDTO
import com.arvatosystems.t9t.doc.api.TemplateType
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import java.util.UUID
import org.eclipse.xtend.lib.annotations.Data

import static extension com.arvatosystems.t9t.doc.extensions.DocExtensions.*
import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*

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
            writeHeaderRow          = Boolean.TRUE
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
