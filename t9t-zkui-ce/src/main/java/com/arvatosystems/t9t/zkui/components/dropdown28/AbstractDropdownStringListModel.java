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
