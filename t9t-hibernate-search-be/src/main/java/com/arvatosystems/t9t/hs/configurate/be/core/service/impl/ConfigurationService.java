package com.arvatosystems.t9t.hs.configurate.be.core.service.impl;

import com.arvatosystems.t9t.hs.configurate.be.core.impl.IndexBuilder;
import com.arvatosystems.t9t.hs.configurate.be.core.service.IConfigurationService;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class ConfigurationService implements IConfigurationService {

    @Override
    public <T> void createIndexesFromScratch(Class<T> entityClass) throws InterruptedException {
        IndexBuilder indexBuilder = Jdp.getRequired(IndexBuilder.class);
        indexBuilder.dropIndex(entityClass);
        indexBuilder.createMissingIndexes(entityClass);
        indexBuilder.checkIndexStatus(entityClass);
    }

    @Override
    public <T> void updateIndexes(Class<T> entityClass) throws InterruptedException {
        IndexBuilder indexBuilder = Jdp.getRequired(IndexBuilder.class);
        indexBuilder.createMissingIndexes(entityClass);
        indexBuilder.checkIndexStatus(entityClass);
    }

    @Override
    public <T> void checkIndexStatus(Class<T> entityClass) throws InterruptedException {
        IndexBuilder indexBuilder = Jdp.getRequired(IndexBuilder.class);
        indexBuilder.checkIndexStatus(entityClass);
    }
}
