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
package com.arvatosystems.t9t.hs.be.elasticsearch.configurate.impl;

import jakarta.annotation.Nonnull;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

import com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties;

/**
 * Defines custom analyzers / normalizers for the Elasticsearch backend.
 * Mirrors the Lucene configuration so field mappings can stay backend-agnostic.
 */
public class T9tElasticsearchAnalysisConfigurer implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(@Nonnull final ElasticsearchAnalysisConfigurationContext context) {

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_STANDARD_TOKENIZER).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER).custom()
                .tokenizer("keyword")
                .tokenFilters("lowercase", "asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_FUZZY).custom()
                .tokenizer("keyword")
                .tokenFilters("lowercase", "asciifolding");

        context.tokenFilter("pattern_replace_email").type("pattern_replace")
                .param("pattern", "@.*")
                .param("replacement", "")
                // Lucene's replace="first" corresponds to Elasticsearch's all=false
                .param("all", "false");

        context.tokenFilter("word_delimiter_graph_email").type("word_delimiter_graph")
                .param("generate_word_parts", "true")
                .param("generate_number_parts", "false")
                .param("split_on_case_change", "false")
                .param("split_on_numerics", "true")
                .param("preserve_original", "true")
                .param("catenate_all", "true")
                .param("catenate_words", "true")
                .param("catenate_numbers", "false");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER_EMAIL).custom()
                .tokenizer("keyword")
                .tokenFilters(
                        "lowercase",
                        "asciifolding",
                        "pattern_replace_email",
                        "word_delimiter_graph_email"
                );

        context.normalizer(HsProperties.ANALYSER_KEYWORD_NORMALIZER).custom()
                .tokenFilters("lowercase", "asciifolding");
    }
}
