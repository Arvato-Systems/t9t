/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.be.tests;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ValidIdTest {
    private static final Pattern ALLOWED_TENANT_ID_PATTERN = Pattern.compile("[_A-Za-z0-9]*");

    private boolean isTenantIdOk(final String id) {
        return ALLOWED_TENANT_ID_PATTERN.matcher(id).matches();
    }

    @Test
    public void testTenantId() {
        Assertions.assertTrue(isTenantIdOk("ACME6"));
        Assertions.assertFalse(isTenantIdOk("ACME-6"));
        Assertions.assertFalse(isTenantIdOk("/ACME6"));
        Assertions.assertFalse(isTenantIdOk("ACME6/"));
    }
}
