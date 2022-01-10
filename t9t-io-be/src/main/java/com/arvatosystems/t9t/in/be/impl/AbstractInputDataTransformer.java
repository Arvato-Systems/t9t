/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.be.impl;

import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.in.services.IInputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;

import java.util.Map;

public abstract class AbstractInputDataTransformer<T extends BonaPortable> implements IInputDataTransformer<T> {
    protected IInputSession inputSession;

    protected BonaPortableClass<?> baseBClass;

    @Override
    public void open(final IInputSession newInputSession, final Map<String, Object> params, final BonaPortableClass<?> newBaseBClass) {
        this.inputSession = newInputSession;
        this.baseBClass = newBaseBClass;
    }
}
