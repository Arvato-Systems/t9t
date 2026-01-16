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
package com.arvatosystems.t9t.hs.be.lucene.configurate.impl;

import jakarta.annotation.Nonnull;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

import com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties;

/**
 * Defines custom analyzers / normalizers for Lucene backend.
 * Provides:
 *  - Analyzer ANALYSER_FULLTEXT_STANDARD_TOKENIZER: standard tokenizer + lowercase + asciifolding
 *  - Normalizer ANALYSER_KEYWORD_NORMALIZER: lowercase + asciifolding for keyword fields
 */
public class T9tLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {
    @Override
    public void configure(@Nonnull final LuceneAnalysisConfigurationContext context) {
        context.analyzer(HsProperties.ANALYSER_FULLTEXT_STANDARD_TOKENIZER).custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER).custom()
                .tokenizer("keyword")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_FUZZY).custom()
                .tokenizer("keyword")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_EMAIL).custom()
                .tokenizer("keyword")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding")
                .tokenFilter("patternReplace")
                    .param("pattern", "@.*")
                    .param("replacement", "")
                    .param("replace", "first")
                .tokenFilter("wordDelimiterGraph")
                    .param("generateWordParts", "1")
                    .param("generateNumberParts", "0")
                    .param("splitOnCaseChange", "0")
                    .param("splitOnNumerics", "1")
                    .param("preserveOriginal", "1")
                    .param("catenateAll", "1")
                    .param("catenateWords", "1")
                    .param("catenateNumbers", "0");

        context.normalizer(HsProperties.ANALYSER_KEYWORD_NORMALIZER).custom()
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");
    }
}
