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

import org.zkoss.zk.ui.Component;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;

/** Interface which must be implemented by custom filters. It is a factory for IField instances.
 * The actual filter usually will be implemented as a (probably static local) subclass of AbstractField.
 *
 * @param <E>
 */
public interface IFieldCustomFactory<E extends Component> {
    IField<E> createField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) throws Exception;
}
