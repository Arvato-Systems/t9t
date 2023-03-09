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
