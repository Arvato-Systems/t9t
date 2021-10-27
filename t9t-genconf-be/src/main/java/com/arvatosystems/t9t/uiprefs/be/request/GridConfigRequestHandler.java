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
package com.arvatosystems.t9t.uiprefs.be.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest;
import com.arvatosystems.t9t.uiprefs.request.GridConfigResponse;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Jdp;

public class GridConfigRequestHandler extends AbstractRequestHandler<GridConfigRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridConfigRequestHandler.class);
//  @Inject
    private final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);
    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    public static final ConcurrentMap<String, Object> UNTRANSLATED_HEADERS = new ConcurrentHashMap<>();
    public static final ConcurrentMap<String, Object> UNTRANSLATED_DEFAULTS = new ConcurrentHashMap<>();

    @Override
    public GridConfigResponse execute(final RequestContext ctx, final GridConfigRequest request) throws Exception {
        final String gridId = request.getGridId();

        // Get the LeanGridConfigResponse using GridConfigRequest
        final LeanGridConfigRequest leanGridConfigRequest = new LeanGridConfigRequest();
        leanGridConfigRequest.setGridId(request.getGridId());
        leanGridConfigRequest.setNoFallbackLanguages(request.getNoFallbackLanguages());
        leanGridConfigRequest.setOverrideLanguage(request.getOverrideLanguage());
        leanGridConfigRequest.setSelection(request.getSelection());
        final LeanGridConfigResponse leanGridConfigResponse = executor.executeSynchronousAndCheckResult(leanGridConfigRequest,
                LeanGridConfigResponse.class);

        if (leanGridConfigResponse == null) {
            LOGGER.warn("No lean grid response found, I have {}",
                    ToStringHelper.toStringML(ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.keySet()));
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST,
                    "No configuration stored for lean grid ID: " + gridId);
        }

        final UILeanGridPreferences leanPrefs = leanGridConfigResponse.getLeanGridConfig();

        if (leanPrefs == null) {
            LOGGER.warn("No lean grid configuration found, I have {}",
                    ToStringHelper.toStringML(ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.keySet()));
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST,
                    "No configuration stored for lean grid ID: " + gridId);
        }

        String lang = request.getOverrideLanguage();
        if (lang == null)
            lang = ctx.internalHeaderParameters.getJwtInfo().getLocale();

        LOGGER.debug("Retrieving grid config {} for user {}, tenant {}, language {}",
                gridId, ctx.userId, ctx.tenantId, lang);
        UIGridPreferences prefs = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(gridId);

        if (prefs == null) {
            // no entry found, not in DB and no default. Should not happen.
            LOGGER.warn("No grid configuration found, I have {}", ToStringHelper.toStringML(IGridConfigContainer.GRID_CONFIG_REGISTRY.keySet()));
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "No configuration stored for grid ID: " + gridId);
        }
        final boolean alsoInvisible = request.getTranslateInvisibleHeaders();

        // Reorder the UIGridPreferences based on UILeanGridPreferences
        prefs = reorderColumns(prefs, leanPrefs);

        // mix in the translations...
        final List<String> selectedFields = new ArrayList<>(prefs.getColumns().size());
        for (final UIColumnConfiguration c : prefs.getColumns()) {
            if (alsoInvisible || c.getVisible())
                selectedFields.add(c.getFieldName());
        }

        final GridConfigResponse response = new GridConfigResponse();
        response.setGridConfig(prefs);
        response.setHeaders(translationProvider.getHeaderTranslations(
                ctx.tenantId,
                lang,
                !request.getNoFallbackLanguages(),
                gridId,
                selectedFields));
        // additional length check here, with posible truncation - it is hard to spot too long fields in the main log
        LOGGER.debug("Headers length check for {} fields ({} xlated)", selectedFields.size(), response.getHeaders().size());
        int index = 0;
        int maxlength = 0;
        for (final String s: response.getHeaders()) {
            if (s != null) {
                if (s.length() > maxlength)
                    maxlength = s.length();
                if (s.startsWith("${")) {
                    final String pqon = s.substring(2, s.length() - 1);
                    UNTRANSLATED_HEADERS.putIfAbsent(pqon, "x");
                    final int i = pqon.lastIndexOf('.');
                    if (i < 0)
                        UNTRANSLATED_DEFAULTS.putIfAbsent(pqon, "x");
                    else
                        UNTRANSLATED_DEFAULTS.putIfAbsent(pqon.substring(i + 1), "x");
                }
                if (s.length() > 160) {
                    LOGGER.error("Translation for header {}: {} is too long: {} characters: {}",
                            index, selectedFields.get(index), s.length(), s);
                    response.getHeaders().set(index,  s.substring(0, 160));
                }
            }
            ++index;
        }
        LOGGER.debug("Headers maxlength was {}", maxlength);
        return response;
    }

    /**
     * FT-3217 - Reorder the columns and sort options based on
     * the fields and filters of UILeanGridPreferences
     *
     * @param uiGrid
     * @param uiLeanGrid
     * @return
     */
    private UIGridPreferences reorderColumns(final UIGridPreferences uiGridSource, final UILeanGridPreferences uiLeanGrid) {
        if (uiGridSource == null || uiLeanGrid == null) {
            LOGGER.warn("Unable to merge due to UIGridPreferences or UILeanGridPreferences object is null. ");
            throw new T9tException(T9tException.CL_INTERNAL_LOGIC_ERROR,
                    "No configuration passed into convert method ");
        }

        final UIGridPreferences targetUIGridPreferences = uiGridSource.ret$MutableClone(true, true);

        final Map<String, UIColumnConfiguration> uiColumnConfMap = new HashMap<>();

        /**
         * Put all the uiGridPreference Columns into a map with its fieldname as
         * key.
         */
        for (final UIColumnConfiguration colConf : targetUIGridPreferences.getColumns()) {
            uiColumnConfMap.put(colConf.getFieldName(), colConf);
        }

        final List<UIColumnConfiguration> orderedUIColConfigurations = new ArrayList<>();

        /** Reorder based on the ordering on UILeanGridPreferences **/
        // Fields -> visible = true
        for (final String uiLeanColFieldname : uiLeanGrid.getFields()) {
            final UIColumnConfiguration conf = uiColumnConfMap.get(uiLeanColFieldname);
            if (conf == null) {
                LOGGER.error("Problem in grid configuration: Cannot find column of name {} in lean grid for vm {} / {}",
                        uiLeanColFieldname, uiGridSource.getViewModel(), uiLeanGrid.getViewModel());
                throw new T9tException(T9tException.CANNOT_FIND_UI_COLUMN, uiLeanColFieldname);
            }
            conf.setVisible(true);
            orderedUIColConfigurations.add(conf);
        }

        // Filter field name does not exist in fields -> visible = false
        if (uiLeanGrid.getFilters() != null) {
            for (final UIFilter filter : uiLeanGrid.getFilters()) {
                final String uiLeanColFilterFieldname = filter.getFieldName();

                if (!uiLeanGrid.getFields().contains(uiLeanColFilterFieldname)) {
                    final UIColumnConfiguration conf = uiColumnConfMap.get(uiLeanColFilterFieldname);
                    conf.setVisible(false);
                    orderedUIColConfigurations.add(conf);
                }
            }
        }

        targetUIGridPreferences.setColumns(orderedUIColConfigurations);

        /**
         * Set the sorting options
         */
        targetUIGridPreferences.setSortColumn(uiLeanGrid.getSortColumn());

        if (uiLeanGrid.getSortDescending() != null) {
            targetUIGridPreferences.setSortDescending(uiLeanGrid.getSortDescending());
        } else {
            targetUIGridPreferences.setSortDescending(false);
        }

        targetUIGridPreferences.freeze();

        return targetUIGridPreferences;
    }
}
