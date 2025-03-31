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
package com.arvatosystems.t9t.all.be.request;

import com.arvatosystems.t9t.all.T9tModuleConfigs;
import com.arvatosystems.t9t.all.request.GetT9tModuleConfigsRequest;
import com.arvatosystems.t9t.all.request.GetT9tModuleConfigsResponse;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver;

import de.jpaw.dp.Jdp;

public class GetT9tModuleConfigsRequestHandler extends AbstractReadOnlyRequestHandler<GetT9tModuleConfigsRequest> {
    private final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    private final IDocModuleCfgDtoResolver docModuleCfgResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    private final IEmailModuleCfgDtoResolver emailModuleCfgResolver = Jdp.getRequired(IEmailModuleCfgDtoResolver.class);
    private final ISolrModuleCfgDtoResolver solrModuleCfgResolver = Jdp.getRequired(ISolrModuleCfgDtoResolver.class);

    @Override
    public GetT9tModuleConfigsResponse execute(final RequestContext ctx, final GetT9tModuleConfigsRequest request) throws Exception {
        final GetT9tModuleConfigsResponse resp = new GetT9tModuleConfigsResponse();
        final T9tModuleConfigs moduleConfigs = new T9tModuleConfigs();
        moduleConfigs.setAuthModuleConfig(authModuleCfgResolver.getModuleConfiguration());
        moduleConfigs.setDocModuleConfig(docModuleCfgResolver.getModuleConfiguration());
        moduleConfigs.setEmailModuleConfig(emailModuleCfgResolver.getModuleConfiguration());
        moduleConfigs.setSolrModuleConfig(solrModuleCfgResolver.getModuleConfiguration());
        resp.setModuleConfigs(moduleConfigs);
        return resp;
    }
}
