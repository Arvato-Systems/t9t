package com.arvatosystems.t9t.hs.configurate.be.core.constants;

import java.util.Map;

public final class HsProperties {

    private HsProperties() {
        // Utility class - private constructor to prevent instantiation
    }

    public static final String ANALYSER_KEYWORD_NORMALIZER = "t9t_keyword_normalizer";

    public static final String ANALYSER_FULLTEXT_STANDARD_TOKENIZER = "t9t_fulltext_standard_tokenizer";
    public static final String ANALYSER_FULLTEXT_KEYWORD_TOKENIZER = "t9t_fulltext_keyword_tokenizer";

    public static final boolean IS_FUZZY = true;
    public static final boolean IS_NOT_FUZZY = false;

    public static final Map<String, Boolean> ANALYSER_FUZZINESS =
            Map.of(ANALYSER_FULLTEXT_STANDARD_TOKENIZER, IS_FUZZY, ANALYSER_FULLTEXT_KEYWORD_TOKENIZER, IS_NOT_FUZZY);
}
