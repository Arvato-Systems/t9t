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
import org.zkoss.zul.Tab;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

/** Uses the translation of the id as label text.
 * Must be a child of a component which knows a viewModel, to get the data type.
 */
public class Tab28 extends Tab {
    private static final Logger LOGGER = LoggerFactory.getLogger(Tab28.class);
    private static final long serialVersionUID = -77019312387261940L;

    public Tab28() {
        super();

    }
    @Override
    public void setId(String myId) {
        super.setId(myId);
        xlate(myId);
    }

    private void xlate(String myId) {
        IViewModelOwner vmOwner = GridIdTools.getAnchestorOfType(this, IViewModelOwner.class);
        String viewModelId = GridIdTools.enforceViewModelId(vmOwner);
        ApplicationSession as = vmOwner.getSession();
        String label = as.translate(viewModelId, myId);
        LOGGER.debug("ViewModel is {}, ID = {}, setting label to {}", viewModelId, myId, label);
        setLabel(label);
    }
}
