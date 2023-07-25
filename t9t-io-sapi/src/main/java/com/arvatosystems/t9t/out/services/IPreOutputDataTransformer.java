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
package com.arvatosystems.t9t.out.services;

import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.services.IDataSinkDefaultConfigurationProvider;

import de.jpaw.bonaparte.core.BonaPortable;
import jakarta.annotation.Nonnull;

/**
 * An {@linkplain IOutputSession} hook which allow transformation of output data before it gets stored.
 */
@FunctionalInterface
public interface IPreOutputDataTransformer extends IDataSinkDefaultConfigurationProvider {

    /**
     * Returns fixed header data.
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return header data
     * @throws T9tException if there is an issue creating header data
     */
    default List<BonaPortable> headerData(@Nonnull final DataSinkDTO sinkCfg, @Nonnull final OutputSessionParameters outputSessionParameters) {
        return null;
    }

    /**
     * Transformed a record into possible N transformed record.
     * @param record the record
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return transformed records (can be an empty list, but never null)
     * @throws T9tException if there is an issue transforming record data
     */
    @Nonnull
    List<BonaPortable> transformData(@Nonnull BonaPortable record, @Nonnull DataSinkDTO sinkCfg, @Nonnull OutputSessionParameters outputSessionParameters);

    /**
     * Returns fixed footer data.
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return footer data
     * @throws T9tException if there is an issue creating footer data
     */
    default List<BonaPortable> footerData(@Nonnull final DataSinkDTO sinkCfg, @Nonnull final OutputSessionParameters outputSessionParameters) {
        return null;
    }

    /**
     * Returns selection criteria and/or header data.
     *
     * @param sinkCfg the data sink configuration
     * @return a data structure with specific output directives
     */
    default FoldableParams getFoldableParams(@Nonnull final DataSinkDTO sinkCfg) {
        return null;
    }
}
