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
package com.arvatosystems.t9t.tfi.component.dropdown;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.request.GetQualifiersRequest;
import com.arvatosystems.t9t.base.request.GetQualifiersResponse;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
import com.arvatosystems.t9t.tfi.component.Dropdown28ForQualifier;

import de.jpaw.dp.Jdp;

public class Dropdown28FactoryForQualifiers {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dropdown28FactoryForQualifiers.class);
    private static final ConcurrentMap<String, List<String>> cache = new ConcurrentHashMap<>(64);

    public static Dropdown28ForQualifier createInstance(String pqon) {
        List<String> values = cache.get(pqon);
        if (values == null) {
            LOGGER.info("No cached data for qualifiers of {}, asking backend", pqon);
            T9TRemoteUtils remote = Jdp.getRequired(T9TRemoteUtils.class);
            final GetQualifiersRequest rq = new GetQualifiersRequest();
            final String [] pqons = pqon.split(",");
            rq.setFullyQualifiedClassNames(new ArrayList<>(pqons.length));
            for (String p: pqons)
                rq.getFullyQualifiedClassNames().add("com.arvatosystems.t9t." + p);
            GetQualifiersResponse resp = remote.executeExpectOk(rq, GetQualifiersResponse.class);
            values = cache.computeIfAbsent(pqon, (k) -> new ArrayList<String>(resp.getQualifiers()));
        }
        return new Dropdown28ForQualifier(values);
    }
}
