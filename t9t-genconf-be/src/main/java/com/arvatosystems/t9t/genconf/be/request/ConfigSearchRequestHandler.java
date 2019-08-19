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
package com.arvatosystems.t9t.genconf.be.request;

import com.arvatosystems.t9t.genconf.ConfigDTO;
import com.arvatosystems.t9t.genconf.request.ConfigSearchRequest;
import com.arvatosystems.t9t.genconf.services.IConfigResolver;
import com.arvatosystems.t9t.base.be.impl.AbstractSearchBERequestHandler;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;

import de.jpaw.dp.Jdp;

public class ConfigSearchRequestHandler extends AbstractSearchBERequestHandler<ConfigDTO, FullTrackingWithVersion, ConfigSearchRequest> {

    // @Inject
    protected final IConfigResolver resolver = Jdp.getRequired(IConfigResolver.class);

    @Override
    public ReadAllResponse<ConfigDTO, FullTrackingWithVersion> execute(ConfigSearchRequest request) throws Exception {
        return execute(resolver.query(
                request.getLimit(),
                request.getOffset(),
                request.getSearchFilter(),
                request.getSortColumns()),
                request.getSearchOutputTarget());
    }
}
