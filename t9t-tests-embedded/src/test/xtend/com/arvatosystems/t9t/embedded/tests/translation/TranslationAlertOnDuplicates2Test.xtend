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
package com.arvatosystems.t9t.embedded.tests.translation

import com.arvatosystems.t9t.jdp.Init
import com.arvatosystems.t9t.translation.be.TranslationsStack
import de.jpaw.annotations.AddLogger
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// alternate implementation of the translation duplicate test, without an InMemoryConnection => needs a valid backend database for that reason
@AddLogger
class TranslationAlertOnDuplicates2Test {

    @Disabled
    @Test
    def public void alertOnDuplicateTranslationsTest() {
        Init.initializeT9t();  // initialize
        val numDuplicates = TranslationsStack.numberOfDuplicateTranslations
        if (numDuplicates != 0) {
            LOGGER.error("{} overwritten translation entries exist - check logs for details", numDuplicates)
            throw new Exception(numDuplicates + " duplicate translation entries exist")
        } else {
            LOGGER.info("Everything fine - no duplicate translations found")
        }
    }
}
