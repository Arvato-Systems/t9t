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
package com.arvatosystems.t9t.component.fields;

import java.util.List;

import org.zkoss.zk.ui.Component;

import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

/** Interface for the dynamically created fields. */
public interface IField<E extends Component> {
    /** Returns a translated label for the field. */
    String getLabel();

    /** Returns the field path. */
    String getFieldName();

    /** Returns the filter type. */
    UIFilterType getFilterType();

    /** Returns the ZK component(s) associated with the field. These are 2 in case of range filters. */
    List<E> getComponents();

    /** Clears the component's current value. */
    void clear();

    /** Returns if the field(s) are currently unset. */
    boolean empty();

    /** Creates a search filter from the component's current value, or returns null if not appliable.
     * This is the raw filter, not respecting any negation. */
    SearchFilter getSearchFilter();

    /** Returns if the field filter uses inverse logic. */
    boolean isNegated();
}
