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
package com.arvatosystems.t9t.rest.filters.test;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.ipblocker.services.IIPAddressBlocker;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.filters.AuthFilterCustomization;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;

import de.jpaw.dp.Jdp;

public class AuthFilterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterTest.class);

    @BeforeAll
    public static void setup() {
        // set system property
        final Properties props = System.getProperties();
        props.setProperty("t9t.restapi.userpw", "true");
        // reset and init Jdp
        Jdp.reset();
        Jdp.bindInstanceTo(new IPAddressBlocker(), IIPAddressBlocker.class);
        Jdp.bindInstanceTo((hdr, params) -> true, IGatewayAuthChecker.class);
        // Jdp.bindInstanceTo(new AuthFilterCustomization(), IAuthFilterCustomization.class);
    }

    @Test
    public void testIfUserPwAllowed() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final String auth = T9tConstants.HTTP_AUTH_PREFIX_USER_PW + "dGVzdDpzZWNyZXQ="; // test:secret as dummy (not a real password)
        final boolean bad = authFilterCustomization.filterAuthenticated(null, auth);
        LOGGER.info("Filter does{} allow access", bad ? " NOT" : "");
        Assertions.assertFalse(bad, "Access should not be blocked");
    }
}
