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
package com.arvatosystems.t9t.auth.jpa.impl;

import com.arvatosystems.t9t.auth.TenantLogoDTO;
import com.arvatosystems.t9t.auth.jpa.entities.TenantLogoEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.ITenantLogoDTOMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.ITenantLogoEntityResolver;
import com.arvatosystems.t9t.auth.services.ITenantLogoDtoResolver;
import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import java.nio.charset.StandardCharsets;

@Singleton
public class TenantLogoDtoResolver extends AbstractModuleConfigResolver<TenantLogoDTO, TenantLogoEntity> implements ITenantLogoDtoResolver {
    private static final byte[] TRANSPARENT_1X1 =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNkYAAAAAYAAjCB0C8AAAAASUVORK5CYII=".getBytes(StandardCharsets.UTF_8);

    private static final MediaData TRANSPARENT_LOGO_1X1 = new MediaData(MediaTypes.MEDIA_XTYPE_PNG);
    private static final TenantLogoDTO DEFAULT_MODULE_CFG = new TenantLogoDTO();
    static {
        TRANSPARENT_LOGO_1X1.setRawData(ByteArray.fromBase64(TRANSPARENT_1X1, 0, TRANSPARENT_1X1.length));
        TRANSPARENT_LOGO_1X1.freeze();
        DEFAULT_MODULE_CFG.setLogo(TRANSPARENT_LOGO_1X1);
        DEFAULT_MODULE_CFG.freeze();
    }

    public TenantLogoDtoResolver() {
        super(ITenantLogoEntityResolver.class, ITenantLogoDTOMapper.class);
    }

    @Override
    public TenantLogoDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
