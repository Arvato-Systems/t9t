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

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Jdp;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.ipblocker.services.IIPAddressBlocker;
import com.arvatosystems.t9t.ipblocker.services.impl.IPAddressBlocker;
import com.arvatosystems.t9t.rest.filters.AuthFilterCustomization;
import com.arvatosystems.t9t.rest.services.IAuthFilterCustomization;
import com.arvatosystems.t9t.rest.services.IGatewayAuthChecker;

public class AuthFilterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthFilterTest.class);

    @BeforeAll
    public static void setup() {
        // Mock RuntimeDelegate to avoid ClassNotFoundException for missing JAX-RS runtime provider
        final RuntimeDelegate mockDelegate = Mockito.mock(RuntimeDelegate.class);
        final Response.ResponseBuilder mockResponseBuilder = Mockito.mock(Response.ResponseBuilder.class, Mockito.CALLS_REAL_METHODS);
        final Response mockResponse = Mockito.mock(Response.class);
        Mockito.doReturn(mockResponse).when(mockResponseBuilder).build();
        Mockito.doReturn(mockResponseBuilder).when(mockResponseBuilder).status(Mockito.anyInt());
        Mockito.doReturn(mockResponseBuilder).when(mockResponseBuilder).status(Mockito.anyInt(), Mockito.anyString());
        Mockito.doReturn(mockResponseBuilder).when(mockResponseBuilder).entity(Mockito.any());
        Mockito.doReturn(mockResponseBuilder).when(mockResponseBuilder).type(Mockito.anyString());
        Mockito.when(mockDelegate.createResponseBuilder()).thenReturn(mockResponseBuilder);
        RuntimeDelegate.setInstance(mockDelegate);
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

    @Test
    public void testFormUrlencodedMediaTypeIsAllowed() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final ContainerRequestContext ctx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(ctx.getMediaType()).thenReturn(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
        Assertions.assertFalse(authFilterCustomization.filterSupportedMediaType(ctx),
                "application/x-www-form-urlencoded should be allowed");
    }

    @Test
    public void testJsonMediaTypeIsAllowed() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final ContainerRequestContext ctx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(ctx.getMediaType()).thenReturn(MediaType.APPLICATION_JSON_TYPE);
        Assertions.assertFalse(authFilterCustomization.filterSupportedMediaType(ctx),
                "application/json should be allowed");
    }

    @Test
    public void testXmlMediaTypeIsAllowed() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final ContainerRequestContext ctx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(ctx.getMediaType()).thenReturn(MediaType.APPLICATION_XML_TYPE);
        Assertions.assertFalse(authFilterCustomization.filterSupportedMediaType(ctx),
                "application/xml should be allowed");
    }

    @Test
    public void testUnsupportedMediaTypeIsRejected() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final ContainerRequestContext ctx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(ctx.getMediaType()).thenReturn(MediaType.TEXT_PLAIN_TYPE);
        Assertions.assertTrue(authFilterCustomization.filterSupportedMediaType(ctx),
                "text/plain should be rejected");
    }

    @Test
    public void testNullMediaTypeIsAllowed() {
        final IAuthFilterCustomization authFilterCustomization = new AuthFilterCustomization();
        final ContainerRequestContext ctx = Mockito.mock(ContainerRequestContext.class);
        Mockito.when(ctx.getMediaType()).thenReturn(null);
        Assertions.assertFalse(authFilterCustomization.filterSupportedMediaType(ctx),
                "null media type should be allowed (no content type check applies)");
    }
}
