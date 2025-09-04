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
package com.arvatosystems.t9t.zkui.components.dropdown28;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.ListModel;
import org.zkoss.zul.SimpleListModel;

public class SimpleListModelExt<E> extends SimpleListModel<E> {
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleListModelExt.class);

    /**
     *
     */
    private static final long serialVersionUID = -848831378739903090L;

    public SimpleListModelExt(final List<E> data) {
        super(data);
        LOGGER.debug("created SimpleListModelExt for {} entries", data.size());
    }

    /**
     * Returns a submodel containing at most nRows elements that match the given value.
     */
    @Override
    public ListModel<E> getSubModel(final Object value, final int nRows) {
        LOGGER.debug("getSubModel {} for {} rows for value {}", value, nRows, value);
        final String filter = value == null ? "" : value.toString().toLowerCase(); // avoid doing toLowerCase multiple times
        return super.getSubModel(filter, nRows);
    }

    /**
     * Compares if the given value shall belong to the submodel represented by the key.
     *
     * Default: converts both key and value to String objects and then return true if the String object of value starts with the String object (or the key is
     * empty). The comparison is case insensitive.
     *
     * @param key   the key representing the submodel. In autocomplete, it is the value entered by user. This has been converted to lowercase already.
     * @param value the value in this model.
     */
    @Override
    protected boolean inSubModel(final Object key, final Object value) {
        if (key == null) {
            return true;
        }
        final String sKey = key.toString();
        return sKey.length() == 0 || value.toString().toLowerCase().startsWith(sKey);
    }
}
