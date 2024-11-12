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
package com.arvatosystems.t9t.base.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.BatchRequest;
import com.arvatosystems.t9t.base.request.ExceptionRequest;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class ExceptionRequestHandler extends AbstractReadOnlyRequestHandler<ExceptionRequest> {

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ExceptionRequest errorRequest) throws Exception {
        // case 1: If we have been passed a special code, throw those!
        if (errorRequest.getSpecialCause() != null) {
            final ServiceResponse response = new ServiceResponse();
            switch (errorRequest.getSpecialCause()) {
            case "NPE":
                response.setReturnCode(errorRequest.getErrorMessage().length());  // will product an NPE
                break;
            case "CLASSCAST":
                final RequestParameters rp = errorRequest;
                final BatchRequest br = (BatchRequest)rp;  // will product a ClassCastException;
                response.setReturnCode(br.getAllowNo() ? 1 : 2);
                break;
            }
            return response;
        }
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
