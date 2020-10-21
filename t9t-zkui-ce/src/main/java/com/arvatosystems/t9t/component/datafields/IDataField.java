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
package com.arvatosystems.t9t.component.datafields;

import java.util.Map;

import org.zkoss.zk.ui.Component;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;

/** Interface for the dynamically created fields. */
public interface IDataField<E extends Component, T> {
    /** Returns a translated label for the field. */
    String getLabel();

    /** Returns the field path. */
    String getFieldName();

    /** Returns the metadata. */
    FieldDefinition getFieldDefintion();

    /** Returns the ZK component(s) associated with the field. These are 2 in case of range filters. */
    E getComponent();

    /** Returns the ZK converter expression, or null if none is required. */
    String getConverter();

    /** Returns the ZK converter args, or null if none are required. */
    Map<String,Object> getConverterArgs();

    /** Clears the component's current value. */
    void clear();

    /** Returns if the field is required (must be not null). */
    boolean getIsRequired();

    /** Returns if the field is currently unset. */
    boolean empty();

    T getValue();

    void setValue(T data);

    /** unified entry to Checkbox and InputElements. */
    void setDisabled(boolean disabled);
}
