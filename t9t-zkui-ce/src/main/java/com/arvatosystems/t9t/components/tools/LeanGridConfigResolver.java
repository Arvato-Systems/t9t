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
package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.arvatosystems.t9t.base.BooleanUtil;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.ILeanGridConfigContainer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.components.GridIdTools;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO;
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigKey;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigCrudRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.BasicNumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIDefaults;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.dp.Jdp;

public class LeanGridConfigResolver implements ILeanGridConfigResolver {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeanGridConfigResolver.class);
    protected static final List<UIFilter> NO_FILTERS = ImmutableList.<UIFilter>of();
    protected final T9TRemoteUtils remoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    private final boolean productionMode = true;
    private final ApplicationSession as;
    private final String myGridId;
    private final CrudViewModel<BonaPortable, TrackingBase> myCrudViewModel;
    private final Map<String, FieldDefinition> pathToFieldDef = new ConcurrentHashMap<String, FieldDefinition>(40);
    private final List<FieldDefinition> allFieldDefs = new ArrayList<FieldDefinition>(20);
    private List<String> translations;
    private List<Integer> widths;

    public static final UIDefaults UI_WIDTH_DEFAULTS = new UIDefaults(5, 32, 40, 80, 120, 10, 12, 200);

    private UILeanGridPreferences gridPrefs;   // mutable shallow copy of the config: on changes, new lists will be created
    private boolean gridPrefsModified = false;
    private int variant = 0;

    private final void loadFromRemote() {
        LeanGridConfigRequest readCmd = new LeanGridConfigRequest();
        readCmd.setGridId(myGridId);
        readCmd.setSelection(variant);
        LeanGridConfigResponse resp = remoteUtils.executeExpectOk(readCmd, LeanGridConfigResponse.class);
        gridPrefs = resp.getLeanGridConfig();
//        LeanGridConfigCrudRequest readCmd = createCrud(OperationType.READ, false);
//        LeanGridConfigDTO n = (LeanGridConfigDTO)(remoteUtils.executeExpectOk(readCmd, CrudSurrogateKeyResponse.class).getData());
//        LOGGER.debug("read {} config for grid ID {}", n.getUserRef().longValue() == 0L ? "TENANT" : "USER", myGridId);
//        gridPrefs = n.getGridPrefs();
    }

    private final void loadConfig() {
        LOGGER.debug("Loading grid config for {}:{}, ZulUtils locale = {}, session = {}",
                myGridId, variant, ZulUtils.getDefaultLanguageCode(), as.getJwtInfo().getLocale());
        gridPrefs = ILeanGridConfigContainer.LEAN_GRID_CONFIG_REGISTRY.get(myGridId);
        if (gridPrefs == null) {
            throw new RuntimeException("No lean grid config for gridId " + myGridId);
        }
        gridPrefs = gridPrefs.ret$MutableClone(true, true);
        if (variant != 0 || productionMode)  // if not production: use local settings to avoid need to deploy server too frequently
            loadFromRemote();

        allFieldDefs.clear();
        // currently we use hardcoded prefs, later we check with the backend if they have been reconfigured
        for (String fieldname : gridPrefs.getFields()) {
            try {
                FieldDefinition fd = FieldMappers.getFieldDefinitionForPath(fieldname, myCrudViewModel);
                if (fd == null)
                    LOGGER.error("Unresolvable field name in {}: {} for DTO {}", myGridId, fieldname, myCrudViewModel.dtoClass.getBonaPortableClass().getSimpleName());
                pathToFieldDef.put(fieldname, fd);
                allFieldDefs.add(fd);
            } catch (Exception e) {
                LOGGER.error("Unresolvable field name in {}: {} for DTO {}", myGridId, fieldname, myCrudViewModel.dtoClass.getBonaPortableClass().getSimpleName());
                // more diagnostics...
                LOGGER.error("Field name is {} of length {}", fieldname, fieldname.length());
                ClassDefinition meta2 = myCrudViewModel.dtoClass.getMetaData();
                int num = meta2.getFields().size() > 10 ? 10 : meta2.getFields().size();
                for (int i = 0; i < num; ++i)
                    LOGGER.info("Field {} is {}", i, meta2.getFields().get(i).getName());
            }
        }
        widths = gridPrefs.getFieldWidths() == null ? new ArrayList<Integer>(gridPrefs.getFields().size()) : gridPrefs.getFieldWidths();
        if (gridPrefs.getFieldWidths() == null) {
            for (FieldDefinition fd: allFieldDefs)
                widths.add(defaultWidth(fd));
        }
        translations = new ArrayList<String>(gridPrefs.getFields().size());
        for (String fieldname : gridPrefs.getFields())
            translations.add(as.translate(myGridId, fieldname));
        gridPrefsModified = false;
    }

    public LeanGridConfigResolver(String gridId, ApplicationSession session) {
        this(gridId, session, 0);
    }

    public LeanGridConfigResolver(String gridId, ApplicationSession session, int variant) {
        as = session;
        myGridId = gridId;
        myCrudViewModel = GridIdTools.getViewModelByGridId(gridId);
        this.variant = variant;

        loadConfig();
    }

    @Override
    public List<UIFilter> getFilters() {
        return gridPrefs.getFilters() == null ? NO_FILTERS : gridPrefs.getFilters();
    }

    @Override
    public FieldDefinition getFieldDefinitionForPath(String fieldname) {
        return pathToFieldDef.get(fieldname);
    }

    @Override
    public List<FieldDefinition> getVisibleColumns() {
        return allFieldDefs;
    }

    @Override
    public List<String> getHeaders() {
        return translations;
    }

    @Override
    public UILeanGridPreferences getGridPreferences() {
        return gridPrefs;
    }

    @Override
    public List<Integer> getWidths() {
        return widths;
    }


    @Override
    public void newSort(String fieldname, boolean isDescending) {
        gridPrefsModified = true;
        gridPrefs.setSortColumn(fieldname);
        gridPrefs.setSortDescending(fieldname == null ? null : isDescending);  // never store sort direction when field name is null
    }


    protected int width(int chars) {
        int calculatedSize = UI_WIDTH_DEFAULTS.getWidthOffset() + chars * UI_WIDTH_DEFAULTS.getWidthPerCharacter();
        return calculatedSize > UI_WIDTH_DEFAULTS.getWidthMax() ? UI_WIDTH_DEFAULTS.getWidthMax() : calculatedSize;

    }

    @Override
    public int defaultWidth(FieldDefinition f) {
        switch (f.getDataCategory()) {
        case NUMERIC:
        case BASICNUMERIC:
            BasicNumericElementaryDataItem b = (BasicNumericElementaryDataItem)f;
            return width(b.getTotalDigits() + (b.getIsSigned() ? 1 : 0) + (b.getDecimalDigits() > 0 ? 1 : 0));
        case BINARY:
            break;
        case ENUM:
        case ENUMALPHA:
        case XENUM:
            return UI_WIDTH_DEFAULTS.getWidthEnum();
        case ENUMSET:
        case ENUMSETALPHA:
        case XENUMSET:
            return UI_WIDTH_DEFAULTS.getWidthEnumset();
        case MISC:
            switch (f.getBonaparteType().toLowerCase()) {
            case "uuid":
                return width(36);
            case "boolean":
                return UI_WIDTH_DEFAULTS.getWidthCheckbox();
            }
            break;
        case OBJECT:
            return UI_WIDTH_DEFAULTS.getWidthObject();
        case STRING:
            AlphanumericElementaryDataItem a = (AlphanumericElementaryDataItem)f;
            return width(a.getLength());
        case TEMPORAL:
            //TemporalElementaryDataItem t = (TemporalElementaryDataItem)f;
            //int fractionalSecondsSize = t.getFractionalSeconds() > 0 ? 4 : 0;
            switch (f.getBonaparteType().toLowerCase()) {
            case "day":
                return 90; // width(10);
            case "time":
                return 80; // width(8 + fractionalSecondsSize);
            default:
                return 150; // width(19 + fractionalSecondsSize);
            }
        default:
            break;
        }
        return 80;
    }

    private LeanGridConfigCrudRequest createCrud(OperationType forWhat, boolean tenantDefault) {
        LeanGridConfigCrudRequest crud = new LeanGridConfigCrudRequest();
        crud.setCrud(forWhat);
        LeanGridConfigKey k = new LeanGridConfigKey();
        k.setGridId(myGridId);
        k.setVariant(variant);
        k.setUserRef(tenantDefault ? 0L : as.getUserRef());
        crud.setNaturalKey(k);
        return crud;
    }

    @Override
    public void save(boolean asTenantDefault) {
        LeanGridConfigCrudRequest crud = createCrud(OperationType.MERGE, asTenantDefault);
        LeanGridConfigDTO n = new LeanGridConfigDTO();
        n.setGridId(myGridId);
        n.setVariant(variant);
        n.setUserRef(asTenantDefault ? 0L : as.getUserRef());
        n.setIsActive(true);
        n.setGridPrefs(gridPrefs);

        crud.setData(n);
        remoteUtils.executeExpectOk(crud, CrudSurrogateKeyResponse.class);
    }

    @Override
    public void deleteConfig(boolean tenantDefault) {
        LeanGridConfigCrudRequest crud = createCrud(OperationType.DELETE, tenantDefault);
        remoteUtils.executeIgnoreErr(crud, T9tException.RECORD_DOES_NOT_EXIST);
    }

    @Override
    public boolean reload() {
        boolean wasDescending = BooleanUtil.isTrue(gridPrefs.getSortDescending());
        String oldSortColumn = gridPrefs.getSortColumn();
        loadConfig();
        boolean isNowDescending = BooleanUtil.isTrue(gridPrefs.getSortDescending());
        return isNowDescending != wasDescending || !Objects.equal(oldSortColumn, gridPrefs.getSortColumn());
    }

    @Override
    public int getVariant() {
        return variant;
    }

    @Override
    public void setVariant(int variant) {
        LOGGER.info("Setting variant of grid {} to {}", myGridId, variant);
        this.variant = variant;
        gridPrefsModified = true;   // could differ from default
    }

    protected void sanityCheck(int columnIndex) {
        if (columnIndex < 0 || columnIndex >= widths.size()) {
            LOGGER.error("Column index {} is beyond grid size {}!", columnIndex, widths.size());
        }
    }

    @Override
    public void changeWidth(int columnIndex, int newWidth, int oldWidth) {
        sanityCheck(columnIndex);

        // if the new width is 0 then load with the default width
        if (  (newWidth == 0 || newWidth == -1) && oldWidth == -1) {
            newWidth = defaultWidth(allFieldDefs.get(columnIndex));
            LOGGER.debug("width is set to 0 or -1. Overwriting with default width {}", newWidth);
        }

        widths.set(columnIndex, newWidth);
        gridPrefs.setFieldWidths(widths);   // widths are no longer default...
        gridPrefsModified = true;

        // on change of first column, log all widths, for inclusion into defaults...
        if (columnIndex == 0) {
            LOGGER.debug("\"fieldWidths\": [ {} ]",
                widths.stream().map(i -> Integer.toString(i)).collect(Collectors.joining(", "))
            );
        }
    }

    @Override
    public void changeColumnOrder(int from, int to) {
        sanityCheck(from);
        sanityCheck(to);
        List<String> fields = gridPrefs.getFields();
        if (from < to) {
            Collections.rotate(fields.subList(from, to + 1), -1);
            Collections.rotate(widths.subList(from, to + 1), -1);
            Collections.rotate(translations.subList(from, to + 1), -1);
            Collections.rotate(allFieldDefs.subList(from, to + 1), -1);
        } else {
            Collections.rotate(fields.subList(to, from + 1), 1);
            Collections.rotate(widths.subList(to, from + 1), 1);
            Collections.rotate(translations.subList(to, from + 1), 1);
            Collections.rotate(allFieldDefs.subList(to, from + 1), 1);
        }
        gridPrefsModified = true;
    }

    public boolean isGridPrefsModified() {
        return gridPrefsModified;
    }

    @Override
    public void addField(String fieldname) {
        gridPrefsModified = true;
        gridPrefs.setFieldWidths(widths);  // is now part of the config
        gridPrefs.getFields().add(fieldname);

        FieldDefinition fd = FieldMappers.getFieldDefinitionForPath(fieldname, myCrudViewModel);
        if (fd == null)
            LOGGER.error("Unresolvable field name in {}: {} for DTO {}", fieldname, myGridId, myCrudViewModel.dtoClass.getBonaPortableClass().getSimpleName());
        pathToFieldDef.put(fieldname, fd);
        allFieldDefs.add(fd);
        widths.add(defaultWidth(fd));
        translations.add(as.translate(myGridId, fieldname));
    }

    @Override
    public void deleteField(int index) {
        sanityCheck(index);
        gridPrefsModified = true;
        gridPrefs.getFields().remove(index);
        allFieldDefs.remove(index);
        widths.remove(index);
        translations.remove(index);
    }

    @Override
    public void setVisibility(int index, boolean isVisible) {
        sanityCheck(index);
        gridPrefsModified = true;
        widths.set(index, isVisible ? defaultWidth(allFieldDefs.get(index)) : 0);
        if (!isVisible)
            gridPrefs.setFieldWidths(widths);   // widths are no longer default...
    }

    @Override
    public void setFilters(List<UIFilter> filters) {
        gridPrefsModified = true;
        gridPrefs.setFilters(filters);

    }
}
