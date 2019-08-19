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
package com.arvatosystems.t9t.uiprefsv3.jpa.persistence.impl

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigRef
import com.arvatosystems.t9t.uiprefsv3.jpa.entities.LeanGridConfigEntity
import com.arvatosystems.t9t.uiprefsv3.jpa.mapping.ILeanGridConfigDTOMapper
import com.arvatosystems.t9t.uiprefsv3.jpa.persistence.ILeanGridConfigEntityResolver
import com.arvatosystems.t9t.uiprefsv3.services.ILeanGridConfigResolver
import de.jpaw.dp.Default
import de.jpaw.dp.Jdp
import de.jpaw.dp.Singleton

@Default @Singleton
class LeanGridConfigJpaResolver extends AbstractJpaResolver<LeanGridConfigRef,LeanGridConfigDTO,FullTrackingWithVersion,LeanGridConfigEntity> implements ILeanGridConfigResolver {

    public new() {
        super("LeanGridConfig", Jdp.getRequired(ILeanGridConfigEntityResolver), Jdp.getRequired(ILeanGridConfigDTOMapper))
    }

    override public LeanGridConfigRef createKey(Long ref) {
        return if (ref !== null) new LeanGridConfigRef(ref);
    }
}
