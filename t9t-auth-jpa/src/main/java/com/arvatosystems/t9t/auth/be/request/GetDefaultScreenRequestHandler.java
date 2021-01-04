package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.GetDefaultScreenRequest;
import com.arvatosystems.t9t.auth.request.GetDefaultScreenResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class GetDefaultScreenRequestHandler extends AbstractRequestHandler<GetDefaultScreenRequest> {

    protected final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);

    @Override
    public GetDefaultScreenResponse execute(RequestContext ctx, GetDefaultScreenRequest request) throws Exception {
        UserEntity userEntity = resolver.find(ctx.userRef);
        GetDefaultScreenResponse response = new GetDefaultScreenResponse();
        response.setDefaultScreenId(userEntity.getDefaultScreenId());
        return response;
    }
}
