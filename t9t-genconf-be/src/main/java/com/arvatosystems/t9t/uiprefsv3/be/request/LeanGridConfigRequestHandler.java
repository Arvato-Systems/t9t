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
package com.arvatosystems.t9t.uiprefsv3.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.arvatosystems.t9t.uiprefs.be.request.GridConfigRequestHandler;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse;
import com.arvatosystems.t9t.uiprefsv3.services.ILeanGridConfigRead;

import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;

public class LeanGridConfigRequestHandler extends AbstractRequestHandler<LeanGridConfigRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeanGridConfigRequestHandler.class);
//  @Inject
    private final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);

    @Override
    public LeanGridConfigResponse execute(RequestContext ctx, LeanGridConfigRequest request) throws Exception {
        //@Inject
        final ILeanGridConfigRead gridConfigReader = Jdp.getRequired(ILeanGridConfigRead.class);
        final String gridId = request.getGridId();
        final LeanGridConfigDTO myConfig = gridConfigReader.readLeanGridConfig(gridId, request.getSelection(), ctx.getUserRef());
        UILeanGridPreferences prefs = null;

        String lang = request.getOverrideLanguage();
        if (lang == null)
            lang = ctx.internalHeaderParameters.getJwtInfo().getLocale();

        LOGGER.debug("Retrieving lean grid config {} for user {}, tenant {}, language {}",
                gridId, ctx.userId, ctx.tenantId, lang);
        if (myConfig != null) {
            prefs = myConfig.getGridPrefs();
        }
        if (prefs == null) {
            // use registered build-in defaults, if available
            LOGGER.trace("No config found for lean gridId {} in the DB, using default", gridId);
            prefs = ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.get(gridId);
        }
        if (prefs == null) {
            // no entry found, not in DB and no default. Should not happen.
            LOGGER.warn("No lean grid configuration found, I have {}", ToStringHelper.toStringML(ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.keySet()));
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "No configuration stored for lean grid ID: " + gridId);
        }

        LeanGridConfigResponse response = new LeanGridConfigResponse();
        response.setLeanGridConfig(prefs);
        response.setHeaders(translationProvider.getHeaderTranslations(
                ctx.tenantId,
                lang,
                !request.getNoFallbackLanguages(),
                gridId,
                prefs.getFields()));
        // additional length check here, with possible truncation - it is hard to spot too long fields in the main log
        LOGGER.debug("Headers length check for {} fields ({} xlated)", prefs.getFields().size(), response.getHeaders().size());
        int index = 0;
        int maxlength = 0;
        for (String s: response.getHeaders()) {
            if (s != null) {
                if (s.length() > maxlength)
                    maxlength = s.length();
                if (s.startsWith("${")) {
                    String pqon = s.substring(2, s.length()-1);
                    GridConfigRequestHandler.UNTRANSLATED_HEADERS.putIfAbsent(pqon, "x");
                    int i = pqon.lastIndexOf('.');
                    if (i < 0)
                        GridConfigRequestHandler.UNTRANSLATED_DEFAULTS.putIfAbsent(pqon, "x");
                    else
                        GridConfigRequestHandler.UNTRANSLATED_DEFAULTS.putIfAbsent(pqon.substring(i+1), "x");
                }
                if (s.length() > 160) {
                    LOGGER.error("Translation for header {}: {} is too long: {} characters: {}",
                            index, prefs.getFields().get(index), s.length(), s);
                    response.getHeaders().set(index,  s.substring(0, 160));
                }
            }
            ++index;
        }
        LOGGER.debug("Headers maxlength was {}", maxlength);
        return response;
    }
}
