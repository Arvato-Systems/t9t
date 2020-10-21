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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.Borderlayout;
import org.zkoss.zul.Center;
import org.zkoss.zul.North;
import org.zkoss.zul.West;

import com.arvatosystems.t9t.component.ext.EventDataSelect28;
import com.arvatosystems.t9t.component.ext.IDataSelectReceiver;

public class ThreeSections28 extends TwoSections28 {
    private static final long serialVersionUID = -2739908309693945324L;
    private static final Logger LOGGER = LoggerFactory.getLogger(ThreeSections28.class);
    protected Groupbox28 detailsGroup;

    public ThreeSections28() {
        super();
        LOGGER.debug("new ThreeSections28() created");
        detailsGroup = new Groupbox28();
        detailsGroup.setVflex("1");
        detailsGroup.setId("detailsGroup");
        detailsGroup.setSclass("detailsSection");
        Borderlayout bl = (Borderlayout) resultsGroup.getParent().getParent().getParent();
        ((North) bl.getFirstChild()).setHeight("50%"); //override the height to 50% for three sections
        Center center = new Center();
        center.setParent(bl);
        detailsGroup.setParent(center); //To attached to the bottom of result panel
    }

    @Override
    @Listen("onCreate")
    public void onCreate() {
        super.onCreate();
        // move all childs of this after the last groupbox into the last groupbox
        List<Component> children = ComponentTools28.moveChilds(this, this.getFirstChild(), detailsGroup);
        if (children != null && !children.isEmpty()) {
            for (Component child : children) {
                if (child instanceof IDataSelectReceiver) {
                    final IDataSelectReceiver recv = (IDataSelectReceiver) child;
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

    // allow to use a nonstandard distribution of space
    public String getVflex3() {
        return detailsGroup.getVflex();
    }

    public void setVflex3(String vflex) {
        detailsGroup.setVflex(vflex);
    }

    @Override
    public void setVlayoutVisible(boolean visible) {
        super.setVlayoutVisible(visible);
        detailsGroup.setTitle(null);              //To reduce the space being taken by the title.
        detailsGroup.setSclass("no-padding-top"); //To reduce the space being taken by the title.
        ((North) resultsGroup.getParent().getParent()).setVisible(false);
        ((West)  filterGroup.getParent()).setVisible(false);
    }
}
