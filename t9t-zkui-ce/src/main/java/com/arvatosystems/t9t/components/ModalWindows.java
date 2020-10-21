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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;

import de.jpaw.bonaparte.core.BonaPortable;

public class ModalWindows {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModalWindows.class);

    public static <T extends BonaPortable> Component runModal(
            final String zulFile,
            final Component parent,
            final T srcViewModel, boolean deepCopy,
            final Consumer<T> onOK) {

        final T viewModel = deepCopy ? (T) srcViewModel.ret$MutableClone(true, true) : srcViewModel;
        Map<String, Object> inst = new HashMap<String, Object>();
        inst.put("inst", viewModel);

        Component modal = Executions.createComponents(zulFile, parent, inst);
        if (onOK != null) {
            modal.addEventListener(Events.ON_OK, ev -> {
                LOGGER.debug("modal window OK: performing task");
                // perform some (blocking) code
                onOK.accept(viewModel);
                // then close the window
                LOGGER.debug("now closing modal window");
                Events.postEvent(new Event(Events.ON_CLOSE, modal, null));
            });
        }
        return modal;
    }
}
