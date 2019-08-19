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

import com.arvatosystems.t9t.all.request.SetT9tModuleConfigsRequest
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.doc.services.IDocModuleCfgDtoResolver
import com.arvatosystems.t9t.email.services.IEmailModuleCfgDtoResolver
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver
import de.jpaw.dp.Inject

class SetT9tModuleConfigsRequestHandler extends AbstractReadOnlyRequestHandler<SetT9tModuleConfigsRequest> {
    @Inject protected IAuthModuleCfgDtoResolver  authModuleCfgResolver
    @Inject protected IDocModuleCfgDtoResolver   docModuleCfgResolver
    @Inject protected IEmailModuleCfgDtoResolver emailModuleCfgResolver
    @Inject protected ISolrModuleCfgDtoResolver  solrModuleCfgResolver

    override ServiceResponse execute(RequestContext ctx, SetT9tModuleConfigsRequest rq) {
        val it = rq.moduleConfigs
        if (authModuleConfig  !== null) authModuleCfgResolver .updateModuleConfiguration(authModuleConfig)
        if (docModuleConfig   !== null) docModuleCfgResolver  .updateModuleConfiguration(docModuleConfig)
        if (emailModuleConfig !== null) emailModuleCfgResolver.updateModuleConfiguration(emailModuleConfig)
        if (solrModuleConfig  !== null) solrModuleCfgResolver .updateModuleConfiguration(solrModuleConfig)
        return ok
    }
}
