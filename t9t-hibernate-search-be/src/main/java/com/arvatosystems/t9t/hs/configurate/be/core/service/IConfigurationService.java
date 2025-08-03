package com.arvatosystems.t9t.hs.configurate.be.core.service;

public interface IConfigurationService {

    /**
     * Initializes the indexes for all entities that are annotated with @Indexed.
     * This method should be called at application startup to ensure that all
     * necessary indexes are created and up-to-date.
     * Note: This operation erases all existing indexes for the entity class!
     */
    <T> void createIndexesFromScratch(Class<T> entityClass) throws InterruptedException;

    <T> void updateIndexes(Class<T> entityClass) throws InterruptedException;

    <T> void checkIndexStatus(Class<T> entityClass) throws InterruptedException;
}
