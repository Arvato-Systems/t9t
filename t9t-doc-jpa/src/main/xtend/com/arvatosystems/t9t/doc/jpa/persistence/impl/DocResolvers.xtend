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
package com.arvatosystems.t9t.doc.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.doc.DocComponentRef
import com.arvatosystems.t9t.doc.DocConfigRef
import com.arvatosystems.t9t.doc.DocEmailCfgRef
import com.arvatosystems.t9t.doc.DocTemplateRef
import com.arvatosystems.t9t.doc.jpa.entities.DocComponentEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocConfigEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocEmailCfgEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocModuleCfgEntity
import com.arvatosystems.t9t.doc.jpa.entities.DocTemplateEntity

/*
 * Generates resolver classes for all entities in the doc module. The generator class itself is not used.
 */
@AutoResolver42
class DocResolvers {

    @AllCanAccessGlobalTenant
    def DocModuleCfgEntity      getDocModuleCfgEntity       (Long key,       boolean onlyActive) {}
    @AllCanAccessGlobalTenant
    def DocConfigEntity         getDocConfigEntity          (DocConfigRef ref,          boolean onlyActive) {}
    @AllCanAccessGlobalTenant
    def DocEmailCfgEntity       getDocEmailCfgEntity        (DocEmailCfgRef ref,        boolean onlyActive) {}
    @AllCanAccessGlobalTenant
    def DocTemplateEntity       getDocTemplateEntity        (DocTemplateRef ref,        boolean onlyActive) {}
    @AllCanAccessGlobalTenant
    def DocComponentEntity      getDocComponentEntity       (DocComponentRef ref,       boolean onlyActive) {}
}
