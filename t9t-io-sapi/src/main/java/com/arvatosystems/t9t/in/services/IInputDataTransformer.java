/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.in.services;

import java.util.Map;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.services.IDataSinkDefaultConfigurationProvider;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;

@FunctionalInterface
public interface IInputDataTransformer<T extends BonaPortable> extends IDataSinkDefaultConfigurationProvider {

    /** Performs any optional initial output. */
    default void open(IInputSession inputSession, Map<String, Object> params, BonaPortableClass<?> baseBClass) {
        // no action required in simple cases
    }

    /**
     * Converts a raw input record into a request. Should not throw exceptions but rather return an "ErrorRequest" if input data is not valid.
     * Can return null, if no current action should be performed. */
    RequestParameters transform(T dto);

    /** Returns any request which was postponed for buffering purposes before. */
    default RequestParameters getPending() { return null; }

    /** Ends processing, writes a summary into the sink table. */
    default void close() {
        // no action required in simple cases
    }
}
