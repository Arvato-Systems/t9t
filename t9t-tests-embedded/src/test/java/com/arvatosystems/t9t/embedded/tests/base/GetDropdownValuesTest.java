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
package com.arvatosystems.t9t.embedded.tests.base;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.request.GetDropdownValuesRequest;
import com.arvatosystems.t9t.base.request.GetDropdownValuesResponse;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;

public class GetDropdownValuesTest {
    private static ITestConnection dlg;

    @BeforeAll
    public static void createConnection() {
        dlg = new InMemoryConnection();
    }

    private static GetDropdownValuesResponse getDropdownValues(final String dropdownId) throws Exception {
        return dlg.typeIO(new GetDropdownValuesRequest(dropdownId), GetDropdownValuesResponse.class);
    }

    @Test
    public void languageCodeDropdownValues() throws Exception {
        final GetDropdownValuesResponse resp = getDropdownValues("languageCode");
        Assertions.assertNotNull(resp.getValues());
        Assertions.assertTrue(resp.getValues().contains("en"), "Expected 'en' in language codes");
    }

    @Test
    public void timeZoneIdDropdownValues() throws Exception {
        final GetDropdownValuesResponse resp = getDropdownValues("timeZoneId");
        Assertions.assertNotNull(resp.getValues());
        Assertions.assertTrue(resp.getValues().contains("Europe/Berlin"), "Expected 'Europe/Berlin' in time zone IDs");
    }

    @Test
    public void charsetEncodingDropdownValues() throws Exception {
        final GetDropdownValuesResponse resp = getDropdownValues("charsetEncoding");
        Assertions.assertNotNull(resp.getValues());
        Assertions.assertTrue(resp.getValues().contains("UTF-8"), "Expected 'UTF-8' in charset encodings");
    }

    @Test
    public void countryCodeDropdownValues() throws Exception {
        final GetDropdownValuesResponse resp = getDropdownValues("countryCode");
        Assertions.assertNotNull(resp.getValues());
        Assertions.assertTrue(resp.getValues().contains("DE"), "Expected 'DE' in country codes");
        Assertions.assertTrue(resp.getValues().contains("EU"), "Expected 'EU' in country codes");
    }

    @Test
    public void currencyCodeDropdownValues() throws Exception {
        final GetDropdownValuesResponse resp = getDropdownValues("currencyCode");
        Assertions.assertNotNull(resp.getValues());
        Assertions.assertTrue(resp.getValues().contains("EUR"), "Expected 'EUR' in currency codes");
        Assertions.assertTrue(resp.getValues().contains("USD"), "Expected 'USD' in currency codes");
    }

    @Test
    public void invalidDropdownIdIsRejected() throws Exception {
        dlg.errIO(new GetDropdownValuesRequest("invalidDropdownId"), T9tException.INVALID_PARAMETER);
    }
}
