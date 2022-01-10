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
package com.arvatosystems.t9t.base.be.execution;

import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.base.types.SessionParameters;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

import java.util.Objects;

public class AuthInfo {
    private final SessionParameters sessionParameters;

    private final AuthenticationParameters authenticationParameters;

    private final JwtInfo jwtInfo;

    private final String encodedJwt;

    public AuthInfo(final SessionParameters sessionParameters, final AuthenticationParameters authenticationParameters, final JwtInfo jwtInfo,
            final String encodedJwt) {
        super();
        this.sessionParameters = sessionParameters;
        this.authenticationParameters = authenticationParameters;
        this.jwtInfo = jwtInfo;
        this.encodedJwt = encodedJwt;
    }

    public SessionParameters getSessionParameters() {
        return sessionParameters;
    }

    public AuthenticationParameters getAuthenticationParameters() {
        return authenticationParameters;
    }

    public JwtInfo getJwtInfo() {
        return jwtInfo;
    }

    public String getEncodedJwt() {
        return encodedJwt;
    }

    @Override
    public int hashCode() {
        return Objects.hash(authenticationParameters, encodedJwt, jwtInfo, sessionParameters);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuthInfo other = (AuthInfo) obj;
        return Objects.equals(authenticationParameters, other.authenticationParameters) && Objects.equals(encodedJwt, other.encodedJwt)
                && Objects.equals(jwtInfo, other.jwtInfo) && Objects.equals(sessionParameters, other.sessionParameters);
    }

    @Override
    public String toString() {
        return "AuthInfo [sessionParameters=" + sessionParameters + ", authenticationParameters=" + authenticationParameters + ", jwtInfo=" + jwtInfo
                + ", encodedJwt=" + encodedJwt + "]";
    }
}
