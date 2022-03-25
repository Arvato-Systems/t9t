package com.arvatosystems.t9t.zkui.components.dropdown28.nodb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import com.arvatosystems.t9t.zkui.viewmodel.beans.ComboBoxItem;

public class Dropdown28ComboBoxItem extends Combobox {
    private static final long serialVersionUID = 391144627872732669L;

    final Map<String, Comboitem> lookupByValues;

    public Dropdown28ComboBoxItem(List<ComboBoxItem> initialModels) {
        super();
        this.setAutocomplete(true);
        this.setAutodrop(true);
        this.setHflex("1");
        this.setSclass("dropdown");
        // this.modelData = initialModels;

        lookupByValues = new HashMap<>(initialModels.size());

        for (ComboBoxItem cbi : initialModels) {
            Comboitem item = new Comboitem(cbi.getName());
            item.setValue(cbi.getValue());
            item.setParent(this);
            lookupByValues.put(item.getValue(), item);
        }

    }

    public Comboitem getComboItemByValue(String value) {
        return lookupByValues.get(value);
    }
}
