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

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.meta.Multiplicity;

public abstract class AbstractCoreDataField<E extends Component, T> implements IDataField<E, T> {
    protected final FieldDefinition cfg;
    protected final String label;
    protected final String path;
    protected final ApplicationSession as;
    protected final boolean isRequired;

    protected AbstractCoreDataField(DataFieldParameters params) {
        this.cfg = params.cfg;
        this.path = params.path;
        this.as = params.as;
        this.label = ZulUtils.i18nLabel(path);
        isRequired = params.overrideRequired != null ? params.overrideRequired.booleanValue() : cfg.getIsRequired() && cfg.getMultiplicity() == Multiplicity.SCALAR;
    }

    @Override
    public String getFieldName() {
        return path;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public FieldDefinition getFieldDefintion() {
        return cfg;
    }

    @Override
    public String getConverter() {
        return null;
    }

    @Override
    public Map<String,Object> getConverterArgs() {
        return null;
    }

    @Override
    public boolean getIsRequired() {
        return isRequired;
    }
}
