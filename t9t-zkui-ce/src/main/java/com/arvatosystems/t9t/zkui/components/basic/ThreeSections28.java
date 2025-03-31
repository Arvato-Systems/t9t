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
package com.arvatosystems.t9t.zkui.components.basic;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.arvatosystems.t9t.zkui.util.JumpTool;
import de.jpaw.bonaparte.core.BonaPortable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import org.zkoss.zul.West;

import com.arvatosystems.t9t.zkui.components.EventDataSelect28;
import com.arvatosystems.t9t.zkui.components.IDataSelectReceiver;
import com.arvatosystems.t9t.zkui.util.T9tConfigConstants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

public class ThreeSections28 extends TwoSections28 {
    private static final long serialVersionUID = -2739908309693945324L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeSections28.class);
    protected static final String DEFAULT_OVERVIEW_HEIGHT = "50%";
    protected Groupbox28 detailsGroup;
    protected String overviewHeight;

    public ThreeSections28() {
        // initialize detail group
        detailsGroup = new Groupbox28();
        detailsGroup.setVflex("1");
        detailsGroup.setId("detailsGroup");
        detailsGroup.setSclass("detailsSection");
    }

    @Override
    @Listen("onCreate")
    public void onCreate() {
        super.onCreate();
        LOGGER.debug("new ThreeSections28() created");
        Borderlayout bl = (Borderlayout) resultsGroup.getParent().getParent().getParent();
        if (overviewHeight == null) {
            String configuredDefaultHeight = ZulUtils.readConfig(T9tConfigConstants.THREE_SECTION_DEFAULT_OVERVIEW_HEIGHT);
            overviewHeight = configuredDefaultHeight == null ? DEFAULT_OVERVIEW_HEIGHT : configuredDefaultHeight;
        }
        ((North) bl.getFirstChild()).setHeight(overviewHeight); //override the height to 50% for three sections
        Center center = new Center();
        center.setParent(bl);
        detailsGroup.setParent(center); //To attached to the bottom of result panel

        // move all childs of this after the last groupbox into the last groupbox
        List<Component> children = ComponentTools28.moveChilds(this, this.getFirstChild(), detailsGroup);
        if (children != null && !children.isEmpty()) {
            for (Component child : children) {
                if (child instanceof Crud28 crud28) {
                    // if the child is crud28 and the data is provided, use it to populate data in crud28 and also us the saveHandler to save the data
                    final Map<String, Object> requestParams = as.getRequestParams();
                    final Object data = requestParams.get(JumpTool.DATA);
                    final Object handler = requestParams.get(JumpTool.SAVE_HANDLER);
                    if (data instanceof BonaPortable bonaData && handler instanceof Consumer saveHandler) {
                        crud28.setDataChanges(bonaData, saveHandler);
                        ((North) resultsGroup.getParent().getParent()).setVisible(false);
                        ((West)  filterGroup.getParent()).setVisible(false);
                        break;
                    }
                }
                if (child instanceof IDataSelectReceiver recv) {
                    // wire events
                    main.addEventListener(EventDataSelect28.ON_DATA_SELECT, ev -> {
                        final EventDataSelect28 evData = (EventDataSelect28) ev.getData();
                        LOGGER.debug("Caught DATA_SELECT event of grid");
                        recv.setSelectionData(evData);
                    });
                    // wire a changed content of a possible crud section to row refresh
                    child.addEventListener("onCrudUpdate", ev -> {
                        if (Boolean.TRUE.equals(ev.getData()))
                            main.refreshCurrentItem(); // single row change
                        else
                            main.search(); // structural change: data has been added or deleted
                    });
                } else {
                    LOGGER.error("Details section is not known as ISelectionReceiver, cannot forward select events");
                }
            }
        }
    }

    @Override
    public void setVlayoutVisible(boolean visible) {
        super.setVlayoutVisible(visible);
        detailsGroup.setTitle(null);              //To reduce the space being taken by the title.
        detailsGroup.setSclass("no-padding-top"); //To reduce the space being taken by the title.
        ((North) resultsGroup.getParent().getParent()).setVisible(false);
        ((West)  filterGroup.getParent()).setVisible(false);
    }

    public void setOverviewHeight(String overviewHeight) {
        this.overviewHeight = overviewHeight;
    }
}
