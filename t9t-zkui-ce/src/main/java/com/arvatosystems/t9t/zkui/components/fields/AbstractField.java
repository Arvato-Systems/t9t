/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.components.fields;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.impl.InputElement;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

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
        this.isNegated = T9tUtil.isTrue(cfg.getNegate());
    }

    @Override
    public String getFieldName() {
        return fieldname;
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
        c.setPlaceholder(suffix.length() == 0 ? label : translateFromOrTo(suffix, label));
        components.add(c);
    }

    /** Translate a label for "From" or "To". */
    private String translateFromOrTo(String suffix, String labelWithoutSuffix) {
        // return session.translate(gridId, fieldname + suffix);
        final String pattern = session.translate(null, suffix);
        if (pattern.indexOf('#') < 0) {
            // just a text: assume the translation is a suffix
            return labelWithoutSuffix + " " + pattern;
        }
        // with #: replace it at flexible position
        return pattern.replace("#", labelWithoutSuffix);
    }

    protected void createComponents() {
        switch (cfg.getFilterType()) {
        case RANGE:
            createComponentSub("From");
            createComponentSub("To");
            break;
        case LOWER_BOUND:
            createComponentSub("From");
            break;
        case UPPER_BOUND:
            createComponentSub("To");
            break;
        case LIKE:
            createComponentSub("Like");
            break;
        default:
            createComponentSub("");
        }
    }

    protected abstract boolean componentEmpty(E c);

    @Override
    public boolean empty() {
        for (E e : components) {
            if (!componentEmpty(e))
                return false;
        }
        return true;
    }

    protected void noLikeFilter() {
        LOGGER.error("A like filter is not supported for field type {}, treating as equals: {}",
                getClass().getSimpleName(), getFieldName());
    }
}
