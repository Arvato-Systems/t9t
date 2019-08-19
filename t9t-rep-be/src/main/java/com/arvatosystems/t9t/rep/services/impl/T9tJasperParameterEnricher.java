/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.rep.services.impl;

import java.util.Locale;
import java.util.Map;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.services.IJasperParameterEnricher;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.google.common.base.Strings;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import net.sf.jasperreports.engine.JRParameter;

@Singleton
public class T9tJasperParameterEnricher implements IJasperParameterEnricher {

    private final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);
    public static final String LANGUAGE_CODE = "languageCode";
    public static final String TRANSLATION_PROVIDER = "translator";
    public static final String TIME_ZONE = "timeZone";

    @Override
    public void enrichParameter(Map<String, Object> parameters, ReportParamsDTO reportParamsDTO,
            Map<String, Object> outputSessionAdditionalParametersList,
            OutputSessionParameters outputSessionParameters) {

        final RequestContext ctx = Jdp.getRequired(RequestContext.class);
        Locale locale = resolveReportLocale(ctx);
        parameters.put(JRParameter.REPORT_LOCALE, locale);
        parameters.put(LANGUAGE_CODE, locale.getLanguage());

        parameters.put(TRANSLATION_PROVIDER, translationProvider);

        if (reportParamsDTO != null && !Strings.isNullOrEmpty(reportParamsDTO.getTimeZone())) {
            // If timezone from reportParams exist. Use first.
            parameters.putIfAbsent(TIME_ZONE, reportParamsDTO.getTimeZone());
        }
    }

    protected Locale resolveReportLocale(RequestContext ctx) {
        Locale locale = null;
        String selectedLanguageCode = ctx.internalHeaderParameters.getLanguageCode();

        if (selectedLanguageCode != null) {
            locale = selectedLanguageCode.length() == 5
                    ? new Locale(selectedLanguageCode.substring(0, 2), selectedLanguageCode.substring(3))
                    : new Locale(selectedLanguageCode.substring(0, 2));
        } else {
            locale = Locale.US;
        }

        return locale;
    }

}
