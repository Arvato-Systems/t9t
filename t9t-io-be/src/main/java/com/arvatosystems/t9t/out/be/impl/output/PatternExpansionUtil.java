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
package com.arvatosystems.t9t.out.be.impl.output;

import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDateTime;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.SimplePatternEvaluator;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;

public class PatternExpansionUtil {
    static final String CLEAR_DATE_PATTERN = "\\w";
    static final String YEAR_PATTERN = "yyyy";
    static final String MONTH_PATTERN = "MM";
    static final String DAY_PATTERN = "dd";
    static final String DATE_PATTERN = "yyyy-MM-dd";

    private static String formatDate(LocalDateTime dt, String datePattern) {
        return dt == null ? datePattern.replaceAll(CLEAR_DATE_PATTERN, "0") : dt.toString(datePattern);
    }

    private static Map<String, Object> buildOutputPatternReplacementsFromParameters(
            RequestContext ctx,
            OutputSessionParameters params,
            MediaTypeDescriptor communicationFormatType,
            Map<String, String> additionalParams) {
        final Map<String, Object> patternReplacements = new HashMap<>(31);

        // default parameters, always supplied
        if (ctx != null) {
            patternReplacements.put("tenantId", ctx.tenantId);
            patternReplacements.put("userId",   ctx.userId);
            patternReplacements.put("language", ctx.internalHeaderParameters.getLanguageCode());
            patternReplacements.put("planDate", ctx.internalHeaderParameters.getPlannedRunDate());
            patternReplacements.put("now",      ctx.executionStart);
        }
        // we have 4 timestamps: now, ctx.executionStart, ctx.internalHeaderParameters.plannedRunDate and params.asOf
        final LocalDateTime now = LocalDateTime.now();
        patternReplacements.put("today",    formatDate(now, DATE_PATTERN));
        patternReplacements.put("year",     formatDate(now, YEAR_PATTERN));
        patternReplacements.put("month",    formatDate(now, MONTH_PATTERN));
        patternReplacements.put("day",      formatDate(now, DAY_PATTERN));

        if (communicationFormatType != null) {
            patternReplacements.put("fileExt", communicationFormatType.getDefaultFileExtension());
        }

        if (params != null) {
            patternReplacements.put("asOf", params.getAsOf());
            patternReplacements.put("ref1", params.getGenericRefs1());
            patternReplacements.put("ref2", params.getGenericRefs2());
            patternReplacements.put("id1", params.getGenericId1());
            patternReplacements.put("id2", params.getGenericId2());

            // customs, if available
            if (params.getAdditionalParameters() != null) {
                patternReplacements.putAll(params.getAdditionalParameters());
            }
        }

        if (additionalParams != null) {
            patternReplacements.putAll(additionalParams);
        }

        return patternReplacements;
    }

    public static String expandFileOrQueueName(RequestContext ctx, String pattern, OutputSessionParameters params,
            MediaTypeDescriptor communicationFormatType, Map<String, String> additionalParams) {
        return SimplePatternEvaluator.evaluate(pattern, buildOutputPatternReplacementsFromParameters(ctx, params, communicationFormatType, additionalParams));
    }
}
