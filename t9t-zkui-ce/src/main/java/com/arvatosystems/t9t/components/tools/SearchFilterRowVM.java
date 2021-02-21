package com.arvatosystems.t9t.components.tools;

import java.util.ArrayList;
import java.util.List;

import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class SearchFilterRowVM {
    private boolean selected;
    private String filterName;
    private String currentSelection;
    private Boolean negate;
    private List<String> filterTypes;

    public SearchFilterRowVM(String name) {
        selected = false;
        filterName = name;
        currentSelection = "";
        negate = false;
        filterTypes = new ArrayList<>(UIFilterType.values().length);
    }

    public SearchFilterRowVM(UIFilter filter) {
        filterName = filter.getFieldName();
        if (filter.getFilterType() != null) {
            currentSelection = filter.getFilterType().name();
        }
        negate = filter.getNegate();
        selected = true;
    }

    public boolean getSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public Boolean getNegate() {
        return negate;
    }

    public void setNegate(Boolean negate) {
        this.negate = negate;
    }

    public String getCurrentSelection() {
        return currentSelection;
    }

    public void setCurrentSelection(String currentSelection) {
        this.currentSelection = currentSelection;
    }

    public List<String> getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(List<String> filterTypes) {
        this.filterTypes = filterTypes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((filterName == null) ? 0 : filterName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SearchFilterRowVM other = (SearchFilterRowVM) obj;
        if (filterName == null) {
            if (other.filterName != null)
                return false;
        } else if (!filterName.equals(other.filterName))
            return false;
        return true;
    }
}