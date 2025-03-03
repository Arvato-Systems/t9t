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
package com.arvatosystems.t9t.zkui.ee.components.datafields;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.components.IStringListModel;
import com.arvatosystems.t9t.zkui.components.datafields.AbstractCoreDataField;
import com.arvatosystems.t9t.zkui.components.datafields.DataFieldParameters;
import com.arvatosystems.t9t.zkui.util.ZulUtils;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zkmax.zul.Chosenbox;
import org.zkoss.zul.ListModelList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class StringChosenboxDataField extends AbstractCoreDataField<Chosenbox, String> {

    protected final Chosenbox c = new Chosenbox();
    protected final ListModelList<String> listModelList;
    protected final IStringListModel stringListModel;

    protected StringChosenboxDataField(@Nonnull final DataFieldParameters params, @Nonnull final String listModel) {
        super(params);
        stringListModel = Jdp.getRequired(IStringListModel.class, listModel);
        listModelList = new ListModelList<>(stringListModel.getListModel());
        c.setModel(listModelList);
        c.setHflex("1");
        c.setCreatable(stringListModel.createNewEntry());
        c.setCreateMessage(ZulUtils.translate("com", "list.newEntry"));
        c.addEventListener("onSearch", event -> {
            if (event instanceof InputEvent inputEvent) {
                final String value = inputEvent.getValue();
                if (!listModelList.contains(value)) {
                    listModelList.add(value);
                    listModelList.addToSelection(value);
                }
            }
        });
    }

    @Override
    public Chosenbox getComponent() {
        return c;
    }

    @Override
    public void clear() {
        c.setSelectedObjects(Collections.EMPTY_SET);
    }

    @Override
    public boolean empty() {
        return c.getSelectedObjects().isEmpty();
    }

    @Override
    public String getValue() {
        final Set<String> selections = c.getSelectedObjects();
        if (T9tUtil.isNotEmpty(selections)) {
            final StringBuilder sb = new StringBuilder();
            int i = 0;
            for (final String selection : selections) {
                sb.append(selection);
                if (i < selections.size() - 1) {
                    sb.append(stringListModel.getListDelimiter());
                }
                i++;
            }
            return sb.toString();
        }
        return null;
    }

    @Override
    public void setValue(final String data) {
        final List<String> selections = new ArrayList<>();
        if (data != null) {
            final String[] items = data.split(stringListModel.getListDelimiter());
            for (final String item : items) {
                if (!listModelList.contains(item)) {
                    listModelList.add(item);
                }
                selections.add(item);
            }
        }
        c.setSelectedObjects(selections);
    }

    @Override
    public void setDisabled(final boolean disabled) {
        c.setDisabled(disabled);
    }
}
