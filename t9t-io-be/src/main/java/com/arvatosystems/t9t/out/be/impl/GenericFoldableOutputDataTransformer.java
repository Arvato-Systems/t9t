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
package com.arvatosystems.t9t.out.be.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.out.services.FoldableParams;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

@Dependent
@Named("defaultFoldableTransformer")
public final class GenericFoldableOutputDataTransformer implements IPreOutputDataTransformer {

    @Override
    public FoldableParams getFoldableParams(final DataSinkDTO sinkCfg) {
        final List<String> fieldNames;
        if (sinkCfg.getGenericParameter1() != null) {
            // assume comma separated list of fields
            final String[] fields = sinkCfg.getGenericParameter1().split(",");
            fieldNames = Arrays.asList(fields);
        } else {
            // obtain full list of fields from the specific PQON
            final BonaPortableClass<?> baseBClass = BonaPortableFactory.getBClassForPqon(sinkCfg.getBaseClassPqon());
            fieldNames = new ArrayList<>(64);
            for (final FieldDefinition fd: baseBClass.getMetaData().getFields()) {
                fieldNames.add(fd.getName());
            }
        }
        return new FoldableParams(null, fieldNames, null, null, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<BonaPortable> transformData(final BonaPortable record, final DataSinkDTO sinkCfg, final OutputSessionParameters outputSessionParameters) {
        return Collections.singletonList(record);
    }
}
