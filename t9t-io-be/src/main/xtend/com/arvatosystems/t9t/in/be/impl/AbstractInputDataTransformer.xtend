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
package com.arvatosystems.t9t.in.be.impl

import com.arvatosystems.t9t.in.services.IInputDataTransformer
import com.arvatosystems.t9t.in.services.IInputSession
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.server.services.IStatefulServiceSession
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.BonaPortableClass
import java.util.Map

abstract class AbstractInputDataTransformer<T extends BonaPortable> implements IInputDataTransformer<T> {
    protected IInputSession inputSession;
    protected DataSinkDTO   cfg;
    protected BonaPortableClass<?> baseBClass

    override void open(IInputSession inputSession, DataSinkDTO sinkCfg, IStatefulServiceSession session, Map<String, Object> params, BonaPortableClass<?> baseBClass) {
        this.inputSession    = inputSession
        this.cfg             = sinkCfg
        this.baseBClass      = baseBClass
    }
}
