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
package com.arvatosystems.t9t.embedded.tests.simple

import com.arvatosystems.t9t.ai.AiAssistantDTO
import com.arvatosystems.t9t.ai.AiAssistantKey
import com.arvatosystems.t9t.ai.AiDtoAssistDTO
import com.arvatosystems.t9t.ai.request.AiCreateOrEditDtoResponse
import com.arvatosystems.t9t.ai.request.GenericAiCreateOrEditDtoRequest
import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRoleNoDeletePermissions
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.io.CsvConfigurationDTO
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.util.ToStringHelper
import java.util.UUID
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static extension com.arvatosystems.t9t.misc.extensions.AiExtensions.*

@AddLogger
class ITOpenAI {

    // disabled by default in order not to consume tokens
    @Disabled
    @Test
    def void OpenAICreateDTOTest1() {
        val dlg = new InMemoryConnection

        val setup = new SetupUserTenantRoleNoDeletePermissions(dlg)

        val newKey = UUID.randomUUID
        setup.createUserTenantRole("openai", newKey, true)

        new AiAssistantDTO => [
            assistantId         = "gpt"
            description         = "Test for DTO creation"
            isActive            = true
            languageCode        = "en"
            aiProvider          = "OpenAI"
            model               = "gpt-5.6-terra"
            greeting            = "Hello, tell me what you need"
            instructions        = '''
                You are an expert assisting the user in configuration tasks.
                You only provide answer you have high confidence in.
                In case of questions or doubt, ask the user for confirmation.
                You MUST define all properties listed as required. For booleans, you can use false as default, except isActive, which should be true unless requested otherwise.
            '''
            // temperature         = 0.0F  // not supported for this model
            merge(dlg)
        ]
        new AiDtoAssistDTO => [
            pqon                = CsvConfigurationDTO.class$MetaData.name
            description         = "Test for DTO creation"
            aiAssistantRef      = new AiAssistantKey("gpt")
            role                = "system"
            dtoInstructions     = "Create the configuration for CSV file import or export"
            merge(dlg)
        ]

        // run the test
        val rq = new GenericAiCreateOrEditDtoRequest => [
            pqon                = CsvConfigurationDTO.class$MetaData.name
            chatInput           = "Create configuration for CSV import with German locale and medium date format, with delimiter pipe symbol. The configuration ID should be csvDE-pipe."
        ]
        val resp = dlg.typeIO(rq, AiCreateOrEditDtoResponse)
        LOGGER.info("Result is {}", ToStringHelper.toStringML(resp))
    }

    // always disabled dummy test, to keep the import of Disabled in, without warning
    @Disabled
    @Test
    def void dummy() {
    }


}
