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
package com.arvatosystems.t9t.base.be.request;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.base.request.ErrorDescription;
import com.arvatosystems.t9t.base.request.RetrievePossibleErrorCodesRequest;
import com.arvatosystems.t9t.base.request.RetrievePossibleErrorCodesResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ApplicationException.ExceptionRangeDescription;

public class RetrievePossibleErrorCodesRequestHandler extends AbstractReadOnlyRequestHandler<RetrievePossibleErrorCodesRequest> {

    // because we are returning internals of the application, this request requires ADMIN permissions.
    @Override
    public OperationType getAdditionalRequiredPermission(final RetrievePossibleErrorCodesRequest request) {
        return OperationType.ADMIN;
    }

    @Override
    public RetrievePossibleErrorCodesResponse execute(final RequestContext ctx, final RetrievePossibleErrorCodesRequest request) {
        final List<ErrorDescription> descriptions = new ArrayList<>(ApplicationException.getNumberOfErrorCodes());
        ApplicationException.forEachCode((returnCode, text) -> {
            final ErrorDescription description = new ErrorDescription(returnCode, text);
            final ExceptionRangeDescription rangeDescription = ApplicationException.getRangeInfoForExceptionCode(returnCode);
            if (rangeDescription != null) {
                description.setApplicationLevel(rangeDescription.layer().name());
                description.setModuleDescription(rangeDescription.description());
            }
            descriptions.add(description);
        });
        final RetrievePossibleErrorCodesResponse resp = new RetrievePossibleErrorCodesResponse();
        resp.setErrorDescriptions(descriptions);
        return resp;
    }
}
