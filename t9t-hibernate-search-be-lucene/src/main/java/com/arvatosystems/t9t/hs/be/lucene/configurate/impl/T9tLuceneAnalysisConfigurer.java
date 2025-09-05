package com.arvatosystems.t9t.hs.be.lucene.configurate.impl;

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
    public void configure(LuceneAnalysisConfigurationContext context) {
        context.analyzer(HsProperties.ANALYSER_FULLTEXT_STANDARD_TOKENIZER).custom()
                .tokenizer("standard")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");

        context.analyzer(HsProperties.ANALYSER_FULLTEXT_KEYWORD_TOKENIZER).custom()
                .tokenizer("keyword")
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");

        context.normalizer(HsProperties.ANALYSER_KEYWORD_NORMALIZER).custom()
                .tokenFilter("lowercase")
                .tokenFilter("asciifolding");
    }
}

