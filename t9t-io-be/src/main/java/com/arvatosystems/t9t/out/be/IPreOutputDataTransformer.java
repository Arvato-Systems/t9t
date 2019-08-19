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
package com.arvatosystems.t9t.out.be;

import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * An {@linkplain IOutputSession} hook which allow transformation of output data before it gets stored.
 *
 * @author LIEE001
 */
@FunctionalInterface
public interface IPreOutputDataTransformer {

    /**
     * Returns fixed header data.
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return header data
     * @throws T9tException if there is an issue creating header data
     */
    default List<BonaPortable> headerData(DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters) {
        return null;
    }

    /**
     * Transformed a record into possible N transformed record.
     * @param record the record
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return transformed records
     * @throws T9tException if there is an issue transforming record data
     */
    List<BonaPortable> transformData(BonaPortable record, DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters);

    /**
     * Returns fixed footer data.
     * @param sinkCfg data sink configuration
     * @param outputSessionParameters output session parameters
     * @return footer data
     * @throws T9tException if there is an issue creating footer data
     */
    default List<BonaPortable> footerData(DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters) {
        return null;
    }
}
