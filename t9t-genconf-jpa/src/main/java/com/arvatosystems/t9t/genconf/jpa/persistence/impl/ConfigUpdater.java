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
