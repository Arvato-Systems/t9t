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
import com.arvatosystems.t9t.doc.DocComponentDTO;
import com.arvatosystems.t9t.doc.DocComponentRef;
import com.arvatosystems.t9t.doc.jpa.entities.DocComponentEntity;
import com.arvatosystems.t9t.doc.jpa.mapping.IDocComponentDTOMapper;
import com.arvatosystems.t9t.doc.jpa.persistence.IDocComponentEntityResolver;
import com.arvatosystems.t9t.doc.services.IDocComponentResolver;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class DocComponentResolver extends AbstractJpaResolver<DocComponentRef, DocComponentDTO, FullTrackingWithVersion, DocComponentEntity>
    implements IDocComponentResolver {

    public DocComponentResolver() {
        super("DocComponent", Jdp.getRequired(IDocComponentEntityResolver.class), Jdp.getRequired(IDocComponentDTOMapper.class));
    }

    @Override
    public DocComponentRef createKey(Long ref) {
        return ref == null ? null : new DocComponentRef(ref);
    }
}
