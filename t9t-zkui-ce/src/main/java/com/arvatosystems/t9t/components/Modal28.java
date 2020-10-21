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
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

public class Modal28 extends Window {
    private static final long serialVersionUID = -820340570301080582L;
    private static final Logger LOGGER = LoggerFactory.getLogger(Modal28.class);

    @Wire("#okButton")
    Button okButton;

    @Wire("#cancelButton")
    Button cancelButton;

    @Wire("#vlayout")
    Vlayout vlayout;

    @Wire("#buttons")
    Hlayout hlayout;

    public Modal28() {
        super();
        LOGGER.debug("new Modal28() created");
        Executions.createComponents("/component/modal28.zul", this, null);
        Selectors.wireComponents(this, this, false);
        // set some properties
        setWidth("33%");
        setClosable(true);
        setSizable(true);
        setMaximizable(true);

        // wire events
        cancelButton.addEventListener(Events.ON_CLICK, ev -> cancelWindow());
        okButton    .addEventListener(Events.ON_CLICK, ev -> confirmWindow());
    }

    @Override
    public void setId(String id) {
        super.setId(id);
        setTitle(ApplicationSession.get().translate(null, id));
    }

    @Listen("onCreate")
    public void onCreate() {
        // rearrange content: first step: move them to the right hierarchy
        List<Component> chilren = ComponentTools28.moveChilds(this, vlayout, vlayout);
        LOGGER.debug("My IDs are {} / {} / {}", hlayout, vlayout, chilren);
        // rearrange content: second step: shuffle hlayout with buttons to the end
        vlayout.appendChild(hlayout);
        setMode(Mode.MODAL);
    }

    public void cancelWindow() {
        Events.postEvent(new Event(Events.ON_CLOSE, this, null));
    }

    public void confirmWindow() {
        LOGGER.debug("Confirmed");
        // create an OK event which can be caught by other components
        Events.postEvent(new Event(Events.ON_OK, this, null));
    }
}
