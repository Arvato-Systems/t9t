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
package com.arvatosystems.t9t.zkui.components.dropdown28.nodb;

import com.arvatosystems.t9t.zkui.util.Constants;

public class Dropdown28Country extends Dropdown28Ext {

    private static final long serialVersionUID = 3911446278727438869L;

    public Dropdown28Country() {
        super(Constants.COUNTRY_MODEL_DATA);
        this.setMaxlength(2);
    }
}
