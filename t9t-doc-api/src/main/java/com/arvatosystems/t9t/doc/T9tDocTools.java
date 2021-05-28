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
package com.arvatosystems.t9t.doc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.doc.request.ConvertTemplatesRule;

public class T9tDocTools {
    private T9tDocTools() {}

    public static String getMailingGroupId(MailingGroupRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof MailingGroupKey) {
            return ((MailingGroupKey)ref).getMailingGroupId();
        }
        if (ref instanceof MailingGroupDTO) {
            return ((MailingGroupDTO)ref).getMailingGroupId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "MailingGroupRef of type " + ref.getClass().getCanonicalName());
    }

    private static final String COMMON_PREFIX = "d.";
    private static final char UNDERSCORE = '_';

    public static String convertTemplateAddOrSwapPrefix(String templateIn, ConvertTemplatesRule rule) {
        return convertTemplateAddOrSwapPrefix(templateIn, rule.getPrefixOld(), rule.getPrefixNew(), rule.getFieldsToExclude());
    }

    /**
     * Convert a template. All occurrences of prefixOld will be replaced by prefixNew, unless it is followed fieldsToExclude.
     * This is not a 100% safe conversion, because we do not parse the full template. An occurrence of "d." within regular text could
     * be falsely converted, but the probability is low.
     * It is not sufficient to check for ${d. only, because the variable could be used within an expression.
     **/
    public static String convertTemplateAddOrSwapPrefix(String templateIn, String prefixOld, String prefixNew, Collection<String> rawFieldsToExclude) {
        final String oldPattern = COMMON_PREFIX + prefixOld;
        final Collection<String> fieldsToExclude;
        if (rawFieldsToExclude == null || rawFieldsToExclude.isEmpty()) {
            // the only string to exclude is the new prefix, to avoid duplicate conversions
            fieldsToExclude = prefixNew.endsWith(".") ? Collections.singleton(prefixNew.substring(0, prefixNew.length()-1)) : Collections.emptySet();
        } else {
            if (prefixNew.endsWith(".")) {
                fieldsToExclude = new HashSet<>(rawFieldsToExclude);
                fieldsToExclude.add(prefixNew.substring(0, prefixNew.length()-1));
            } else {
                fieldsToExclude = rawFieldsToExclude;
            }
        }
        int nextCandidatePos = templateIn.indexOf(oldPattern);
        if (nextCandidatePos < 0) {
            // shortcut: no single occurrence
            return templateIn;
        }

        // iterate manually
        final StringBuilder sb = new StringBuilder(templateIn.length() + 100);
        int transferredUpTo = 0;
        while (nextCandidatePos >= 0) {
            // found a candidate. It should be replaced unless it is followed by one of the exclusion strings
            // in any case, copy up to start of the pattern, and including the pattern
            // source is [readPos ... nextCandidatePos + COMMON_PREFIX.length)
            sb.append(templateIn.substring(transferredUpTo, nextCandidatePos + COMMON_PREFIX.length()));
            transferredUpTo = nextCandidatePos + COMMON_PREFIX.length();

            // check if the previous character is not part of the same identifier
            final char previousChar = nextCandidatePos == 0 ? ' ' : templateIn.charAt(nextCandidatePos-1);
            if (previousChar != UNDERSCORE && !Character.isLetterOrDigit(previousChar)) {
                boolean skipThis = false;
                for (String exclusion: fieldsToExclude) {
                    if (skipThis || isExcluded(templateIn, nextCandidatePos, oldPattern, exclusion)) {
                        skipThis = true;
                    }
                }
                if (!skipThis) {
                    // skip the old pattern, instead transfer the new pattern
                    transferredUpTo += prefixOld.length();
                    sb.append(prefixNew);
                }
            }
            nextCandidatePos = templateIn.indexOf(oldPattern, transferredUpTo);
        }
        // transfer the remaining chunk of data...
        if (transferredUpTo < templateIn.length()) {
            sb.append(templateIn.substring(transferredUpTo, templateIn.length()));
        }
        return sb.toString();
    }

    private static final boolean isExcluded(String templateIn, int nextCandidatePos, String oldPattern, String exclusion) {
        if (!templateIn.startsWith(exclusion, nextCandidatePos + oldPattern.length())) {
            return false;  // does not start with it
        }
        // check for end of document
        if (templateIn.length() <= nextCandidatePos + oldPattern.length() + exclusion.length()) {
            return true;   // exclusions ends by end of document
        }
        // start with it, now check for end of word
        char nextChar = templateIn.charAt(nextCandidatePos + oldPattern.length() + exclusion.length());
        if (nextChar == UNDERSCORE || Character.isLetterOrDigit(nextChar)) {
            return false;  // starts with it, but identifier is continued
        }
        return true;
    }
}
