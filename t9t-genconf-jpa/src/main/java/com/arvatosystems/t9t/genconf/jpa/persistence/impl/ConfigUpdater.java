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
package com.arvatosystems.t9t.genconf.jpa.persistence.impl;

import com.arvatosystems.t9t.genconf.ConfigDTO;
import com.arvatosystems.t9t.genconf.jpa.entities.ConfigEntity;
import com.arvatosystems.t9t.genconf.jpa.mapping.IConfigDTOMapper;
import com.arvatosystems.t9t.genconf.jpa.persistence.IConfigEntityResolver;
import com.arvatosystems.t9t.genconf.services.IConfigUpdater;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.persistence.Query;

@Singleton
public class ConfigUpdater implements IConfigUpdater {

    private static final String DELETE_CONFIG_SQL = "DELETE FROM " + ConfigEntity.class.getSimpleName() + " c" + " WHERE c.objectRef = :objectRef";

    private final IConfigEntityResolver configEntityResolver = Jdp.getRequired(IConfigEntityResolver.class);
    private final IConfigDTOMapper configMapper = Jdp.getRequired(IConfigDTOMapper.class);

    @Override
    public void updateConfig(final ConfigDTO configDto) {

        final ConfigEntity configEntity = configMapper.mapToEntity(configDto);

        if (configDto.getObjectRef() == null) {
            configEntity.setObjectRef(configEntityResolver.createNewPrimaryKey());
            configEntityResolver.save(configEntity);
        } else {
            configEntityResolver.update(configEntity);
        }
    }

    @Override
    public void deleteConfig(final Long configRef) {
        final Query deleteQuery = configEntityResolver.getEntityManager().createQuery(DELETE_CONFIG_SQL);
        deleteQuery.setParameter("objectRef", configRef);
        deleteQuery.executeUpdate();
    }
}
