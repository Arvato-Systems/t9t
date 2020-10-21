/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.components;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabs;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.FilterGenerator;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;
import com.arvatosystems.t9t.component.ext.IFilterGenerator;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;
import com.arvatosystems.t9t.components.extensions.ITabboxExtension;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;

/** A tab box which distributes any received onSelect event to the currently active tab.
 * It also provides automated setting of the tabs headers.
 *
 * The parent of this control must define the viewModelId
 *
 * The client must code the <tabpanels> manually.
 */
public class Tabbox28 extends Tabbox implements IViewModelOwner, IDataSelectReceiver {
    private static final long serialVersionUID = -7088578448192569162L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Tabbox28.class);
    private static final AtomicInteger UNDEF_IDs = new AtomicInteger();

    private Tabs tabs;
    private EventDataSelect28 lastSelected;
    private final ApplicationSession as = ApplicationSession.get();
    private String viewModelId;
    private CrudViewModel<BonaPortable, TrackingBase> crudViewModel;  // set when gridId is defined
    private Tabpanel28 firstTab;
    private IFilterGenerator defaultFilterGenerator;    // filter to be used if no tab specific filter is provided
    private ITabboxExtension extension = null;

    public Tabbox28() {
        super();
        setVflex("1");
        tabs = new Tabs();
        tabs.setParent(this);
    }

    public void setFilterFieldname(String fieldname) {
        defaultFilterGenerator = FilterGenerator.filterForFieldname(fieldname);
    }

    public void setFilterName(String name) {
        defaultFilterGenerator = FilterGenerator.filterForName(name);
    }

    public IFilterGenerator getDefaultFilterGenerator() {
        return defaultFilterGenerator;
    }

    public void register(final Tabpanel28 panel) {
        if (firstTab == null)
            firstTab = panel;
        if (panel.getId() == null) {
            LOGGER.error("Missing id for tabpanel28!");
            panel.setId("TP_UNDEF" + UNDEF_IDs.incrementAndGet());
        }
        final String tabId = panel.getId();
        Tab tab = new Tab();
        tab.setLabel(as.translate(null, tabId));
        tab.setClosable(false);
        tab.setParent(tabs);
        // onClick handler:
        // 1) send the current selection to the new tabpanel
        // 2) in case of a new selection (inbound), forward that to the current tabpanel (therefore we have to store it)
        tab.addEventListener(Events.ON_SELECT, (SelectEvent ev) -> {
            LOGGER.debug("SELECT event on Tab {}", tabId);
//          currentTab = panel;
            panel.setSelectionData(lastSelected);
        });
        if (extension != null)
            extension.afterRegisterPanel(this, tab, panel);
    }

    public void unregister(final Tabpanel28 panel) {
        LOGGER.debug("Unregistering panel with id = {}", panel.getId());
        if (firstTab.getId().equals(panel.getId())) {
            firstTab = null;
        }
        panel.getLinkedTab().setParent(null);
        panel.setParent(null);
        panel.removeEventListener(Events.ON_SELECT, (SelectEvent ev) -> {
            LOGGER.debug("Removing event listener on Tab {}", panel.getLinkedTab().getId());
            panel.setSelectionData(lastSelected);
        });
    }

    public void notifyCurrent() {
        Tabpanel current = getSelectedPanel();
        LOGGER.debug("received SELECT/NOTIFY, current = {}", current);
        if (current != null) {
            ((Tabpanel28)current).setSelectionData(lastSelected);
        }
    }

    @Override
    public CrudViewModel<BonaPortable, TrackingBase> getCrudViewModel() {
        GridIdTools.enforceViewModelId(this);
        return crudViewModel;
    }

    @Override
    public String getViewModelId() {
        return viewModelId;
    }

    @Override
    public void setViewModelId(String viewModelId) {
        this.viewModelId = viewModelId;
        crudViewModel = GridIdTools.getViewModelByViewModelId(viewModelId);
    }

    @Override
    public void setSelectionData(EventDataSelect28 eventData) {
        lastSelected = eventData;
        notifyCurrent();
    }

    @Override
    public ApplicationSession getSession() {
        return as;
    }

    public void setExtension28(String qualifier) {
        this.extension = Jdp.getRequired(ITabboxExtension.class, qualifier);
        this.extension.init(this);
    }

    public EventDataSelect28 getLastSelected() {
        return lastSelected;
    }
}
