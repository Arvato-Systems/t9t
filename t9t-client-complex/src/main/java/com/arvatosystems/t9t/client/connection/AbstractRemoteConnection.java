/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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
package com.arvatosystems.t9t.client.connection;

import java.util.List;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.request.BatchRequest;

// sole purpose is to keep project t9t-client-complex Java 6 compatible
public abstract class AbstractRemoteConnection implements IRemoteConnection {
    @Override
    public ServiceResponse execute(String authenticationHeader, List<RequestParameters> rpList) {
        if (rpList.isEmpty()) {
            // should be configurable, we might want to see them in the logs...
            return new ServiceResponse();
        }
        if (rpList.size() == 1) {
            return execute(authenticationHeader, rpList.get(0));
        } else {
            BatchRequest batchRequest = new BatchRequest();
            batchRequest.setAllowNo(false);
            batchRequest.setCommands(rpList);
            return execute(authenticationHeader, batchRequest);
        }
    }
}
