/*
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

import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.jpa.impl.AbstractSearch42RequestHandler;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.msglog.MessageStatisticsDTO;
import com.arvatosystems.t9t.msglog.jpa.entities.MessageStatisticsEntity;
import com.arvatosystems.t9t.msglog.jpa.mapping.IMessageStatisticsDTOMapper;
import com.arvatosystems.t9t.msglog.jpa.persistence.IMessageStatisticsEntityResolver;
import com.arvatosystems.t9t.msglog.request.MessageStatisticsSearchRequest;

import de.jpaw.dp.Jdp;

public class MessageStatisticsSearchRequestHandler extends AbstractSearch42RequestHandler<Long, MessageStatisticsDTO,
  WriteTracking, MessageStatisticsSearchRequest, MessageStatisticsEntity> {

    protected final IMessageStatisticsEntityResolver resolver = Jdp.getRequired(IMessageStatisticsEntityResolver.class);
    protected final IMessageStatisticsDTOMapper mapper = Jdp.getRequired(IMessageStatisticsDTOMapper.class);

    @Override
    public ReadAllResponse<MessageStatisticsDTO, WriteTracking>
      execute(final RequestContext ctx, final MessageStatisticsSearchRequest request) throws Exception {
        return execute(ctx, request, resolver, mapper);
    }
}
