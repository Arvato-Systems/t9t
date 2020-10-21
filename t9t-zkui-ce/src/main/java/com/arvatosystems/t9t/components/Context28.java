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
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

/** Component which creates a context menu by comma separated list of IDs.
 * The IDs are used to look up the translations for the entry as well as to identify the action.
 *
 * When an entry is clicked, the component fires an "onClick" event with the event data,
 * this means the events on the menuItems are transformed into events of the control itself.
 *
 * Tbd: is it better to use postEvent or sendEvent? (see https://www.zkoss.org/wiki/ZK_Developer's_Reference/Event_Handling/Event_Firing)
 *
 */
public class Context28 extends Menupopup {
    private static final Logger LOGGER = LoggerFactory.getLogger(Context28.class);
    private static final long serialVersionUID = -8089587938052410249L;
    protected final ApplicationSession session = ApplicationSession.get();

    public Context28() {
        super();
    }


    /** Creates the options which are provided as a comma separate list of strings. */
    public void setContextOptions(String options) {
        String myPrefix = getId() + ".";
        String [] optionsList = options.split(",");
        for (String option : optionsList) {
            if (option.length() == 0) {
                // empty option is a separator
                Menuseparator sep = new Menuseparator();
                sep.setParent(this);
            } else {
                final String itemId = myPrefix + option;
                final Permissionset perms = session.getPermissions(itemId);
                if (perms.contains(OperationType.EXECUTE)) {
                    LOGGER.debug("Installing context option {} for {}", option, getId());
                    Menuitem item = new Menuitem();
                    item.setId(itemId);
                    item.setParent(this);
                    item.setLabel(session.translate(getId(), option));
                    // event listener not required - a listener is added in grid28 already!
//                  item.addEventListener(Events.ON_CLICK, (Event ev) -> {
//                      LOGGER.debug("Clicked {} in menuitem", itemId);
//                      Events.postEvent(Events.ON_CLICK, this, itemId);
//                  });
                } else {
                    LOGGER.debug("NOT installing context option {} for {} - no permissions for user {}", option, getId(), session.getUserId());
                }
            }
        }
    }
}
