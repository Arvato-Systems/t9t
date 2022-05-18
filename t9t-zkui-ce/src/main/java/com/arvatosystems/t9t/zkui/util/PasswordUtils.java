/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.util;

/**
 * PasswordUtils.
 */
public class PasswordUtils {

    private final int defLength;
    private final int defLower;
    private final int defUpper;
    private final int defDigit;
    private final int defLetter; // how many letters (upper + lower case) must be part of the password?
    private final int defSpecial;
    private final int defMaxSubstring; // how many identical substring characters found in the password are allowed?

    private boolean passedLength = false;
    private boolean passedLower = false;
    private boolean passedUpper = false;
    private boolean passedDigit = false;
    private boolean passedLetter = false;
    private boolean passedSpecial = false;
    private boolean passedMaxSubs = true;


    /**
     * minimum password length - compete password.
     * password must contain minimum x numeric chars
     * password must contain minimum x special chars
     * password must contain minimum x UPPER chars
     * password must contain minimum x lOWER chars
     */
    public PasswordUtils(int length, int lower, int upper, int digit, int letter, int special, int maxSubstring) {
        this.defLength = length;
        this.defLower = lower;
        this.defUpper = upper;
        this.defLetter = letter;
        this.defDigit = digit;
        this.defSpecial = special;
        this.defMaxSubstring = maxSubstring;
    }

    /**
     *
     * @param oldPassword String
     * @param newPassword newPassword
     * @return boolean
     */
    public boolean verifyPasswordStrength(final String oldPassword, final String newPassword) {
        if (newPassword == null)
            // throw new IllegalArgumentException("Invalid password | New password cannot be null");
            return false;

        final int length = newPassword.length();
        int lower = 0;
        int upper = 0;
        int digit = 0;
        int special = 0;

        // can't change to a password that contains any (maxSubstring?) character substring of old password
        if (oldPassword != null && defMaxSubstring > 0) {
            for (int i = 0; i < oldPassword.length() - (defMaxSubstring - 1); i++) {
                final String sub = oldPassword.substring(i, i + defMaxSubstring);
                if (newPassword.indexOf(sub) > -1) {
                    passedMaxSubs = false;
                }
            }
        }

        // new password must have enough character sets and length
        for (int i = 0; i < newPassword.length(); i++) {
            final char c = newPassword.charAt(i);
            if (c >= 'a' && c <= 'z') {
                // count the lower case characters
                lower++;
            } else if (c >= 'A' && c <= 'Z') {
                // count the upper case characters
                upper++;
            } else if (c >= '0' && c <= '9') {
                // count the digits
                digit++;
            } else {
                // special character
                special++;
            }
        }

        // calculate and verify password strength
        passedLength = length >= defLength;
        passedLower = lower >= defLower;
        passedUpper = upper >= defUpper;
        passedDigit = digit >= defDigit;
        passedLetter = lower + upper >= defLetter;
        passedSpecial = special >= defSpecial;

        return passedLength && passedLower && passedUpper && passedDigit && passedLetter && passedSpecial && passedMaxSubs;
    }

    public final int getDefLength() {
        return defLength;
    }

    public final int getDefLower() {
        return defLower;
    }

    public final int getDefUpper() {
        return defUpper;
    }

    public final int getDefDigit() {
        return defDigit;
    }

    public final int getDefSpecial() {
        return defSpecial;
    }

    public final boolean isPassedLength() {
        return passedLength;
    }

    public final boolean isPassedLower() {
        return passedLower;
    }

    public final boolean isPassedUpper() {
        return passedUpper;
    }

    public final boolean isPassedDigit() {
        return passedDigit;
    }

    public final boolean isPassedSpecial() {
        return passedSpecial;
    }
}
