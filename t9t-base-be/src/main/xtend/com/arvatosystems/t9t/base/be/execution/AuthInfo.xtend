package com.arvatosystems.t9t.base.be.execution

import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.base.types.SessionParameters
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import org.eclipse.xtend.lib.annotations.Data

@Data
class AuthInfo {
    SessionParameters sessionParameters
    AuthenticationParameters authenticationParameters
    JwtInfo jwtInfo
    String encodedJwt
}
