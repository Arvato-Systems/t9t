package com.arvatosystems.t9t.core.services;

import java.util.regex.Pattern;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public interface IIbanChecker {
    record IbanSpec(
        String countryCode,
        String countryName,
        int totalLength,
        int bankCodeLength,
        int secondaryBankCodeLength,
        int accountNumberLength,
        Pattern pattern
    ) {
        public IbanSpec {
            if (countryCode == null || !countryCode.matches("[A-Z]{2}")) {
                throw new IllegalArgumentException("Invalid country code: " + countryCode);
            }
            if (pattern == null) {
                throw new IllegalArgumentException("IBAN pattern must not be null for " + countryCode);
            }
            if (totalLength < 5 || totalLength > 34) {
                throw new IllegalArgumentException("Invalid IBAN length: " + totalLength);
            }
            if (bankCodeLength < 0 || secondaryBankCodeLength < 0 || accountNumberLength < 0) {
                throw new IllegalArgumentException("Component lengths must be non-negative");
            }
        }
    }

    /**
     * Registers a new IBAN specification for a country, or updates an existing one.
     */
    void registerIbanSpec(@Nonnull IbanSpec ibanSpec);

    /**
     * Checks an IBAN for validity.
     * Validates registered country, exact national structure, and MOD-97 checksum.
     */
    boolean isValidIban(@Nonnull String iban);

    /**
     * Checks an bank code and / or bank account number for validity.
     * (Just validates the maximum length, in case the data is available).
     */
    boolean isValidBankCodeAndAccount(@Nonnull String countryCode, @Nullable String bankCode, @Nullable String accountNumber);
}
