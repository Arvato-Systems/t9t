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
package com.arvatosystems.t9t.doc.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver;
import com.arvatosystems.t9t.doc.DocTemplateDTO;
import com.arvatosystems.t9t.doc.DocTemplateRef;
import com.arvatosystems.t9t.doc.jpa.entities.DocTemplateEntity;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocTemplateDTOMapper;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocTemplateEntityResolver;
import com.arvatosystems.t9t.doc.services.IDocTemplateResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DocTemplateResolver extends AbstractJpaResolver<DocTemplateRef, DocTemplateDTO, FullTrackingWithVersion, DocTemplateEntity>
    implements IDocTemplateResolver {

    public DocTemplateResolver() {
        super("DocTemplate", Jdp.getRequired(IDocTemplateEntityResolver.class), Jdp.getRequired(IDocTemplateDTOMapper.class));
    }

    @Override
    public DocTemplateRef createKey(Long ref) {
        return ref == null ? null : new DocTemplateRef(ref);
    }
}
