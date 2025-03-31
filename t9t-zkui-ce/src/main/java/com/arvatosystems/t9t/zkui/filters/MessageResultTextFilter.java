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
package com.arvatosystems.t9t.zkui.filters;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.msglog.MessageDTO;
import com.arvatosystems.t9t.msglog.request.RetrieveParametersRequest;
import com.arvatosystems.t9t.msglog.request.RetrieveParametersResponse;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

@Singleton
@Named("message")
public class MessageResultTextFilter implements IResultTextFilter<DataWithTracking<MessageDTO, NoTracking>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageResultTextFilter.class);

    protected final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    @Override
    public Predicate<DataWithTracking<MessageDTO, NoTracking>> getFilter(@Nonnull final String filterText) {
        return dwt -> {
            if (T9tUtil.isBlank(filterText)) {
                return false;
            }
            final MessageDTO dto = dwt.getData();
            try {
                final RetrieveParametersRequest req = new RetrieveParametersRequest();
                req.setProcessRef(dto.getObjectRef());
                req.setRequestParameters(true);
                req.setServiceResponse(false);
                final RetrieveParametersResponse resp = remoteUtils.executeAndHandle(req, RetrieveParametersResponse.class);
                if (resp.getRequestParameters() != null) {
                    final String json = JsonComposer.toJsonString(resp.getRequestParameters());
                    return json.contains(filterText);
                }
            } catch (Exception e) {
                LOGGER.error("Error while applying filter. Error: {}", e.getMessage());
            }
            return false;
        };
    }
}
