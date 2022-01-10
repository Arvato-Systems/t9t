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
package com.arvatosystems.t9t.auth.be.impl;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

public class AuthData {
    private final String jwtToken; // encoded form, without "Bearer" prefix

    private final JwtInfo jwtInfo; // decoded data in map form

    public boolean isValid() {
        return (this.jwtToken != null);
    }

    public AuthData(final String jwtToken, final JwtInfo jwtInfo) {
        super();
        this.jwtToken = jwtToken;
        this.jwtInfo = jwtInfo;
    }

    @Override
    @Pure
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.jwtToken == null) ? 0 : this.jwtToken.hashCode());
        return prime * result + ((this.jwtInfo == null) ? 0 : this.jwtInfo.hashCode());
    }

    @Override
    @Pure
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AuthData other = (AuthData) obj;
        if (this.jwtToken == null) {
            if (other.jwtToken != null)
                return false;
        } else if (!this.jwtToken.equals(other.jwtToken))
            return false;
        if (this.jwtInfo == null) {
            if (other.jwtInfo != null)
                return false;
        } else if (!this.jwtInfo.equals(other.jwtInfo))
            return false;
        return true;
    }

    @Override
    @Pure
    public String toString() {
        ToStringBuilder b = new ToStringBuilder(this);
        b.add("jwtToken", this.jwtToken);
        b.add("jwtInfo", this.jwtInfo);
        return b.toString();
    }

    @Pure
    public String getJwtToken() {
        return this.jwtToken;
    }

    @Pure
    public JwtInfo getJwtInfo() {
        return this.jwtInfo;
    }
}
