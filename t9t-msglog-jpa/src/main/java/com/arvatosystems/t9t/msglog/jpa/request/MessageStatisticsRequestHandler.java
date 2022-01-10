/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
    public ServiceResponse execute(final RequestContext ctx, final MessageStatisticsSearchRequest request) throws Exception {
        return mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());
    }
}
