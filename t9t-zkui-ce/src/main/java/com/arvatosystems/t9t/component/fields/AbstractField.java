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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.base.BooleanUtil;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;
import de.jpaw.bonaparte.pojos.ui.UIFilterType;

public abstract class AbstractField<E extends InputElement> implements IField {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractField.class);
    protected final UIFilter cfg;
    protected String label;
    protected final String fieldname;
    protected final FieldDefinition desc;
    protected final List<E> components = new ArrayList<E>(2);
    protected final ApplicationSession session;
    protected final String gridId;
    protected final boolean isNegated;

    protected AbstractField(String fieldname, UIFilter cfg, FieldDefinition desc, String gridId, ApplicationSession session) {
        this.fieldname = fieldname;
        this.cfg = cfg;
        this.desc = desc;
        this.label = session.translate(gridId, fieldname);
        this.gridId = gridId;
        this.session = session;
        this.isNegated = BooleanUtil.isTrue(cfg.getNegate());
    }

    @Override
    public String getFieldName() {
        return fieldname;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public List<E> getComponents() {
        return components;
    }

    @Override
    public UIFilterType getFilterType() {
        return cfg.getFilterType();
    }

    @Override
    public boolean isNegated() {
        return this.isNegated;
    }

    protected abstract E createComponent(String suffix);

    private void createComponentSub(String suffix) {
        E c = createComponent(suffix);
        c.setId(cfg.getFieldName() + suffix);
        c.setHflex("1");
        c.setPlaceholder(suffix == null || suffix.length() == 0 ? label : session.translate(gridId, fieldname + suffix));
        components.add(c);
    }

    protected void createComponents() {
        if (cfg.getFilterType() == UIFilterType.RANGE) {
            createComponentSub("From");
            createComponentSub("To");
        } else {
            createComponentSub("");
        }
    }

    protected abstract boolean componentEmpty(E c);

    @Override
    public boolean empty() {
        for (E e : components)
            if (!componentEmpty(e))
                return false;
        return true;
    }

    protected void noLikeFilter() {
        LOGGER.error("A like filter is not supported for field type {}, treating as equals: {}",
                getClass().getSimpleName(), getFieldName());
    }
}
