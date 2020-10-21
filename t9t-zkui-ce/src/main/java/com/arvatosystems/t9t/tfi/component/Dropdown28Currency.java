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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

public class Dropdown28Currency extends Dropdown28Ext {

    private static final long serialVersionUID = 7804881425211020003L;

    private static final List<String> currencyModelData = new ArrayList<>();
    static {
        currencyModelData.addAll(
                Currency.getAvailableCurrencies().stream().sorted(Comparator.comparing(Currency::getCurrencyCode))
                .map(Currency::getCurrencyCode).collect(Collectors.toList()));
    }

    public Dropdown28Currency() {
        super(currencyModelData);
        this.setMaxlength(3);
    }
}
