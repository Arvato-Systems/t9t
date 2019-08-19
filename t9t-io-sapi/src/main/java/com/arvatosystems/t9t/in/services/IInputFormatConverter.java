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
package com.arvatosystems.t9t.in.services;

import java.io.InputStream;
import java.util.Map;

import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.server.services.IStatefulServiceSession;

import de.jpaw.bonaparte.core.BonaPortableClass;

// implementations must be thread safe for method transform
@FunctionalInterface
public interface IInputFormatConverter {
    /** Performs any optional initial output. */
    default void open(IInputSession inputSession, DataSinkDTO cfg, IStatefulServiceSession session, Map<String, Object> params,
            BonaPortableClass<?> baseBClass) {
        // no action required in simple cases
    }

    /** Processes a whole stream of data. Should invoke inutSession.setHeaderData() per header field and inputSession.process(BonaPortable)
     * per data record. */
    void process(InputStream is);

    /** Ends processing, writes a summary into the sink table. */
    default void close() {
        // no action required in simple cases
    }
}
