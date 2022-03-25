package com.arvatosystems.t9t.zkui.components.datafields;

import com.arvatosystems.t9t.zkui.components.dropdown28.factories.Dropdown28FactoryForQualifiers;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28ComboBoxItem;

public class DropdownComboBoxItemDataField extends AbstractDataField<Dropdown28ComboBoxItem, String> {
    protected final Dropdown28ComboBoxItem c;

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public DropdownComboBoxItemDataField(DataFieldParameters params, String qualifierFor) {
        super(params);
        c = Dropdown28FactoryForQualifiers.createInstance(qualifierFor);
        setConstraints(c, null);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Dropdown28ComboBoxItem getComponent() {
        return c;
    }

    @Override
    public String getValue() {
        return c.getSelectedItem() == null ? null : c.getSelectedItem().getValue();
    }

    @Override
    public void setValue(String value) {
        c.setSelectedItem(c.getComboItemByValue(value));
    }
}
