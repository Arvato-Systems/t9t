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
package com.arvatosystems.t9t.solr;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.solr.request.SolrModuleCfgCrudRequest;
import com.arvatosystems.t9t.solr.request.SolrModuleCfgSearchRequest;

public final class T9tSolrModels implements IViewModelContainer {

    private static final CrudViewModel<SolrModuleCfgDTO, FullTrackingWithVersion> SOLR_MODULE_CFG_VIEW_MODEL
      = new CrudViewModel<SolrModuleCfgDTO, FullTrackingWithVersion>(
        SolrModuleCfgDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        SolrModuleCfgSearchRequest.BClass.INSTANCE,
        SolrModuleCfgCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("solrModuleCfg",  SOLR_MODULE_CFG_VIEW_MODEL);
    }
}
