/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.services.ISimpleEmailAddressNormalizer;

import de.jpaw.dp.Singleton;

@Singleton
public class SimpleEmailAddressNormalizer implements ISimpleEmailAddressNormalizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleEmailAddressNormalizer.class);

    private static final String ATOM = "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]";
    private static final String DOMAIN = ATOM + "+(\\." + ATOM + "+)*";
    private static final String IP_DOMAIN = "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

    private static final String EMAIL_REGEX = ATOM + "+(\\." + ATOM + "+)*" + "@" + DOMAIN + "|" + IP_DOMAIN;
    private static final String EMAIL_WITH_DISPLAY_NAME_REGEX = ".*<(" + EMAIL_REGEX + ")>";

    private static final String GOOGLE_DOMAIN_1 = "gmail.com";
    private static final String GOOGLE_DOMAIN_2 = "googlemail.com";

    private static final Pattern EMAIL_WITH_DISPLAY_NAME_REGEX_PATTERN = Pattern.compile(EMAIL_WITH_DISPLAY_NAME_REGEX);

    @Override
    public String normalizeEmail(final String emailIn) {
        String emailAddress = T9tUtil.spaceNormalize(emailIn);
        final Matcher matcher = EMAIL_WITH_DISPLAY_NAME_REGEX_PATTERN.matcher(emailAddress);
        if (matcher.matches()) {
            emailAddress = matcher.group(1);
        }
        emailAddress = emailAddress.trim().toLowerCase();
        final int atIndex = emailAddress.indexOf("@");
        if (atIndex <= 0) {
            LOGGER.debug("Unable to normalize email address {}, it doesn't seem to be a valid email address format.", emailIn);
            throw new T9tException(T9tException.INVALID_EMAIL_FORMAT, emailIn);
        }

        String domain = emailAddress.substring(atIndex + 1);
        if (domain.equals(GOOGLE_DOMAIN_1) || domain.equals(GOOGLE_DOMAIN_2)) {
            return normalizeGoogleMail(emailAddress);
        } else {
            return normalizeOtherMail(emailAddress);
        }
    }

    /** Just a hook to be overridden in case needed. */
    protected String normalizeOtherMail(final String emailAddress) {
        return emailAddress;
    }

    /** Special handling for google domains. */
    protected String normalizeGoogleMail(final String emailAddress) {
        final StringBuilder sb = new StringBuilder(emailAddress.length());
        boolean skip = false; //To skip the text after '+'
        boolean afterAt = false;

        for (int i = 0; i < emailAddress.length(); i++) {
            final char c = emailAddress.charAt(i);
            if (c == '+') {
                skip = true;
            } else if (c == '@') {
                afterAt = true;
            }

            if (afterAt || (!skip && c != '.')) {
                sb.append(emailAddress.charAt(i));
            }
        }

        return sb.toString();
    }
}
