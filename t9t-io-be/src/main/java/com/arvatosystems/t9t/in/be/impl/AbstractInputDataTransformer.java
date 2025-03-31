/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

import java.util.Collections;
import java.util.Map;

import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;

/**
 * Superclass for IInputDataTransformers, which transform BonaPortables into import requests (second step of conversion).
 * The main task of this class is to store the key parameters passed to the open() method.
 *
 * @param <T> The type of the intermediate object (the object passed into this transformer)
 */
public abstract class AbstractInputDataTransformer<T extends BonaPortable> implements IInputDataTransformer<T> {
    protected IInputSession inputSession;
    protected Map<String, Object> params;
    protected BonaPortableClass<?> baseBClass;
    protected DataSinkDTO importDataSinkDTO;

    @Override
    public void open(final IInputSession newInputSession, final Map<String, Object> newParams, final BonaPortableClass<?> newBaseBClass) {
        this.inputSession = newInputSession;
        this.params = newParams != null ? newParams : Collections.emptyMap();
        this.baseBClass = newBaseBClass;
        this.importDataSinkDTO = inputSession.getDataSinkDTO();
    }
}
