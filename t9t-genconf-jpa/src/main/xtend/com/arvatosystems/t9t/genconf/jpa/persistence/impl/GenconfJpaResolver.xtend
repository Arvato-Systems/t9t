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
package com.arvatosystems.t9t.genconf.jpa.persistence.impl

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.jpa.impl.AbstractJpaResolver
import com.arvatosystems.t9t.genconf.ConfigDTO
import com.arvatosystems.t9t.genconf.ConfigRef
import com.arvatosystems.t9t.genconf.jpa.entities.ConfigEntity
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver
import com.arvatosystems.t9t.genconf.services.IConfigResolver
import de.jpaw.dp.Default
import de.jpaw.dp.Jdp
import de.jpaw.dp.Singleton

@Default @Singleton
class GenconfJpaResolver extends AbstractJpaResolver<ConfigRef,ConfigDTO,FullTrackingWithVersion,ConfigEntity> implements IConfigResolver {

    public new() {
        super("Config", Jdp.getRequired(IConfigEntityResolver), Jdp.getRequired(IConfigDTOMapper))
    }

    override public ConfigRef createKey(Long ref) {
        return if (ref !== null) new ConfigRef(ref);
    }
}
