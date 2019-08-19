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
package com.arvatosystems.t9t.auth.jwt.tests;


import org.joda.time.Instant;
import org.junit.Assert;
import org.junit.Test;

import com.arvatosystems.t9t.auth.jwt.IJWT;
import com.arvatosystems.t9t.auth.jwt.JWT;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

public class JwtTest {

    @Test
    public void checkSign() throws Exception {
        JWT jwt = JWT.createJwt(IJWT.class.getResourceAsStream("/mykeystore.jceks"), "xyzzy5");
        JwtInfo info = new JwtInfo();

        String encoded = jwt.sign(info, 10L, null);
        long now = System.currentTimeMillis();


        JwtInfo info2 = jwt.decode(encoded);

        Instant whenSigned = info.getIssuedAt();  // the system timestamp written back

        Assert.assertNotNull(whenSigned);
        Assert.assertTrue(now >= whenSigned.getMillis());
        Assert.assertEquals(whenSigned, info2.getIssuedAt());
    }
}
