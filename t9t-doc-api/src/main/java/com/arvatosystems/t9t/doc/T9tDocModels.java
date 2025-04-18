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
package com.arvatosystems.t9t.doc;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.doc.request.DocComponentCrudRequest;
import com.arvatosystems.t9t.doc.request.DocComponentSearchRequest;
import com.arvatosystems.t9t.doc.request.DocConfigCrudRequest;
import com.arvatosystems.t9t.doc.request.DocConfigSearchRequest;
import com.arvatosystems.t9t.doc.request.DocEmailCfgCrudRequest;
import com.arvatosystems.t9t.doc.request.DocEmailCfgSearchRequest;
import com.arvatosystems.t9t.doc.request.DocModuleCfgCrudRequest;
import com.arvatosystems.t9t.doc.request.DocModuleCfgSearchRequest;
import com.arvatosystems.t9t.doc.request.DocTemplateCrudRequest;
import com.arvatosystems.t9t.doc.request.DocTemplateSearchRequest;
import com.arvatosystems.t9t.doc.request.MailingGroupCrudRequest;
import com.arvatosystems.t9t.doc.request.MailingGroupSearchRequest;

public final class T9tDocModels implements IViewModelContainer {

    private static final CrudViewModel<DocConfigDTO, FullTrackingWithVersion> DOC_CONFIG_VIEW_MODEL = new CrudViewModel<>(
        DocConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DocConfigSearchRequest.BClass.INSTANCE,
        DocConfigCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<DocEmailCfgDTO, FullTrackingWithVersion> DOC_EMAIL_CFG_VIEW_MODEL = new CrudViewModel<>(
        DocEmailCfgDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DocEmailCfgSearchRequest.BClass.INSTANCE,
        DocEmailCfgCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<DocTemplateDTO, FullTrackingWithVersion> DOC_TEMPLATE_VIEW_MODEL = new CrudViewModel<>(
        DocTemplateDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DocTemplateSearchRequest.BClass.INSTANCE,
        DocTemplateCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<DocComponentDTO, FullTrackingWithVersion> DOC_COMPONENT_VIEW_MODEL = new CrudViewModel<>(
        DocComponentDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DocComponentSearchRequest.BClass.INSTANCE,
        DocComponentCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<DocModuleCfgDTO, FullTrackingWithVersion> DOC_MODULE_CFG_VIEW_MODEL = new CrudViewModel<>(
        DocModuleCfgDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DocModuleCfgSearchRequest.BClass.INSTANCE,
        DocModuleCfgCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<MailingGroupDTO, FullTrackingWithVersion> MAILING_GROUP_VIEW_MODEL = new CrudViewModel<>(
        MailingGroupDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        MailingGroupSearchRequest.BClass.INSTANCE,
        MailingGroupCrudRequest.BClass.INSTANCE);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("docConfig",  DOC_CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("docEmailCfg",  DOC_EMAIL_CFG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("docTemplate",  DOC_TEMPLATE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("docComponent",  DOC_COMPONENT_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("docModuleCfg",  DOC_MODULE_CFG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("mailingGroup",  MAILING_GROUP_VIEW_MODEL);
    }
}
