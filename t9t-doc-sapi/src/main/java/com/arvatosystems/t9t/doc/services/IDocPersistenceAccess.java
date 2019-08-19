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
package com.arvatosystems.t9t.doc.services;

import java.util.Map;

import com.arvatosystems.t9t.doc.DocConfigDTO;
import com.arvatosystems.t9t.doc.DocEmailCfgDTO;
import com.arvatosystems.t9t.doc.DocModuleCfgDTO;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.api.DocumentSelector;

import de.jpaw.bonaparte.pojos.api.media.MediaData;

/** Defines the communication layer between the backend modules (business logic / persistence layer). */
public interface IDocPersistenceAccess {
    DocConfigDTO                    getDocConfigDTO     (String templateId);
    DocEmailCfgDTO                  getDocEmailCfgDTO   (DocModuleCfgDTO cfg, String templateId, DocumentSelector selector);
    DocTemplateDTO                  getDocTemplateDTO   (DocModuleCfgDTO cfg, String templateId, DocumentSelector selector);
    Map<String,MediaData>           getDocComponents    (DocModuleCfgDTO cfg, DocumentSelector selector);
}
