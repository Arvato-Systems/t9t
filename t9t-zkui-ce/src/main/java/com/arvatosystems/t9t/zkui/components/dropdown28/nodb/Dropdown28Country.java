/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dropdown28Country extends Dropdown28Ext {

    private static final List<String> COUNTRY_MODEL_DATA = new ArrayList<>();
    static {
        Stream<String> s = Arrays.stream(Locale.getISOCountries());
        COUNTRY_MODEL_DATA.add("XX");  // used for wildcards in t9t doc module
        COUNTRY_MODEL_DATA.add("EU");  // sometimes used for European Union, will be mapped to specific code by the backend
        COUNTRY_MODEL_DATA.addAll(s.sorted().collect(Collectors.toList()));
    }

    /**
     *
     */
    private static final long serialVersionUID = 3911446278727438869L;

    public Dropdown28Country() {
        super(COUNTRY_MODEL_DATA);
        this.setMaxlength(2);
    }
}
