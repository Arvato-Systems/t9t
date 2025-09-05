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

public class SimpleListModelFullList<E> extends SimpleListModel<E> {

    private static final long serialVersionUID = -4934843732616895559L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleListModelFullList.class);

    public SimpleListModelFullList(final List<E> data) {
        super(data);
        LOGGER.debug("created SimpleListModelFullList for {} entries", data.size());
    }

    /**
     * Always returns the full model, ignoring filtering and nRows.
     */
    @Override
    public ListModel<E> getSubModel(final Object value, final int nRows) {
        LOGGER.debug("getSubModel called, returning full list (ignoring filter and nRows)");
        return this;
    }
}
