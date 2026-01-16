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
package com.arvatosystems.t9t.hs.configurate.be.core.constants;

import java.util.Map;

public final class HsProperties {

    private HsProperties() {
        // Utility class - private constructor to prevent instantiation
    }

    public static final String ANALYSER_KEYWORD_NORMALIZER = "t9t_keyword_normalizer";

    public static final String ANALYSER_FULLTEXT_STANDARD_TOKENIZER = "t9t_fulltext_standard_tokenizer";
    public static final String ANALYSER_FULLTEXT_KEYWORD_TOKENIZER = "t9t_fulltext_keyword_tokenizer";
    public static final String ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_FUZZY = "t9t_fulltext_keyword_tokenizer_fuzzy";
    public static final String ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_EMAIL = "t9t_fulltext_keyword_tokenizer_email";

    public static final boolean IS_FUZZY = true;
    public static final boolean IS_NOT_FUZZY = false;

    public static final Map<String, Boolean> ANALYSER_FUZZINESS =
            Map.of(
                ANALYSER_FULLTEXT_STANDARD_TOKENIZER, IS_FUZZY,
                ANALYSER_FULLTEXT_KEYWORD_TOKENIZER, IS_NOT_FUZZY,
                ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_FUZZY, IS_FUZZY,
                ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_EMAIL, IS_FUZZY
            );
}
