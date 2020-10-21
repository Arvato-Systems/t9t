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
package com.arvatosystems.t9t.tfi.web.security;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;


/**
 * PasswordUtils.
 * @author INCI02
 *
 */
public class PasswordUtils {

    int defLength = 0;
    int defLower = 0;
    int defUpper = 0;
    int defDigit = 0;
    int defSpecial = 0;
    boolean defCheckIsoControl=false;

    int length = 0;
    int lower = 0;
    int upper = 0;
    int digit = 0;
    int special = 0;
    int isoControl = 0;

    boolean passedLength = false;
    boolean passedLower = false;
    boolean passedUpper = false;
    boolean passedDigit = false;
    boolean passedSpecial = false;
    boolean passedIsoControl = false;

    StringBuffer verficationMessage = new StringBuffer("... not yet verified ...");

    public PasswordUtils() {}

    /**
     * minimum password length - compete password.
     * password must contain minimum x numeric chars
     * password must contain minimum x special chars
     * password must contain minimum x UPPER chars
     * password must contain minimum x lOWER chars
     */
    public PasswordUtils(int length, int lower, int upper, int digit, int special, boolean checkIsoControl) {
        this.defLength = length;
        this.defLower = lower;
        this.defUpper = upper;
        this.defDigit = digit;
        this.defSpecial = special;
        this.defCheckIsoControl =checkIsoControl;
    }

    /**
     * Generate a strong password.
     *
     * @return a new strong password
     */
    public final String generateStrongPassword() {
        Randomizer r = new Randomizer();

        String passLettersUpper = r.getRandomString(defUpper, CHAR_PASSWORD_UPPERS);
        String passLettersLower = r.getRandomString(defLower, CHAR_PASSWORD_LOWERS);
        String passSpecial = r.getRandomString(defSpecial, CHAR_PASSWORD_SPECIALS);
        String passDigits = r.getRandomString(defDigit, CHAR_PASSWORD_DIGITS);

        String minimalPassword = passLettersLower + passLettersUpper + passSpecial + passDigits;
        int remaining = defLength - minimalPassword.length();
        String remainingPassword = "";
        if (defLength > 0) {
            remainingPassword = r.getRandomString(remaining, CHAR_PASSWORD);
        }
        String shuffeledPassword = r.shuffle(minimalPassword + remainingPassword);
        return shuffeledPassword;
    }

