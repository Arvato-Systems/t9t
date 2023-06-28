/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.embedded.tests.apikey;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.UserKey;
import com.arvatosystems.t9t.auth.extensions.AuthExtensions;
import com.arvatosystems.t9t.auth.request.SessionSearchRequest;
import com.arvatosystems.t9t.auth.request.UserCrudAndSetPasswordRequest;
import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;

import de.jpaw.bonaparte.api.SearchFilters;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class ApikeyTest {
    private static final String USER_ID = "JohnDoe73";

    @Disabled  // no tenant restriction on session
    @Test
    public void createUserAndApiKeyTest() throws Exception {
        // connect
        final ITestConnection dlg = new InMemoryConnection();

        // setup
        final UserDTO user = new UserDTO();
        user.setUserId(USER_ID);
        user.setName("John Doe");
        user.setIsActive(true);
        user.validate();
        final UserCrudAndSetPasswordRequest rq = new UserCrudAndSetPasswordRequest();
        rq.setCrud(OperationType.MERGE);
        rq.setData(user);
        rq.setNaturalKey(new UserKey(user.getUserId()));
        rq.setPassword("predefined");
        rq.validate();
        dlg.okIO(rq);
        final PermissionsDTO perms = new PermissionsDTO();
        perms.setMinPermissions(new Permissionset(0xfffff));
        perms.setMaxPermissions(new Permissionset(0xfffff));
        perms.setResourceIsWildcard(true);
        perms.setResourceRestriction("B.");
        final UUID theKey = UUID.randomUUID();
        final ApiKeyDTO apiKey = new ApiKeyDTO();
        apiKey.setApiKey(theKey);
        apiKey.setUserRef(new UserKey(USER_ID));
        apiKey.setIsActive(true);
        apiKey.setName("key for embedded test class ApikeyTest");
        apiKey.setPermissions(perms);
        final CrudSurrogateKeyResponse<ApiKeyDTO, FullTrackingWithVersion> crudResp = AuthExtensions.merge(apiKey, dlg);

        // test
        dlg.switchUser(theKey);

        // validate
        final SessionSearchRequest srq = new SessionSearchRequest();
        srq.setSortColumns(List.of(new SortColumn("cTimestamp", true)));
        srq.setSearchFilter(SearchFilters.equalsFilter("apiKeyRef", crudResp.getKey()));
        final ReadAllResponse<SessionDTO, NoTracking> rars = dlg.typeIO(srq, ReadAllResponse.class);
        Assertions.assertFalse(rars.getDataList().isEmpty());
        final SessionDTO session = rars.getDataList().get(0).getData();
        Assertions.assertNotNull(session.getApiKeyRef(), "Expected an API key reference");
    }
}
