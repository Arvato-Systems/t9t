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
package com.arvatosystems.t9t.xml.exports

import com.arvatosystems.t9t.auth.UserDTO
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.out.be.IPreOutputDataTransformer
import com.arvatosystems.t9t.xml.User001
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import com.arvatosystems.t9t.auth.RoleRef
import com.arvatosystems.t9t.auth.RoleKey
import com.arvatosystems.t9t.auth.RoleDTO
import com.arvatosystems.t9t.auth.UserRef
import com.arvatosystems.t9t.auth.UserKey

@Named("xmlUserExport")
@AddLogger
@Dependent
class UserExportDataTransformer implements IPreOutputDataTransformer {

    // convert a UserDTO into a User001 object
    override transformData(BonaPortable record, DataSinkDTO sinkCfg, OutputSessionParameters outputSessionParameters) {
        val internal = record as UserDTO
        val external = new User001

        external.userId         = internal.userId
        external.name           = internal.name ?: "?"
        external.emailAddress   = internal.emailAddress
        external.isActive       = internal.isActive
        external.roleId         = getRoleId(internal.roleRef)
        external.isTechnical    = internal.isTechnical
        external.office         = internal.office
        external.department     = internal.department
        external.jobTitle       = internal.jobTitle
        external.phoneNo        = internal.phoneNo
        external.mobilePhoneNo  = internal.mobilePhoneNo
        external.externalAuth   = internal.externalAuth
        external.supervisorId   = getUserId(internal.supervisorRef)
        external.z              = internal.z

        return #[ external ]
    }

    def protected String getRoleId(RoleRef ref) {
        if (ref === null) {
            return null;
        }
        if (ref instanceof RoleKey) {
            return ref.roleId;
        }
        if (ref instanceof RoleDTO) {
            return ref.roleId;
        }
        return null;
    }

    def protected String getUserId(UserRef ref) {
        if (ref === null) {
            return null;
        }
        if (ref instanceof UserKey) {
            return ref.userId;
        }
        if (ref instanceof UserDTO) {
            return ref.userId;
        }
        return null;
    }
}
