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

import com.arvatosystems.t9t.out.be.ISpecificTranslationProvider;

import de.jpaw.bonaparte.core.DelegatingBaseComposer;
import de.jpaw.bonaparte.core.MessageComposer;
import de.jpaw.bonaparte.enums.BonaTokenizableEnum;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.bonaparte.pojos.meta.EnumDataItem;

public class EnumTranslatorComposerFilter<E extends Exception> extends DelegatingBaseComposer<E> {
    private final ISpecificTranslationProvider translator;

    public EnumTranslatorComposerFilter(MessageComposer<E> delegateComposer, ISpecificTranslationProvider translator) {
        super(delegateComposer);
        this.translator = translator;
    }

    // enums replaced by the internal token
    /*
    @Override
    public void addEnum(EnumDataItem di, BasicNumericElementaryDataItem ord, BonaNonTokenizableEnum<?> n) throws E {
        delegateComposer.addField(StaticMeta.ENUM_TOKEN, n == null ? null : n.toString());
    }
     */

    // enum with alphanumeric expansion: delegate to Null/String
    // use the existing token meta here, because the name is better, depsite the length may be too short
    @Override
    public void addEnum(EnumDataItem di, AlphanumericElementaryDataItem token, BonaTokenizableEnum n) throws E {
        delegateComposer.addField(token, n == null ? null : translator.translateEnum(di, n));
    }

}
