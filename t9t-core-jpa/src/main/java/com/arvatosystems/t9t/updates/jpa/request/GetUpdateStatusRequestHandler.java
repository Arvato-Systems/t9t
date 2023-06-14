package com.arvatosystems.t9t.updates.jpa.request;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.updates.UpdateStatusDTO;
import com.arvatosystems.t9t.updates.UpdateStatusTicketKey;
import com.arvatosystems.t9t.updates.jpa.mapping.IUpdateStatusDTOMapper;
import com.arvatosystems.t9t.updates.jpa.persistence.IUpdateStatusEntityResolver;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusRequest;
import com.arvatosystems.t9t.updates.request.GetUpdateStatusResponse;
import de.jpaw.dp.Jdp;

public class GetUpdateStatusRequestHandler extends AbstractRequestHandler<GetUpdateStatusRequest> {
    protected final IUpdateStatusDTOMapper mapper = Jdp.getRequired(IUpdateStatusDTOMapper.class);
    protected final IUpdateStatusEntityResolver resolver = Jdp.getRequired(IUpdateStatusEntityResolver.class);

    @Override
    public GetUpdateStatusResponse execute(final RequestContext ctx, final GetUpdateStatusRequest request) {
        final UpdateStatusDTO updateStatus = mapper.mapToDto(resolver.getEntityData(new UpdateStatusTicketKey(request.getTicketId()), true));
        final GetUpdateStatusResponse response = new GetUpdateStatusResponse();
        response.setUpdateStatus(updateStatus);
        return response;
    }
}
