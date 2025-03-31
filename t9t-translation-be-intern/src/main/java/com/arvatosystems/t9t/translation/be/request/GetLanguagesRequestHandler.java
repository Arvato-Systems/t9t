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
package com.arvatosystems.t9t.translation.be.request;

import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.translation.request.GetLanguagesRequest;
import com.arvatosystems.t9t.translation.request.GetLanguagesResponse;
import com.arvatosystems.t9t.translation.request.UILanguageDTO;
import java.util.ArrayList;
import java.util.List;

public class GetLanguagesRequestHandler extends AbstractReadOnlyRequestHandler<GetLanguagesRequest> {
    private static final List<UILanguageDTO> LANGUAGES;

    static {
        LANGUAGES = new ArrayList<>(13);
        LANGUAGES.add(new UILanguageDTO("en",    "English",     "English"));
        LANGUAGES.add(new UILanguageDTO("de",    "German",      "Deutsch"));
        LANGUAGES.add(new UILanguageDTO("fr",    "French",      "Français"));
        LANGUAGES.add(new UILanguageDTO("es",    "Spanish",     "Español"));
        LANGUAGES.add(new UILanguageDTO("it",    "Italian",     "Italiano"));
        LANGUAGES.add(new UILanguageDTO("pt",    "Portuguese",  "Português"));
        LANGUAGES.add(new UILanguageDTO("pl",    "Polish",      "Polskie"));
        LANGUAGES.add(new UILanguageDTO("cs",    "Czech",       "České"));
        LANGUAGES.add(new UILanguageDTO("tr",    "Turkish",     "Türkçe"));
        LANGUAGES.add(new UILanguageDTO("ar",    "Arabic",      "العربية"));
        LANGUAGES.add(new UILanguageDTO("zh_CN", "Chinese simplified",  "中国"));
        LANGUAGES.add(new UILanguageDTO("zh_TW", "Chinese traditional", "中國"));
    }

    @Override
    public GetLanguagesResponse execute(final RequestContext ctx, final GetLanguagesRequest request) throws Exception {
        final GetLanguagesResponse response = new GetLanguagesResponse();
        response.setLanguages(LANGUAGES);
        return response;
    }
}
