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
package com.arvatosystems.t9t.tfi.component;

import java.util.List;

import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;

public abstract class Dropdown28Ext extends Combobox {
    /**
     *
     */
    private static final long serialVersionUID = -4334311396799707798L;
    final List<String> modelData;
    final AbstractComponent self = this;

    protected Dropdown28Ext(List<String> initialModel) {
        super();
        this.setAutocomplete(true);
        this.setAutodrop(true);
        this.setHflex("1");
        this.setSclass("dropdown");
        this.modelData = initialModel;

        setModel(new SimpleListModelExt<String>(modelData));

        this.addEventListener(Events.ON_CHANGE, (event) -> {
            if (!modelData.contains(getValue())) {
                setRawValue("");  // clearing raw data is not always what we want, it kills the ability to search with LIKE criteria...
                setModel(new SimpleListModelExt<String>(modelData));
            }
        });
    }
}
