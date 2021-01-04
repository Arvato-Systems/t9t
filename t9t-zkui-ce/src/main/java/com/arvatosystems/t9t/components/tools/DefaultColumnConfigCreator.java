package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.zkoss.util.Pair;
import org.zkoss.zul.Div;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.ui.UIColumnConfiguration;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

/**
 * CE version of column configuration component that list out all the fields available on a specific grid id
 */
@Singleton
@Fallback
public class DefaultColumnConfigCreator implements IColumnConfigCreator {

    private final ApplicationSession session = ApplicationSession.get();
    private UIGridPreferences uiGridPreferences = null;
    private Set<String> currentGrid = null;
    private Listbox listbox = null;

    @Override
    public void createColumnConfigComponent(Div parent, UIGridPreferences uiGridPreferences, Set<String> currentGrid) {
        parent.setVflex("1");
        this.currentGrid = currentGrid;
        listbox = new Listbox();
        List<String> allAvailableFieldNames = new LinkedList<>();
        uiGridPreferences.getColumns().stream().forEach(uiColumns -> {
            allAvailableFieldNames.add(uiColumns.getFieldName());
        });
        listbox.setItemRenderer(new ListitemRenderer<String>() {
            @Override
            public void render(Listitem item, String data, int index) throws Exception {
                item.setValue(data);
                item.setLabel(session.translate(null, data));
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
    public Pair<List<String>, List<String>> getAddRemovePairs() {
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
