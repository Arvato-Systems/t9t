/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.be.jwt;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.auth.services.IAuthenticator;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Jdp;

@Fallback
@Dependent
public class Authenticator implements IAuthenticator {

    private final IJWT generator = Jdp.getRequired(IJWT.class); // JWT.createDefaultJwt();

    @Override
    public String doSign(JwtInfo info) {
        return generator.sign(info, 12L * 60 * 60, null);
    }

    @Override
    public String doSign(JwtInfo info, Long durationInSeconds) {
        return generator.sign(info, durationInSeconds, null);
    }
}
