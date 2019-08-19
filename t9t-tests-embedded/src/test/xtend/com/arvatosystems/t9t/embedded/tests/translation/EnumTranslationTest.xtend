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
package com.arvatosystems.t9t.embedded.tests.translation

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationRequest
import com.arvatosystems.t9t.translation.request.GetEnumsTranslationResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.util.ToStringHelper
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.Test

@AddLogger
class EnumTranslationTest {
    static private ITestConnection dlg

    @BeforeClass
    def public static void createConnection() {
        // use a single connection for all tests (faster)
        dlg = new InMemoryConnection
    }

    def void testEnumTranslation(String pqon, String language, boolean doUseFallback) {
        val rq = new GetEnumsTranslationRequest => [
            enumPQONs           = #[ pqon ]
            overrideLanguage    = language
            useFallback         = doUseFallback
        ]
        val res = dlg.typeIO(rq, GetEnumsTranslationResponse).translations.get(0)
        Assert.assertEquals(res.enumPQON, pqon)
        LOGGER.info(ToStringHelper.toStringML(res.instances))
    }
    def void testEnumTranslation(String pqon, String language, boolean doUseFallback, int expectedError) {
        val rq = new GetEnumsTranslationRequest => [
            enumPQONs           = #[ pqon ]
            overrideLanguage    = language
            useFallback         = doUseFallback
        ]
        dlg.errIO(rq, expectedError)
    }

    @Test
    def public void enumTransEnTest() {
        testEnumTranslation("t9t.ssm.SchedulerSetupRecurrenceType", "en", false)
    }
    @Test
    def public void enumTransDeTest() {
        testEnumTranslation("t9t.ssm.SchedulerSetupRecurrenceType", "de", false)
    }
    @Test
    def public void enumTransThTest() {
        testEnumTranslation("t9t.doc.api.TemplateType", "th", true)
    }
    @Test
    def public void enumTransTh2Test() {
        testEnumTranslation("t9t.doc.api.TemplateType", "th", false)
    }

    @Test
    def public void enumTransNonExistingEnumTest() {
        testEnumTranslation("t9t.NonExistingClass", "de", false, T9tException.NOT_AN_ENUM)
    }
    @Test
    def public void enumTransNotAnEnumTest() {
        testEnumTranslation("t9t.base.api.ServiceResponse", "de", false, T9tException.NOT_AN_ENUM)
    }
}
