package com.arvatosystems.t9t.jpa.refs;

import jakarta.persistence.EntityManager;

import com.arvatosystems.t9t.refs.PersistenceProvider;

public interface PersistenceProviderJPA extends PersistenceProvider {
    EntityManager getEntityManager();
}
