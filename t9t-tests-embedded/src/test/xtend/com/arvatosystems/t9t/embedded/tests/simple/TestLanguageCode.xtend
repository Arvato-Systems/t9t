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
package com.arvatosystems.t9t.embedded.tests.simple

import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.msglog.MessageDTO
import org.joda.time.Instant
import org.junit.Test

// errors have been reported with language codes according to BCP47

class TestLanguageCode {

    @Test
    def testLanguages() {
        val msglog = new MessageDTO => [  // create an object which contains a language code with pattern
            sessionRef              = 1L
            tenantRef               = T9tConstants.GLOBAL_TENANT_REF42
            userId                  = "john"
            executionStartedAt      = new Instant
            requestParameterPqon    = "dummy.PingRequest"
            validate
        ]
        #[ "en", "de", "en_GB", "en-US" ].forEach [
            println("Testing language code " + it)
            msglog.languageCode = it
            msglog.validate
        ]
    }
}
