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
package com.arvatosystems.t9t.solr.jpa.impl

import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver
import com.arvatosystems.t9t.solr.SolrModuleCfgDTO
import com.arvatosystems.t9t.solr.jpa.entities.SolrModuleCfgEntity
import com.arvatosystems.t9t.solr.jpa.persistence.ISolrModuleCfgEntityResolver
import com.arvatosystems.t9t.solr.services.ISolrModuleCfgDtoResolver
import de.jpaw.dp.Singleton

@Singleton
class SolrModuleCfgDtoResolver extends AbstractModuleConfigResolver<SolrModuleCfgDTO, SolrModuleCfgEntity> implements ISolrModuleCfgDtoResolver {
    private static final SolrModuleCfgDTO DEFAULT_MODULE_CFG = new SolrModuleCfgDTO(
        null,                       // Json z
        null,                       // baseUrl
        null,                       // smtpServerUserId;
        null,                       // smtpServerPassword
        null                        // smtpServerTls
    );

    public new() {
        super(ISolrModuleCfgEntityResolver)
    }

    override public SolrModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
