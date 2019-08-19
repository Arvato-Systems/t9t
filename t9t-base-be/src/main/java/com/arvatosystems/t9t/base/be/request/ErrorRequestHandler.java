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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.ErrorRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;

/**
 * A technical request handler which is used to pass exceptions thrown in outer transport layers through appropriate database logging and response message
 * translation.
 */
public class ErrorRequestHandler extends AbstractReadOnlyRequestHandler<ErrorRequest> {

    @Override
    public ServiceResponse execute(ErrorRequest errorRequest) {
        return error(errorRequest.getReturnCode(), errorRequest.getErrorDetails());
    }

}
