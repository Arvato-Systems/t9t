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
package com.arvatosystems.t9t.all.be.request

import com.arvatosystems.t9t.all.request.GetT9tModuleConfigsRequest
import com.arvatosystems.t9t.all.request.GetT9tModuleConfigsResponse
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver
import de.jpaw.dp.Inject
import com.arvatosystems.t9t.all.T9tModuleConfigs
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver

class GetT9tModuleConfigsRequestHandler extends AbstractReadOnlyRequestHandler<GetT9tModuleConfigsRequest> {
    @Inject protected IAuthModuleCfgDtoResolver  authModuleCfgResolver
    @Inject protected IDocModuleCfgDtoResolver   docModuleCfgResolver
    @Inject protected IEmailModuleCfgDtoResolver emailModuleCfgResolver
    @Inject protected ISolrModuleCfgDtoResolver  solrModuleCfgResolver

    override GetT9tModuleConfigsResponse execute(RequestContext ctx, GetT9tModuleConfigsRequest rq) {
        return new GetT9tModuleConfigsResponse => [
            moduleConfigs = new T9tModuleConfigs => [
                authModuleConfig    = authModuleCfgResolver.moduleConfiguration
                docModuleConfig     = docModuleCfgResolver.moduleConfiguration
                emailModuleConfig   = emailModuleCfgResolver.moduleConfiguration
                solrModuleConfig    = solrModuleCfgResolver.moduleConfiguration
            ]
        ]
    }
}
