package com.arvatosystems.t9t.msglog.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.jpa.mapping.IMessageStatisticsDTOMapper;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.msglog.request.MessageStatisticsSearchRequest;

import de.jpaw.dp.Jdp;

public class MessageStatisticsRequestHandler extends AbstractSearchRequestHandler<MessageStatisticsSearchRequest> {
    protected final IMessageStatisticsEntityResolver resolver = Jdp.getRequired(IMessageStatisticsEntityResolver.class);
    protected final IMessageStatisticsDTOMapper mapper = Jdp.getRequired(IMessageStatisticsDTOMapper.class);
    
    @Override
    public ServiceResponse execute(RequestContext ctx, MessageStatisticsSearchRequest request) throws Exception {
        return mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());
    }
}
