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
package com.arvatosystems.t9t.schemaLoader.util;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Collections.emptyList;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * @author TWEL006
 */
public class SimpleSqlParser {

    private static final Pattern PATTERN_COMMENT_SINGLE_LINE = Pattern.compile("--.*?$",
                                                                               Pattern.MULTILINE);
    private static final Pattern PATTERN_COMMENT_MULTI_LINE  = Pattern.compile("/\\*.*?\\*/",
                                                                               Pattern.DOTALL);


    public static List<String> getStatements(String script) {

        if (isEmpty(script)) {
            return emptyList();
        }

        // first remove all comments
        script = PATTERN_COMMENT_MULTI_LINE.matcher(script)
                                           .replaceAll("");
        script = PATTERN_COMMENT_SINGLE_LINE.matcher(script)
                                            .replaceAll("");

        final List<String> statements        = new LinkedList<>();
        StringBuilder      buffer            = new StringBuilder();
        Character          inLiteralWithChar = null;

        for (int iChar = 0; iChar < script.length(); iChar++) {
            final char character = script.charAt(iChar);

            buffer.append(character);

            switch (character) {
                case '\"':
                case '\'': {

                    if (inLiteralWithChar == null) {
                        // was not in a string, but is now
                        inLiteralWithChar = character;
                    } else if (inLiteralWithChar == character) {
                        // To cover escaping we need a lookbehind
                        // Only if char before is not \, this is a valid delimiter to end
                        if (iChar == 0 || script.charAt(iChar - 1) != '\\') {
                            // was in a string with the current char as boundary, thus no longer in string
                            inLiteralWithChar = null;
                        }
                    }

                    break;
                }

                case ';': {

                    if (inLiteralWithChar == null) {
                        // Not within literal, thus is delimiter
                        if (buffer.length() > 0) {
                            statements.add(buffer.toString());
                            buffer = new StringBuilder();
                        }
                    }

                    break;
                }
            }
        }

        if (buffer.length() > 0) {
            statements.add(buffer.toString());
        }

        return statements;
    }


}
