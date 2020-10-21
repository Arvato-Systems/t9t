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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Pair;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;

public class EditGridViewModel {

    protected Listbox editGridListBox;
    private Window windowComponent = null;
    private UIGridPreferences uiGridPreferences = null;
    private Set<String> currentGrid = null;
    private Pair<List<String>, List<String>> addRemovePair = null;
    private final ApplicationSession session = ApplicationSession.get();

    @Wire("#editListBox")
    Listbox listbox;

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
        }

    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, addRemovePair);
        windowComponent.onClose();
    }

    @Command
    public void updateGrid() {
        List<Listitem> selectedItems = listbox.getItems();
        List<String> addPair = new ArrayList<>();
        List<String> removePair = new ArrayList<>();

        if (listbox.getSelectedCount() == 0) {
            Messagebox.show(session.translate("editGrid", "selectedFieldCountZero"));
            return;
        }

        for (Listitem listItem : selectedItems) {
            UIColumnConfiguration uiColumnConfiguration = uiGridPreferences.getColumns().get(listItem.getIndex());

            if (listItem.isSelected()) {
                if (!currentGrid.contains(uiColumnConfiguration.getFieldName())) {
                    addPair.add(uiColumnConfiguration.getFieldName());
                }
            } else {
                if (currentGrid.contains(uiColumnConfiguration.getFieldName())) {
                    removePair.add(uiColumnConfiguration.getFieldName());
                }
            }
        }

        addRemovePair = new Pair<List<String>, List<String>>(addPair, removePair);
        closeWindow();
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);

        List<String> allAvailableFieldNames = new LinkedList<>();
        uiGridPreferences.getColumns().stream().forEach(uiColumns->{allAvailableFieldNames.add(uiColumns.getFieldName());});
        listbox.setItemRenderer(new ListitemRenderer<String>() {
            @Override
            public void render(Listitem item, String data, int index) throws Exception {
                item.setValue(data);
                item.setLabel(data);
            }
        });
        ListModel<String> models = new ListModelList<>(allAvailableFieldNames,false);
        listbox.setModel(models);
        listbox.setCheckmark(true);
        listbox.setMultiple(true);
        listbox.renderAll();

        listbox.getItems().stream().forEach(item->{
            if (currentGrid.contains(item.getLabel())) {
                item.setSelected(true);
            }
        });
    }

    public UIGridPreferences getUiGridPreferences() {
        return uiGridPreferences;
    }

    public void setUiGridPreferences(UIGridPreferences uiGridPreferences) {
        this.uiGridPreferences = uiGridPreferences;
    }

    public Set<String> getCurrentGrid() {
        return currentGrid;
    }

    public void setCurrentGrid(Set<String> currentGrid) {
        this.currentGrid = currentGrid;
    }
}
