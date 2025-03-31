/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.util.ArrayList;
import java.util.List;

import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public class SearchFilterRowVM {
    private boolean selected;
    private String filterName;
    private String currentSelection;
    private Boolean negate;
    private String qualifier;
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
        setQualifier(filter.getQualifier());
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

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
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
