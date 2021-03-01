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
package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.util.Pair;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Fallback;

/**
 * CE version of column configuration component that list out all the fields available on a specific grid id
 */
@Dependent
@Fallback
public class DefaultColumnConfigCreator implements IColumnConfigCreator {

    private UIGridPreferences uiGridPreferences = null;
    private Set<String>       currentGrid = null;
    private Listbox           listbox = null;
    protected String          viewModelId = null;

    @Override
    public void createColumnConfigComponent(ApplicationSession session, Div parent, UIGridPreferences uiGridPreferences, Set<String> currentGrid) {
        parent.setVflex("1");
        this.currentGrid = currentGrid;
        viewModelId = uiGridPreferences.getViewModel();
        listbox = new Listbox();
        List<String> allAvailableFieldNames = new LinkedList<>();
        uiGridPreferences.getColumns().stream().forEach(uiColumns -> {
            allAvailableFieldNames.add(uiColumns.getFieldName());
        });
        Listhead head = new Listhead();
        Listheader checkboxHeader = new Listheader();
        checkboxHeader.setWidth("40px");
        checkboxHeader.setParent(head);
        Listheader fieldHeader= new Listheader();
        fieldHeader.setParent(head);
        head.setParent(listbox);
        listbox.setItemRenderer(new ListitemRenderer<String>() {
            @Override
            public void render(Listitem item, String data, int index) throws Exception {
                Div wrapper = new Div();
                Listcell cell1 = new Listcell();
                cell1.setParent(item);
                Listcell cell2 = new Listcell();
                cell2.setParent(item);
                wrapper.setParent(cell2);
                Popup tooltip = new Popup();
                Label toolTipLabel = new Label(data);
                toolTipLabel.setParent(tooltip);
                tooltip.setParent(wrapper);
                Label label = new Label(session.translate(viewModelId,  data));
                label.setParent(wrapper);
                label.setTooltip(tooltip);
                item.setTooltip(tooltip);
                item.setValue(data);
            }
        });
        ListModel<String> models = new ListModelList<>(allAvailableFieldNames, false);
        listbox.setModel(models);
        listbox.setCheckmark(true);
        listbox.setMultiple(true);
        listbox.setParent(parent);
        listbox.renderAll();
        listbox.setSclass("inline-listbox");
        listbox.setHflex("1");
        listbox.setVflex("1");
        listbox.setSpan(true);
        listbox.setEmptyMessage(session.translate("com", "noDataFound"));

        listbox.getItems().stream().forEach(item -> {
            if (currentGrid.contains(item.getValue())) {
                item.setSelected(true);
            }
        });
    }

    @Override
    public Pair<List<String>, List<String>> getAddRemovePairs(ApplicationSession session) {
        List<Listitem> selectedItems = listbox.getItems();
        List<String> addPair = new ArrayList<>();
        List<String> removePair = new ArrayList<>();

        if (listbox.getSelectedCount() == 0) {
            Messagebox.show(session.translate("editGrid", "selectedFieldCountZero"));
            return null;
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

        return new Pair<List<String>, List<String>>(addPair, removePair);
    }
}
