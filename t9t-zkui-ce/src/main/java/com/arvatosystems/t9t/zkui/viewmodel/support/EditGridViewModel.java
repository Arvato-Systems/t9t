/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.services.IColumnConfigCreator;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.GridConfigUtil;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.meta.ClassDefinition;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.ReflectionsPackageCache;

public class EditGridViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(EditGridViewModel.class);
    private static final Set<String> TRACKING_FIELDS = GridConfigUtil.getTrackingFieldNames();

    protected Listbox editGridListBox;
    private Window windowComponent = null;
    private UIGridPreferences uiGridPreferences = null;
    private Set<String> currentGrid = null;
    private final IColumnConfigCreator columnConfigCreator = Jdp.getRequired(IColumnConfigCreator.class);
    private final Set<String> trackingColumns = new HashSet<>();
    private final Set<String> topLevelDataColumns = new HashSet<>();
    private final Set<String> childDataColumns = new HashSet<>();

    @Wire("#component")
    private Div div;

    @SuppressWarnings("unchecked")
    @Init(superclass = true)
    public void init(@BindingParam("initParams") HashMap<String, Object> initParams,
            @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();
        if (initParams != null && initParams.get("gridId") != null) {
            uiGridPreferences = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(initParams.get("gridId"));
            if (initParams.get("currentGridList") != null) {
                currentGrid = ((List<String>) initParams.get("currentGridList")).stream().collect(Collectors.toSet());
            }
            for (final String fieldName: getAvailableFieldNames()) {
                if (TRACKING_FIELDS.contains(fieldName)) {
                    trackingColumns.add(fieldName);
                } else if (fieldName.indexOf(".") == -1) {
                    topLevelDataColumns.add(fieldName);
                } else {
                    childDataColumns.add(fieldName);
                }
            }
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        columnConfigCreator.createColumnConfigComponent(ApplicationSession.get(), div, uiGridPreferences, currentGrid);
    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    @Command
    public void updateGrid() {
        Pair<List<String>, List<String>> pairs = columnConfigCreator.getAddRemovePairs(ApplicationSession.get());
        // do not allow user to close the dialog if nothing is selected
        if (pairs != null) {
            Events.sendEvent("onClose", windowComponent, pairs);
            windowComponent.onClose();
        }
    }

    @Command
    public void addTrackingColumns() {
        columnConfigCreator.selectColumns(trackingColumns);
    }

    @Command
    public void addDataColumns() {
        columnConfigCreator.selectColumns(topLevelDataColumns);
    }

    @Command
    public void removeTrackingColumns() {
        columnConfigCreator.unselectColumns(trackingColumns);
    }

    @Command
    public void removeDataColumns() {
        columnConfigCreator.unselectColumns(topLevelDataColumns);
        columnConfigCreator.unselectColumns(childDataColumns);
    }

    private static Set<String> getTrackingFields() {
        final Set<String> trackingFields = new HashSet<String>();
        final Reflections[] reflections = ReflectionsPackageCache.getAll(MessagingUtil.BONAPARTE_PACKAGE_PREFIX, MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX);
        for (final Reflections reflection : reflections) {
            for (final Class<? extends TrackingBase> cls : reflection.getSubTypesOf(TrackingBase.class)) {
                try {
                    final Method method = cls.getDeclaredMethod("class$MetaData");
                    final ClassDefinition classDefinition = (ClassDefinition) method.invoke(null);
                    for (final FieldDefinition fieldDefinition : classDefinition.getFields()) {
                        trackingFields.add(fieldDefinition.getName());
                    }
                } catch (Exception ex) {
                    LOGGER.error("Error occured while getting tracking field", ex);
                    throw new T9tException(T9tException.GENERAL_SERVER_ERROR, ex.getMessage());
                }
            }
        }
        return trackingFields;
    }

    private Set<String> getAvailableFieldNames() {
        final Set<String> fieldNames = new HashSet<>();
        uiGridPreferences.getColumns().stream().forEach(uiColumn -> {
            if (uiColumn.getMeta() == null || uiColumn.getMeta().getDataType() == null || !uiColumn.getMeta().getDataType().equalsIgnoreCase("ref")) {
                // skip refs
                fieldNames.add(uiColumn.getFieldName());
            }
        });
        if (uiGridPreferences.getMapColumns() != null) {
            uiGridPreferences.getMapColumns().stream().forEach(mapColumn -> {
                fieldNames.add(mapColumn);
            });
        }
        return fieldNames;
    }
}
