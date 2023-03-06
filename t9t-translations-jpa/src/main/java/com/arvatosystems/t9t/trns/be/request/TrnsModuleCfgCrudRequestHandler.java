package com.arvatosystems.t9t.trns.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudModuleCfgRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.trns.TrnsModuleCfgDTO;
import com.arvatosystems.t9t.trns.jpa.entities.TrnsModuleCfgEntity;
import com.arvatosystems.t9t.trns.jpa.mapping.ITrnsModuleCfgDTOMapper;
import com.arvatosystems.t9t.trns.jpa.persistence.ITrnsModuleCfgEntityResolver;
import com.arvatosystems.t9t.trns.request.TrnsModuleCfgCrudRequest;

import de.jpaw.dp.Jdp;

public class TrnsModuleCfgCrudRequestHandler extends AbstractCrudModuleCfgRequestHandler<TrnsModuleCfgDTO, TrnsModuleCfgCrudRequest, TrnsModuleCfgEntity> {

    private final ITrnsModuleCfgDTOMapper mapper = Jdp.getRequired(ITrnsModuleCfgDTOMapper.class);
    private final ITrnsModuleCfgEntityResolver resolver = Jdp.getRequired(ITrnsModuleCfgEntityResolver.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, TrnsModuleCfgCrudRequest request) throws Exception {
        return execute(ctx, mapper, resolver, request);
    }
}
