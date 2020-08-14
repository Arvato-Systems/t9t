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
package com.arvatosystems.t9t.authc.be.api;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.authc.api.GetMultipleUserDataResponse;
import com.arvatosystems.t9t.authc.api.GetUserDataByUserIdsRequest;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.services.RequestContext;

public class GetUserDataByUserIdsRequestHandler extends AbstractGetUserDataRequestHandler<GetUserDataByUserIdsRequest> {

    @Override
    public GetMultipleUserDataResponse execute(RequestContext ctx, GetUserDataByUserIdsRequest rq) {
        final List<UserData> userData = new ArrayList<>(rq.getUserIds().size());
        for (String userId: rq.getUserIds()) {
            userData.add(responseFromDto(resolver.getDTO(new UserKey(userId))));
        }
        return new GetMultipleUserDataResponse(0, userData);
    }
}
