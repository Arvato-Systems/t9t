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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

public class Groupbox28 extends Groupbox{
    private static final Logger LOGGER = LoggerFactory.getLogger(Groupbox28.class);
    private static final long serialVersionUID = -8089438052410249L;
    private String initialVFlex;

    public Groupbox28() {
        super();
        LOGGER.debug("new Groupbox28() created");
        // Executions.createComponents("/component/groupbox28.zul", this, null);
        // setVflex("1");
        // setHflex("1");
        //setWidth("100%");
        setClosable(false);
        setSclass("caption");

        addEventListener(Events.ON_OPEN, new EventListener<OpenEvent>() {
            @Override
            public void onEvent(OpenEvent event) throws Exception {
                LOGGER.debug("GB event {}, now {}", getId(), event.isOpen());
                onOpenhandler(event);
            }
        });
    }

    void onOpenhandler(OpenEvent event) {
        Component parent = event.getTarget().getParent();

        Window window = (Window) getRoot();
        if (parent instanceof ThreeSections28) {
            threeSectionHandler(event, (ThreeSections28) parent);
            window.invalidate();
        } else if (parent instanceof TwoSections28) {
            twoSectionHandler(event, (TwoSections28) parent);
            window.invalidate();
        } else {
            LOGGER.error("No handler found");
        }
    }

    void twoSectionHandler(OpenEvent event, TwoSections28 section)  {
        section.filterGroup.setInitialVFlex(section.filterGroup.getVflex());
        section.resultsGroup.setInitialVFlex(section.resultsGroup.getVflex());

        section.filterGroup.setVflex(section.filterGroup.isOpen()?section.filterGroup.getInitialVFlex():null);
        section.resultsGroup.setVflex(section.resultsGroup.isOpen()?section.resultsGroup.getInitialVFlex():null);
    }


    void threeSectionHandler(OpenEvent event, ThreeSections28 section) {
        section.filterGroup.setInitialVFlex(section.filterGroup.getVflex());
        section.resultsGroup.setInitialVFlex(section.resultsGroup.getVflex());
        section.detailsGroup.setInitialVFlex(section.detailsGroup.getVflex());

        section.filterGroup.setVflex(section.filterGroup.isOpen()?section.filterGroup.getInitialVFlex():null);
        section.resultsGroup.setVflex(section.resultsGroup.isOpen()?section.resultsGroup.getInitialVFlex():null);
        section.detailsGroup.setVflex(section.detailsGroup.isOpen()?section.detailsGroup.getInitialVFlex():null);
    }

    @Override
    public void setId(String id) {
        LOGGER.debug("Groupbox28() assigned  ID {}", id);
        super.setId(id);
        setTitle(ApplicationSession.get().translate(null, id));
    }

    @Override
    public void setVflex(String flex) {
        super.setVflex(flex);
        //setInitialVFlex(flex);
    }

    public String getInitialVFlex() {
        return initialVFlex;
    }

    public void setInitialVFlex(String initialVFlex) {
        if (null == this.initialVFlex) {
            this.initialVFlex = initialVFlex;
        }
    }
}
