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
package com.arvatosystems.t9t.out.be.impl.output;

import com.arvatosystems.t9t.base.misc.Variant;

import de.jpaw.bonaparte.core.BonaCustom;
import de.jpaw.bonaparte.core.DelegatingBaseComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

/**
 * Composer which replaces the contents of a base.api.Variant object by its non-null value. Used for OMS typesafe history.
 */
public class VariantComposerFilter<E extends Exception> extends DelegatingBaseComposer<E> {

    public VariantComposerFilter(MessageComposer<E> delegateComposer) {
        super(delegateComposer);
    }

    @Override
    public void addField(ObjectReference di, BonaCustom obj) throws E {
        if (!(obj instanceof Variant)) {
            super.addField(di, obj);
        } else {
            Variant v = (Variant)obj;
            if (v.getTextValue() != null) {
                addField(Variant.meta$$textValue, v.getTextValue());
            } else if (v.getBoolValue() != null) {
                addField(Variant.meta$$boolValue, v.getBoolValue());
            } else if (v.getIntValue() != null) {
                addField(Variant.meta$$intValue, v.getIntValue());
            } else if (v.getLongValue() != null) {
                addField(Variant.meta$$longValue, v.getLongValue());
            } else if (v.getNumValue() != null) {
                addField(Variant.meta$$numValue, v.getNumValue());
            } else if (v.getDayValue() != null) {
                addField(Variant.meta$$dayValue, v.getDayValue());
            } else if (v.getInstantValue() != null) {
                addField(Variant.meta$$instantValue, v.getInstantValue());
            } else if (v.getEnumValue() != null) {
                addField(Variant.meta$$enumValue, v.getEnumValue());
            } else {
                writeNull(di);
            }
        }
    }

}
