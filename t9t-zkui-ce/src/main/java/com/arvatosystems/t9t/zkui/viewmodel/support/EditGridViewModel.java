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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.base.IGridConfigContainer;
import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.services.IColumnConfigCreator;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.dp.Jdp;

public class EditGridViewModel {

    protected Listbox editGridListBox;
    private Window windowComponent = null;
    private UIGridPreferences uiGridPreferences = null;
    private Set<String> currentGrid = null;
    private final IColumnConfigCreator columnConfigCreator = Jdp.getRequired(IColumnConfigCreator.class);

    @Wire("#component")
    private Div div;

    @SuppressWarnings("unchecked")
    @Init(superclass = true)
    public void init(@BindingParam("initParams") HashMap<String, Object> initParams,
            @ContextParam(ContextType.COMPONENT) Component component) {
        windowComponent = (Window) component.getRoot();
        if (initParams != null && initParams.get("gridId") != null) {
            uiGridPreferences = IGridConfigContainer.GRID_CONFIG_REGISTRY.get(initParams.get("gridId"));
            if (initParams.get("currentGridList") != null) {
                currentGrid = ((List<String>) initParams.get("currentGridList")).stream().collect(Collectors.toSet());
            }
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
        columnConfigCreator.createColumnConfigComponent(ApplicationSession.get(), div, uiGridPreferences, currentGrid);
    }

    @Command
    public void closeWindow() {
        Events.sendEvent("onClose", windowComponent, null);
        windowComponent.onClose();
    }

    @Command
    public void updateGrid() {
        Pair<List<String>, List<String>> pairs = columnConfigCreator.getAddRemovePairs(ApplicationSession.get());
        // do not allow user to close the dialog if nothing is selected
        if (pairs != null) {
            Events.sendEvent("onClose", windowComponent, pairs);
            windowComponent.onClose();
        }
    }
}
