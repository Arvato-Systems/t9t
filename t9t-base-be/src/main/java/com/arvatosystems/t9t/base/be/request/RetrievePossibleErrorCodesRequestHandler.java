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
