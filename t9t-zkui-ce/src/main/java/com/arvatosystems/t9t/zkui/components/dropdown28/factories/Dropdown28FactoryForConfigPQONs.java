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
package com.arvatosystems.t9t.zkui.components.dropdown28.factories;

import com.arvatosystems.t9t.base.request.GetConfigPQONsRequest;
import com.arvatosystems.t9t.base.request.GetConfigPQONsResponse;
import com.arvatosystems.t9t.zkui.components.dropdown28.nodb.Dropdown28ForConfigPQONs;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Named("cfgPQON")
@Singleton
public final class Dropdown28FactoryForConfigPQONs implements IDropdown28BasicFactory<Dropdown28ForConfigPQONs> {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dropdown28FactoryForConfigPQONs.class);
    private static final List<String> CACHE = Collections.synchronizedList(new ArrayList<>(64));

    @Override
    public String getDropdownId() {
        return "cfgPQON";
    }

    @Nonnull
    public Dropdown28ForConfigPQONs createInstance() {
        final List<String> pqons = new ArrayList<>(CACHE.isEmpty() ? 64 : CACHE.size());
        if (CACHE.isEmpty()) {
            LOGGER.info("No cached data for config PQONs, asking backend");
            IT9tRemoteUtils remote = Jdp.getRequired(IT9tRemoteUtils.class);
            GetConfigPQONsRequest request = new GetConfigPQONsRequest();
            GetConfigPQONsResponse response = remote.executeExpectOk(request, GetConfigPQONsResponse.class);
            CACHE.addAll(response.getPqons());
        }
        final ApplicationSession as = ApplicationSession.get();
        for (final String pqon : CACHE) {
            String translated = as.translate(null, pqon);
            pqons.add(translated == null || translated.startsWith("$") ? pqon : translated);
        }
        return new Dropdown28ForConfigPQONs(pqons);
    }
}
