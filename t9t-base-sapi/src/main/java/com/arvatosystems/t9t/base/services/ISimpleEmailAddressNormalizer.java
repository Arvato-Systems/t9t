package com.arvatosystems.t9t.base.services;

public interface ISimpleEmailAddressNormalizer {
    /** Normalizes an email address. the result is a lower case string without any display character portions. */
    String normalizeEmail(String emailIn);
}
