package com.arvatosystems.t9t.hs.be.elasticsearch.configurate.impl;

import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurationContext;
import org.hibernate.search.backend.elasticsearch.analysis.ElasticsearchAnalysisConfigurer;

import com.arvatosystems.t9t.hs.configurate.be.core.constants.HsProperties;

/**
 * Defines custom analyzers / normalizers for the Elasticsearch backend.
 * Mirrors the Lucene configuration so field mappings can stay backend-agnostic.
 *
 * Provides:
 *  - Analyzer ANALYSER_FULLTEXT_STANDARD_TOKENIZER: standard tokenizer + lowercase + asciifolding
 *  - Normalizer ANALYSER_KEYWORD_NORMALIZER: lowercase + asciifolding (for keyword sort fields)
 */
public class T9tElasticsearchAnalysisConfigurer implements ElasticsearchAnalysisConfigurer {
    @Override
    public void configure(ElasticsearchAnalysisConfigurationContext context) {
        context.analyzer(HsProperties.ANALYSER_FULLTEXT_STANDARD_TOKENIZER).custom()
                .tokenizer("standard")
                .tokenFilters("lowercase", "asciifolding");


        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER).custom()
                .tokenizer("keyword")
                .tokenFilters("lowercase", "asciifolding");

        context.normalizer(HsProperties.ANALYSER_KEYWORD_NORMALIZER).custom()
                .tokenFilters("lowercase", "asciifolding");
    }
}

