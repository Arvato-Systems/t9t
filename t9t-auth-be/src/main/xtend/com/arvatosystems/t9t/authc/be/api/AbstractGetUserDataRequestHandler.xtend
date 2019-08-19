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
package com.arvatosystems.t9t.authc.be.api

import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.services.IUserResolver
import com.arvatosystems.t9t.authc.api.GetUserDataResponse
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import de.jpaw.dp.Inject
import de.jpaw.bonaparte.annotations.FieldMapper
import com.arvatosystems.t9t.authc.api.UserData

class AbstractGetUserDataRequestHandler<T extends RequestParameters> extends AbstractReadOnlyRequestHandler<T> {
    @Inject
    protected IUserResolver resolver

    @FieldMapper
    def protected void mapUserData(UserData data, UserDTO dto) {
    }

    def protected GetUserDataResponse responseFromDto(UserDTO dto) {
        val resp = new GetUserDataResponse
        val data = new UserData
        mapUserData(data, dto)
        data.userRef = dto.objectRef
        resp.userData = data
        return resp
    }
}
