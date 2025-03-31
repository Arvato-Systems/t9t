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
package com.arvatosystems.t9t.ai.tools.impl;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.ai.service.IAiTool;
import com.arvatosystems.t9t.ai.tools.AiToolUserList;
import com.arvatosystems.t9t.ai.tools.AiToolUserListResult;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.request.UserSearchRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named(AiToolUserList.my$PQON)
@Singleton
public class AiToolUserSearch implements IAiTool<AiToolUserList, AiToolUserListResult> {

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public AiToolUserListResult performToolCall(final RequestContext ctx, final AiToolUserList request) {
        final UserSearchRequest searchRq = new UserSearchRequest();
        if (request.getName() != null) {
            final UnicodeFilter filter = new UnicodeFilter("name");
            filter.setLikeValue("%" + request.getName() + "%");
            searchRq.setSearchFilter(filter);
        }
        searchRq.setLimit(20);  // no more than 20 results for now
        final ReadAllResponse<UserDTO, FullTrackingWithVersion> response = executor.executeSynchronousAndCheckResult(ctx, searchRq, ReadAllResponse.class);
        // transfer the result, we only want the DTOs, no tracking data
        final List<UserDTO> resultList = new ArrayList<>(response.getDataList().size());
        for (final DataWithTrackingS<UserDTO, FullTrackingWithVersion> dwt: response.getDataList()) {
            resultList.add(dwt.getData());
        }
        final AiToolUserListResult result = new AiToolUserListResult();
        result.setUsers(resultList);
        return result;
    }
}