    /**
     *
     * @param oldPassword String
     * @param newPassword newPassword
     * @return boolean
     */
    public boolean verifyPasswordStrength(String oldPassword, String newPassword) {
        if (newPassword == null)
            // throw new IllegalArgumentException("Invalid password | New password cannot be null");
            return false;

        // can't change to a password that contains any 3 character substring of old password
        if (oldPassword != null) {
            int length = oldPassword.length();
            for (int i = 0; i < length - 2; i++) {
                String sub = oldPassword.substring(i, i + 3);
                if (newPassword.indexOf(sub) > -1) {
                    // throw new
                    // IllegalArgumentException("Invalid password | New password cannot contain pieces of old password");
                    return false;
                }
            }
        }

        // new password must have enough character sets and length
        length = newPassword.length();
        for (int i = 0; i < newPassword.length(); i++)
            if (search(CHAR_LOWERS, newPassword.charAt(i))) {
                lower++;
            }
        for (int i = 0; i < newPassword.length(); i++) {
            if (search(CHAR_UPPERS, newPassword.charAt(i))) {
                upper++;
            }
        }
        for (int i = 0; i < newPassword.length(); i++)
            if (search(CHAR_DIGITS, newPassword.charAt(i))) {
                digit++;
            }
        for (int i = 0; i < newPassword.length(); i++) {
            if (search(CHAR_SPECIALS, newPassword.charAt(i))) {
                special++;
            }
        }
        if (defCheckIsoControl) {
            for (int i = 0; i < newPassword.length(); i++) {
                if (Character.isISOControl (newPassword.charAt(i))) {
                    isoControl++;
                }
            }
        }

        // calculate and verify password strength
        passedLength = length >= this.defLength;
        passedLower = lower >= this.defLower;
        passedUpper = upper >= this.defUpper;
        passedDigit = digit >= this.defDigit;
        passedSpecial = special >= this.defSpecial;
        passedIsoControl = isoControl == 0;


        boolean passed = passedLength && passedLower && passedUpper && passedDigit && passedSpecial && passedIsoControl;
        if (!passed) {
            verficationMessage = new StringBuffer();
            verficationMessage.append("\n** password not passed");
            verficationMessage.append("\n**   length (" + length + ")  >= def.length  (" + this.defLength + ")  " + (length >= this.defLength));
            verficationMessage.append("\n**   lower  (" + lower + ")  >= def.lower   (" + this.defLower + ")  " + (lower >= this.defLower));
            verficationMessage.append("\n**   upper  (" + upper + ")  >= def.upper   (" + this.defUpper + ")  " + (upper >= this.defUpper));
            verficationMessage.append("\n**   digit  (" + digit + ")  >= def.digit   (" + this.defDigit + ")  " + (digit >= this.defDigit));
            verficationMessage.append("\n**   special(" + special + ") >= def.special (" + this.defSpecial + ") " + (special >= this.defSpecial));
            verficationMessage.append("\n**   isoControl(" + isoControl + ") >= def.CheckIsoContro (" + this.defCheckIsoControl + ") " + (isoControl == 0));
        }

        // if (!passed) throw new
        // IllegalArgumentException("Invalid password | New password is not long and complex enough");
        return passed;
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
    public final boolean getDefCheckIsoControl() {
        return defCheckIsoControl;
    }
    public final int getIsoControl() {
        return isoControl;
    }
    public final boolean isPassedIsoControl() {
        return passedIsoControl;
    }
    public final int getLength() {
        return length;
    }

    public final int getLower() {
        return lower;
    }

    public final int getUpper() {
        return upper;
    }

    public final int getDigit() {
        return digit;
    }

    public final int getSpecial() {
        return special;
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


    @Override
    public String toString() {
        return this.verficationMessage.toString();
    }

    private static boolean search(char[] a, char key) {
        for (int j = 0; j < a.length; j++) {
            if (key == a[j]) {
                return true;
            }
        }
        return false;
    }
    /** Standard character sets */
    private static final char[] CHAR_LOWERS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
    private static final char[] CHAR_UPPERS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    private static final char[] CHAR_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
    private static final char[] CHAR_SPECIALS = { '.', '-', '_', '!', '@', '$', '^', '*', '=', '~', '|', '+', '?','#' };

 // The example displays the following output to the console:
//  \U0000    \U0001    \U0002    \U0003    \U0004    \U0005
//  \U0006    \U0007    \U0008    \U0009    \U000A    \U000B
//  \U000C    \U000D    \U000E    \U000F    \U0010    \U0011
//  \U0012    \U0013    \U0014    \U0015    \U0016    \U0017
//  \U0018    \U0019    \U001A    \U001B    \U001C    \U001D
//  \U001E    \U001F    \U007F    \U0080    \U0081    \U0082
//  \U0083    \U0084    \U0085    \U0086    \U0087    \U0088
//  \U0089    \U008A    \U008B    \U008C    \U008D    \U008E
//  \U008F    \U0090    \U0091    \U0092    \U0093    \U0094
//  \U0095    \U0096    \U0097    \U0098    \U0099    \U009A
//  \U009B    \U009C    \U009D    \U009E    \U009F


    /**
     * Password character set, is alphanumerics (without l, i, I, o, O, and 0) selected specials like + (bad for URL
     * encoding, | is like i and 1, etc...)
     */
    public static final char[] CHAR_PASSWORD_LOWERS = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's',
            't', 'u', 'v', 'w', 'x', 'y', 'z' };
    public static final char[] CHAR_PASSWORD_UPPERS = { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R',
            'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' };
    public static final char[] CHAR_PASSWORD_DIGITS = { '2', '3', '4', '5', '6', '7', '8', '9' };
    public static final char[] CHAR_PASSWORD_SPECIALS = { '_', '.', '!', '@', '$', '*', '=', '-', '?' };
    public static final char[] CHAR_PASSWORD_LETTERS = StringUtilities.union(CHAR_PASSWORD_LOWERS, CHAR_PASSWORD_UPPERS);
    public static final char[] CHAR_PASSWORD_LETTERS_DIGITS = StringUtilities.union(CHAR_PASSWORD_LETTERS, CHAR_PASSWORD_DIGITS);
    public static final char[] CHAR_PASSWORD = StringUtilities.union(CHAR_PASSWORD_LETTERS_DIGITS, CHAR_PASSWORD_SPECIALS);

    static class StringUtilities {

        /**
         * Union two character arrays.
         *
         * @param c1 the c1
         * @param c2 the c2
         * @return the char[]
         */
        public static char[] union(char[] c1, char[] c2) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < c1.length; i++) {
                if (!contains(sb, c1[i])) {
                    sb.append(c1[i]);
                }
            }
            for (int i = 0; i < c2.length; i++) {
                if (!contains(sb, c2[i])) {
                    sb.append(c2[i]);
                }
            }
            char[] c3 = new char[sb.length()];
            sb.getChars(0, sb.length(), c3, 0);
            Arrays.sort(c3);
            return c3;
        }

        /**
         * Returns true if the character is contained in the provided StringBuffer.
         */
        public static boolean contains(StringBuffer haystack, char c) {
            for (int i = 0; i < haystack.length(); i++) {
                if (haystack.charAt(i) == c) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Reference implementation of the Randomizer interface. This implementation builds on the JCE provider to provide a
     * cryptographically strong source of entropy. The specific algorithm used is configurable in ESAPI.properties.
     *
     * @author Jeff Williams (jeff.williams .at. aspectsecurity.com) <a href="http://www.aspectsecurity.com">Aspect
     *         Security</a>
     * @since June 1, 2007
     * @see org.owasp.esapi.Randomizer
     */
    public class Randomizer {

        /** The sr. */
        private SecureRandom secureRandom = null;

        /**
         * Hide the constructor for the Singleton pattern.
         */
        public Randomizer() {
            String algorithm = "SHA1PRNG";
            try {
                secureRandom = SecureRandom.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                // Can't throw an exception from the constructor, but this will get
                // it logged and tracked
                throw new IllegalArgumentException("Error creating randomizer | Can't find random algorithm " + algorithm, e);
            }
        }

        /**
         * {@inheritDoc}
         */
        public String getRandomString(int length, char[] characterSet) {
            StringBuffer sb = new StringBuffer();
            for (int loop = 0; loop < length; loop++) {
                int index = secureRandom.nextInt(characterSet.length);
                sb.append(characterSet[index]);
            }
            String nonce = sb.toString();
            return nonce;
        }

        public int getRandomInteger(int min, int max) {
            return secureRandom.nextInt(max - min) + min;
        }

        public String shuffle(String text) {
            if (text.length() <= 1)
                return text;

            int split = text.length() / 2;

            String temp1 = shuffle(text.substring(0, split));
            String temp2 = shuffle(text.substring(split));

            if (Math.random() > 0.5)
                return temp1 + temp2;
            else
                return temp2 + temp1;
        }

    }
}
