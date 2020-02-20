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
package com.arvatosystems.t9t.xml.imports

import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.auth.UserKey
import com.arvatosystems.t9t.auth.request.UserCrudRequest
import com.arvatosystems.t9t.in.be.impl.AbstractInputDataTransformer
import com.arvatosystems.t9t.xml.User001
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named

@Named("xmlUser001Import")
@AddLogger
@Dependent
class UserImportDataTransformer extends AbstractInputDataTransformer<User001> {

    // convert a data object into a request
    override transform(User001 xml) {
        val crud = new UserCrudRequest
        crud.crud       = OperationType.MERGE
        crud.naturalKey = new UserKey(xml.userId)
        crud.data       = transformSub(xml)
        return crud
    }

    def protected UserDTO transformSub(User001 xml) {
        val dto = new UserDTO

        dto.userId         = xml.userId
        dto.name           = xml.name
        dto.emailAddress   = xml.emailAddress
        dto.isActive       = xml.isActive ?: Boolean.TRUE
        dto.roleRef        = if (xml.roleId !== null) new RoleKey(xml.roleId);
        dto.isTechnical    = xml.isTechnical
        dto.office         = xml.office
        dto.department     = xml.department
        dto.jobTitle       = xml.jobTitle
        dto.phoneNo        = xml.phoneNo
        dto.mobilePhoneNo  = xml.mobilePhoneNo
        dto.externalAuth   = xml.externalAuth
        dto.supervisorRef  = if (xml.supervisorId !== null) new UserKey(xml.supervisorId);
        dto.z              = xml.z

        return dto
    }
}
