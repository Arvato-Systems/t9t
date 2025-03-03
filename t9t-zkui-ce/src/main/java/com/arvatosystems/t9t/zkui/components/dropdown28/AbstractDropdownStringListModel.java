package com.arvatosystems.t9t.zkui.components.dropdown28;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.zkui.components.IStringListModel;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDropdownStringListModel implements IStringListModel {

    @Nonnull
    @Override
    public List<String> getListModel() {
        final ApplicationSession session = ApplicationSession.get();
        final String cacheId = getId();
        final List<Description> descriptions;
        if (cacheId == null) {
            descriptions = session.getDropDownData(getLeanSearchRequest());
        } else {
            descriptions = session.getDropDownData(cacheId, getLeanSearchRequest());
        }
        final List<String> result = new ArrayList<>(descriptions.size());
        for (final Description desc: descriptions) {
            result.add(desc.getId());
        }
        return result;
    }

    @Nullable
    public String getId() {
        // default implementation returns null, override if needed
        return null;
    }

    @Nonnull
    protected abstract LeanSearchRequest getLeanSearchRequest();
}
