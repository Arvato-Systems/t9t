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
package com.arvatosystems.t9t.tfi.model.bean;

/**
 * General bean for combobox entries.
 * @author INCI02
 *
 */
public class ComboBoxItem {

    /** name of the combobox entry. This will be shown on frontend **/
    private String name;
    /** value of the combobox entry. **/
    private String value;

    /**
     * Constructor.
     * @param name of the combobox entry. This will be shown on frontend .
     * @param value of the combobox entry.
     */
    public ComboBoxItem(final String name, final String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name.
     * @return name
     */
    public final String getName() {
        return name;
    }
    /**
     *
     * @param name set name.
     */
    public final void setName(final String name) {
        this.name = name;
    }

    /**
     * Returns the value.
     * @return value
     */
    public final String getValue() {
        return value;
    }

    /**
     * @param value set value.
     */
    public final void setValue(final String value) {
        this.value = value;
    }

}
