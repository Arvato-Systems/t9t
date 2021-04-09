package com.arvatosystems.t9t.tfi.services;

import java.util.List;

import com.arvatosystems.t9t.tfi.component.dropdown.Dropdown28Db;

public interface ISelectionsResolver {
    public default List<String> getSelections() { return null; };
    public abstract void setSelection(String object);
    public default String getNextScreen() { return null; };
    public default Dropdown28Db<?> getDropdownComponent() { return null; }
}
