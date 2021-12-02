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
package com.arvatosystems.t9t.zkui.ee.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Detail;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Row;
import org.zkoss.zul.RowRenderer;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.services.impl.DefaultColumnConfigCreator;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Specializes;

/**
 * EE version of column configuration component that turn all available fields
 * of a specific grid id into nested grouped list.
 */
@Dependent
@Specializes
public class ColumnConfigCreatorEE extends DefaultColumnConfigCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ColumnConfigCreatorEE.class);

    protected final Map<String, List<FieldNameModel>> columnsByKey = new HashMap<>();

    private Set<String> currentGrid = null;
    private Set<String> selections = null;

    @Override
    public void createColumnConfigComponent(final ApplicationSession session, final Div parent, final UIGridPreferences uiGridPreferences,
      final Set<String> xcurrentGrid) {

        final List<FieldNameModel> firstLevelColumns = new ArrayList<>();

        this.currentGrid = xcurrentGrid;
        selections = new HashSet<>(currentGrid);
        viewModelId = uiGridPreferences.getViewModel();

        initUiGrids(uiGridPreferences, firstLevelColumns);
        final Grid grid = new Grid();
        parent.setVflex("1");
        grid.setVflex("1");
        grid.setParent(parent);
        grid.setSclass("nestedGrid grid no-padding");
        createGrid(session, grid, firstLevelColumns);
    }

    @Override
    public Pair<List<String>, List<String>> getAddRemovePairs(final ApplicationSession session) {

        if (selections.isEmpty()) {
            Messagebox.show(session.translate("editGrid", "selectedFieldCountZero"));
            return null;
        }

        final List<String> addPair = new ArrayList<>();
        final List<String> removePair = new ArrayList<>();
        addIfAbsent(currentGrid, selections, removePair);
        addIfAbsent(selections, currentGrid, addPair);
        return new Pair<>(addPair, removePair);
    }

    /**
     * The method group all the fields into hierarchy based on the path (separated
     * by dot) then put into a map ready for lazy loading when expanding on the
     * grouped list.
     */
    private void initUiGrids(final UIGridPreferences uiGridPreferences, final List<FieldNameModel> firstLevelColumns) {
        uiGridPreferences.getColumns().stream().forEach(uiColumns -> {
            final String fullPath = uiColumns.getFieldName();
            if (fullPath.indexOf(".") != -1) {
                final String[] splitted = fullPath.split("\\.");
                String fieldName = "";

                for (int i = 0; i < splitted.length; i++) {

                    if (i == 0) {
                        fieldName = splitted[i];
                        final FieldNameModel fnm = new FieldNameModel();
                        fnm.setFieldName(fieldName);
                        if (!firstLevelColumns.contains(fnm)) {
                            firstLevelColumns.add(fnm);
                        }
                    } else {
                        final String upperLevel = fieldName;
                        fieldName += "." + splitted[i];
                        final FieldNameModel fnm = new FieldNameModel();
                        fnm.setFieldName(fieldName);
                        if (i == splitted.length - 1) {
                            fnm.setFullPath(fullPath);
                            fnm.setSelected(selections.contains(fullPath));
                        }
                        final List<FieldNameModel> fnms = columnsByKey.computeIfAbsent(upperLevel, (k) -> new ArrayList<>());
                        if (!fnms.contains(fnm)) {
                            fnms.add(fnm);
                        }
                    }
                }
            } else {
                final FieldNameModel fnm = new FieldNameModel();
                fnm.setFieldName(fullPath);
                fnm.setFullPath(fullPath);
                fnm.setSelected(selections.contains(fullPath));
                if (!firstLevelColumns.contains(fnm)) {
                    firstLevelColumns.add(fnm);
                }
            }
        });
    }

    /**
     * A recursive method to dynamically construct grid on first level also applied
     * on grouping item being opened
     */
    private void createGrid(final ApplicationSession session, final Grid grid, final List<FieldNameModel> data) {
        final Columns columns = new Columns();
        final Column detail = new Column();
        detail.setWidth("40px");
        columns.appendChild(detail);
        columns.appendChild(new Column(session.translate("editGrid", "fieldName")));
        columns.appendChild(new Column(session.translate("editGrid", "technicalName")));

        grid.appendChild(columns);
        grid.setRowRenderer(new RowRenderer<FieldNameModel>() {

            @Override
            public void render(final Row row, final FieldNameModel data, final int index) throws Exception {
                final String fieldName = data.getFieldName();

                final String translatedLabel = session.translate(viewModelId, fieldName);
                if (columnsByKey.containsKey(data.getFieldName())) {
                    final Detail detail = new Detail();
                    detail.setAttribute("data", data);
                    detail.addEventListener(Events.ON_OPEN, (e) -> {
                        e.getTarget().getChildren().clear();
                        final FieldNameModel fnm = (FieldNameModel) e.getTarget().getAttribute("data");
                        final Grid grid = new Grid();
                        grid.setParent(e.getTarget());
                        createGrid(session, grid, columnsByKey.get(fnm.getFieldName()));
                        LOGGER.debug("EVENT: {} {}", fnm.getFieldName(), e.getTarget());
                    });
                    detail.setParent(row);
                } else {
                    final Checkbox cb = new Checkbox();
                    cb.setChecked(data.isSelected);
                    cb.addEventListener(Events.ON_CHECK, (e) -> {
                        if (((Checkbox) e.getTarget()).isChecked()) {
                            selections.add(data.fullPath);
                        } else {
                            selections.remove(data.fullPath);
                        }
                    });
                    cb.setParent(row);
                }

                // translate the label
                final Popup tooltip = new Popup();
                final Div labelWrapper = new Div();
                final Label label = new Label(translatedLabel);
                final Label toolTipLabel = new Label(data.fieldName);
                toolTipLabel.setParent(tooltip);
                label.setTooltip(tooltip);
                tooltip.setParent(labelWrapper);
                label.setParent(labelWrapper);
                labelWrapper.setParent(row);

                final Label technicalName = new Label(data.fieldName);
                technicalName.setParent(row);

            }
        });
        grid.setModel(new ListModelList<>(data));
        grid.setVflex("1");
    }

    /**
     * Convenient method to check each of the items in the first list if they are
     * existed in second list, if not add into third list.
     */
    private void addIfAbsent(final Collection<String> c1, final Collection<String> c2, final Collection<String> c3) {
        for (final String s : c1) {
            if (!c2.contains(s)) {
                c3.add(s);
            }
        }
    }

    public class FieldNameModel {

        private String fieldName;
        private String fullPath;
        private boolean isSelected;

        public String getFieldName() {
            return fieldName;
        }

        public String getFullPath() {
            return fullPath;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(final boolean xisSelected) {
            this.isSelected = xisSelected;
        }

        public void setFieldName(final String fieldName) {
            this.fieldName = fieldName;
        }

        public void setFullPath(final String fullPath) {
            this.fullPath = fullPath;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FieldNameModel other = (FieldNameModel) obj;
            if (fieldName == null) {
                if (other.fieldName != null)
                    return false;
            } else if (!fieldName.equals(other.fieldName))
                return false;
            return true;
        }
    }
}
