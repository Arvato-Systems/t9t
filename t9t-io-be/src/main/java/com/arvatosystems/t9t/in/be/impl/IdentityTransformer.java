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

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.request.ErrorRequest;
import com.arvatosystems.t9t.io.T9tIOException;

import de.jpaw.bonaparte.core.BonaPortable;

/** Implementation which just validates that the received data is of type RequestParameters. */
public class IdentityTransformer extends AbstractInputDataTransformer<BonaPortable> {
    @Override
    public RequestParameters transform(final BonaPortable dto) {
        if (baseBClass != null && baseBClass.getBonaPortableClass().isAssignableFrom(dto.getClass())) {
            // as no transformer exists, any parser object type is also a processor object type
            final ErrorRequest req = new ErrorRequest();
            req.setReturnCode(T9tIOException.WRONG_RECORD_TYPE);
            req.setErrorDetails(baseBClass.getClass().getSimpleName());
            return req;
        }
        if (dto instanceof RequestParameters rp) {
            return rp;
        }
        final ErrorRequest req = new ErrorRequest();
        req.setReturnCode(T9tIOException.WRONG_RECORD_TYPE);
        req.setErrorDetails("RequestParameters");
        return req;
    }
}
