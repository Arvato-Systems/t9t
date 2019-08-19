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
package com.arvatosystems.t9t.out.be.impl;

import java.util.Collections;
import java.util.List;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.be.IPreOutputDataTransformer;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * Implementation of {@linkplain IInputSession} which doesn't transform data.
 *
 * @author LIEE001
 */
public class OutputDataTransformerIdentity implements IPreOutputDataTransformer {

    public static final IPreOutputDataTransformer INSTANCE = new OutputDataTransformerIdentity();

    private OutputDataTransformerIdentity() {
        // prevent instantiation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BonaPortable> transformData(final BonaPortable record, final DataSinkDTO sinkCfg, final OutputSessionParameters outputSessionParameters) {
        return Collections.singletonList(record);
    }
}
