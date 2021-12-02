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
package com.arvatosystems.t9t.zkui.components.dropdown28.factories;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.request.GetQualifiersRequest;
import com.arvatosystems.t9t.base.request.GetQualifiersResponse;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28ForQualifier;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;

import de.jpaw.dp.Jdp;

public final class Dropdown28FactoryForQualifiers {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dropdown28FactoryForQualifiers.class);
    private static final ConcurrentMap<String, List<String>> CACHE = new ConcurrentHashMap<>(64);

    private Dropdown28FactoryForQualifiers() { }

    public static Dropdown28ForQualifier createInstance(String pqon) {
        List<String> values = CACHE.get(pqon);
        if (values == null) {
            LOGGER.info("No cached data for qualifiers of {}, asking backend", pqon);
            IT9tRemoteUtils remote = Jdp.getRequired(IT9tRemoteUtils.class);
            final GetQualifiersRequest rq = new GetQualifiersRequest();
            final String[] pqons = pqon.split(",");
            rq.setFullyQualifiedClassNames(new ArrayList<>(pqons.length));
            for (String p: pqons) {
                rq.getFullyQualifiedClassNames().add("com.arvatosystems.t9t." + p);
            }
            GetQualifiersResponse resp = remote.executeExpectOk(rq, GetQualifiersResponse.class);
            values = CACHE.computeIfAbsent(pqon, (k) -> new ArrayList<String>(resp.getQualifiers()));
        }
        return new Dropdown28ForQualifier(values);
    }
}
