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

import com.arvatosystems.t9t.components.Permissions28;

import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class PermissionsetDataField extends AbstractCoreDataField<Permissions28, Permissionset> {
    protected final Permissions28 c = new Permissions28();

    @Override
    public boolean empty() {
        return c.getValue() == null;
    }

    public PermissionsetDataField(DataFieldParameters params) {
        super(params);
    }

    @Override
    public void clear() {
        c.setValue(null);
    }

    @Override
    public Permissions28 getComponent() {
        return c;
    }

    @Override
    public Permissionset getValue() {
        return c.getValue();
    }

    @Override
    public void setValue(Permissionset data) {
        c.setValue(data);
    }

    @Override
    public void setDisabled(boolean disabled) {
        // getComponent().setDisabled(disabled);  // TODO
    }
}
