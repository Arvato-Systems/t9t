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
