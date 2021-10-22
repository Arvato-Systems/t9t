/**
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.msglog.jpa.request;

import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageEntity;
import com.arvatosystems.t9t.msglog.jpa.mapping.IMessageDTOMapper;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageEntityResolver;
import com.arvatosystems.t9t.msglog.request.MessageSearchRequest;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.dp.Jdp;

public class MessageSearchRequestHandler extends AbstractSearch42RequestHandler<Long, MessageDTO, NoTracking, MessageSearchRequest, MessageEntity> {

    protected final IMessageEntityResolver resolver = Jdp.getRequired(IMessageEntityResolver.class);
    protected final IMessageDTOMapper mapper = Jdp.getRequired(IMessageDTOMapper.class);

    @Override
    public ReadAllResponse<MessageDTO, NoTracking> execute(final RequestContext ctx, final MessageSearchRequest request)
            throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
