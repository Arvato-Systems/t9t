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
package com.arvatosystems.t9t.auth.jpa.impl

import com.arvatosystems.t9t.auth.TenantLogoDTO
import com.arvatosystems.t9t.auth.jpa.entities.TenantLogoEntity
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantLogoEntityResolver
import com.arvatosystems.t9t.auth.services.ITenantLogoDtoResolver
import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver
import de.jpaw.bonaparte.api.media.MediaTypes
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.dp.Singleton
import de.jpaw.util.ByteArray
import java.nio.charset.StandardCharsets

@Singleton
class TenantLogoDtoResolver extends AbstractModuleConfigResolver<TenantLogoDTO, TenantLogoEntity> implements ITenantLogoDtoResolver {
    static final byte [] TRANSPARENT_1X1 =
        "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=".getBytes(StandardCharsets.UTF_8)
    static final TenantLogoDTO DEFAULT_MODULE_CFG = new TenantLogoDTO(
        null,                       // Json z
        new MediaData => [
            mediaType   = MediaTypes.MEDIA_XTYPE_PNG
            rawData     = ByteArray.fromBase64(TRANSPARENT_1X1, 0, TRANSPARENT_1X1.length)
        ]
    );

    new() {
        super(ITenantLogoEntityResolver)
    }

    override TenantLogoDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
