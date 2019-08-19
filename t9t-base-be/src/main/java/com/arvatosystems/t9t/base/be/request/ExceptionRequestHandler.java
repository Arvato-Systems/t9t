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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.ExceptionRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;

public class ExceptionRequestHandler extends AbstractReadOnlyRequestHandler<ExceptionRequest> {

    @Override
    public ServiceResponse execute(ExceptionRequest errorRequest) {
        final int code = errorRequest.getReturnCode();
        final String message = errorRequest.getErrorMessage();
        if (code > 0) {
            if (message == null)
                throw new T9tException(code);
            else
                throw new T9tException(code, message);
        } else {
            if (message == null)
                throw new RuntimeException();
            else
                throw new RuntimeException(message);
        }
    }
}
