package com.arvatosystems.t9t.auth.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.request.SetDefaultScreenRequest;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class SetDefaultScreenRequestHandler extends AbstractRequestHandler<SetDefaultScreenRequest> {

    protected final IUserEntityResolver resolver = Jdp.getRequired(IUserEntityResolver.class);
    private final Logger LOGGER = LoggerFactory.getLogger(SetDefaultScreenRequestHandler.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, SetDefaultScreenRequest request) throws Exception {
        UserEntity userEntity = resolver.find(ctx.userRef);
        userEntity.setDefaultScreenId(request.getDefaultScreenId());
        LOGGER.info("set default screen as {} to user {}", request.getDefaultScreenId(), userEntity.getObjectRef());
        return ok();
    }
}
