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
package com.arvatosystems.t9t.base.be.tests;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.be.impl.DefaultRequestHandlerResolver;
import com.arvatosystems.t9t.base.be.stubs.NoTenantCustomization;
import com.arvatosystems.t9t.base.request.PauseRequest;
import com.arvatosystems.t9t.base.services.IRequestHandlerResolver;

import de.jpaw.dp.Jdp;

public class NoTenantCustomizationTest {

    @Test
    public void testDefaultHandlerName() {

        // prep:
        Jdp.bindInstanceTo(new DefaultRequestHandlerResolver(), IRequestHandlerResolver.class);

        final List<String> actualNames = new NoTenantCustomization().getRequestHandlerClassnameCandidates(new PauseRequest());
        Assertions.assertEquals(2, actualNames.size(), "expect 2 possible handler names");
        Assertions.assertEquals(MessagingUtil.TWENTYEIGHT_PACKAGE_PREFIX + ".base.be.request.PauseRequestHandler", actualNames.get(0),
          "first name should be in the be package");
    }
}
