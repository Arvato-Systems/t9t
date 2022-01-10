/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
import com.arvatosystems.t9t.all.request.SetT9tModuleConfigsRequest;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver;
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver;
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver;

import de.jpaw.dp.Jdp;

public class SetT9tModuleConfigsRequestHandler extends AbstractReadOnlyRequestHandler<SetT9tModuleConfigsRequest> {
    protected final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);
    protected final IDocModuleCfgDtoResolver docModuleCfgResolver = Jdp.getRequired(IDocModuleCfgDtoResolver.class);
    protected final IEmailModuleCfgDtoResolver emailModuleCfgResolver = Jdp.getRequired(IEmailModuleCfgDtoResolver.class);
    protected final ISolrModuleCfgDtoResolver solrModuleCfgResolver = Jdp.getRequired(ISolrModuleCfgDtoResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final SetT9tModuleConfigsRequest request) throws Exception {
        final T9tModuleConfigs moduleConfigs = request.getModuleConfigs();
        if (moduleConfigs.getAuthModuleConfig() != null) {
            authModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getAuthModuleConfig());
        }
        if (moduleConfigs.getDocModuleConfig() != null) {
            docModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getDocModuleConfig());
        }
        if (moduleConfigs.getEmailModuleConfig() != null) {
            emailModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getEmailModuleConfig());
        }
        if (moduleConfigs.getSolrModuleConfig() != null) {
            solrModuleCfgResolver.updateModuleConfiguration(moduleConfigs.getSolrModuleConfig());
        }
        return ok();
    }
}
