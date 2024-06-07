package com.arvatosystems.t9t.ai.tools.impl;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.ai.service.IAITool;
import com.arvatosystems.t9t.ai.tools.AIToolUserList;
import com.arvatosystems.t9t.ai.tools.AIToolUserListResult;
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

@Named(AIToolUserList.my$PQON)
@Singleton
public class AIToolUserSearch implements IAITool<AIToolUserList, AIToolUserListResult> {

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public AIToolUserListResult performToolCall(final RequestContext ctx, final AIToolUserList request) {
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
        final AIToolUserListResult result = new AIToolUserListResult();
        result.setUsers(resultList);
        return result;
    }
}
