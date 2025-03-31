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
package com.arvatosystems.t9t.doc.be.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.doc.services.IDocTextReplacer;

import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

@Singleton
public class DocTextReplacer implements IDocTextReplacer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocTextReplacer.class);
    private static final String CONVERT_CASE           = ConfigProvider.getCustomParameter("docCase");
    private static final String DEFAULT_SEPARATOR_CHARS = T9tUtil.nvl(ConfigProvider.getCustomParameter("docSeparators"),
            "- ,");
    private static final String MAP_FROM                = T9tUtil.nvl(ConfigProvider.getCustomParameter("docMapFrom"),
            "äöüÄÖÜßãõñÃÕÑçÇåÅ®©¢"
            + "áàâÁÀÂéèêÉÈÊíìîÍÌÎóòôÓÒÔúùûÚÙÛëïËÏ");
    private static final String MAP_TO                  = T9tUtil.nvl(ConfigProvider.getCustomParameter("docMapTo"),
            "ae,oe,ue,Ae,Oe,Ue,ss,a,o,n,A,O,N,c,C,a,A,R,c,c,"
            + "a,a,a,A,A,A,e,e,e,E,E,E,i,i,i,I,I,I,o,o,o,O,O,O,u,u,u,U,U,U,e,i,E,I");
    private static final Map<Character, String> MAPPINGS = new HashMap<>(200);
    private static final List<Map<Character, String>> DEFAULT_MAPPINGS = List.of(MAPPINGS);

    private enum CaseConversion {
        KEEP, TOUPPER, TOLOWER
    }

    private static final CaseConversion CASE_CONVERSION = "L".equals(CONVERT_CASE)
            ? CaseConversion.TOLOWER
            : "U".equals(CONVERT_CASE) ? CaseConversion.TOUPPER : CaseConversion.KEEP;

    static {
        final String[] mapTo = MAP_TO.split(",");
        if (MAP_FROM.length() != mapTo.length) {
            LOGGER.error("Bad docMap configuration: from has {} chars, to has {} strings", MAP_FROM.length(), mapTo.length);
        }
        final int limit = Math.min(MAP_FROM.length(), mapTo.length);
        for (int i = 0; i < limit; ++i) {
            MAPPINGS.put(MAP_FROM.charAt(i), mapTo[i]);
        }
        LOGGER.info("Created mapping for {} characters");
    }

    /**
     * Returns a list of mappings to try. The default implementation returns a single element list with the default mapping, which covers the DACH region and France.
     * The default mapping list does not depend on the locale. Customizations may decide to depend on language/country, then language, and finally a global fallback list.
     */
    protected List<Map<Character, String>> getMappingList(@Nonnull final Locale locale) {
        return DEFAULT_MAPPINGS;
    }

    /**
     * Obtains a word delimiter string, based on locale and list of separator characters.
     * The default implementation returns a single-character string which is taken from the first character of the delimiter character alternatives.
     */
    protected String getDelimiter(final Locale locale, final String effectiveDelimiters) {
        return effectiveDelimiters.isEmpty() ? "-" : effectiveDelimiters.substring(0, 1);
    }

    /**
     * Obtains the mapping of a given character, based on the provided list of alternatives, or null, if the character should be transferred 1:1.
     * The default implementation returns the first match.
     */
    protected String getReplacement(final List<Map<Character, String>> mappings, final char c) {
        for (final Map<Character, String> mapping: mappings) {
            final String mappingResult = mapping.get(c);
            if (mappingResult != null) {
                return mappingResult;
            }
        }
        return null;  // no mapping found in any of the candidates
    }

    /**
     * Replaces specific characters by substitutions.
     * The default implementation only evaluate the locale for the upper/lowercase conversion.
     */
    @Override
    public String textReplace(final Locale locale, final String text, final String delimiters) {
        final String effectiveDelimiters = T9tUtil.nvl(delimiters, DEFAULT_SEPARATOR_CHARS);
        final String delimiter = getDelimiter(locale, effectiveDelimiters);
        final List<Map<Character, String>> mappings = getMappingList(locale);
        final StringBuilder sb = new StringBuilder(text.length() * 2);
        boolean delim = false;
        for (int i = 0; i < text.length(); ++i) {
            final char c = text.charAt(i);
            // check for word separator
            int pos = effectiveDelimiters.indexOf(c);
            if (pos >= 0) {
                // found word delimiter
                if (!delim) {
                    // first of them
                    delim = true;
                    sb.append(delimiter);
                } // else character is skipped (no not translate multiple spaces)
            } else {
                delim = false;  // now not between words
                final String replacement = getReplacement(mappings, c);
                if (replacement == null) {
                    // character is copied without change
                    sb.append(c);
                } else {
                    // append the replacement string (this can be multiple characters, but also an empty string)
                    sb.append(replacement);
                }
            }
        }
        final String rawOut = sb.toString();
        switch (CASE_CONVERSION) {
        case KEEP:
            return rawOut;
        case TOLOWER:
            return rawOut.toLowerCase(locale);
        case TOUPPER:
            return rawOut.toUpperCase(locale);
        default:
            return rawOut;
        }
    }
}
