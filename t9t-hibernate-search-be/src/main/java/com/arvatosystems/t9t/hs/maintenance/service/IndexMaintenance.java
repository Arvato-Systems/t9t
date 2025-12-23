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
package com.arvatosystems.t9t.hs.maintenance.service;

import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.model.FieldConfig;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.hibernate.search.mapper.orm.Search;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.hibernate.search.mapper.orm.work.SearchIndexingPlan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Singleton
public class IndexMaintenance {


    private static final Logger LOGGER = LoggerFactory.getLogger(IndexMaintenance.class);
    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    public void maintainIndex(@Nonnull final OperationType operationType, @Nonnull final String relatedEntityName, @Nonnull final Long objectRef) throws Exception {

        final List<EntityConfig> entityConfigList = ConfigurationLoader.getEntityConfigCache().getEntities();

        for (final EntityConfig hsViewEntity : entityConfigList) {
            final Class<?> hsViewEntityClass = Class.forName(hsViewEntity.getClassName());
            for (FieldConfig indexedField : hsViewEntity.getFields()) {
                if (indexedField.getRelatedEntity() != null && indexedField.getRelatedEntity().equals(relatedEntityName)) {
                    try {
                        final EntityManager em = jpaContextProvider.get().getEntityManager();
                        // Found a field that relates to the changed entity
                        String sql = "SELECT e FROM " + hsViewEntityClass.getSimpleName() + " e"
                                + " WHERE e." + indexedField.getName() + " = :objectRef";
                        final TypedQuery<?> query = em.createQuery(sql, hsViewEntityClass);
                        query.setParameter("objectRef", objectRef);

                        // Update changed entities in the index
                        List<?> hsRows = query.getResultList();
                        if (hsRows != null && !hsRows.isEmpty()) {
                            final SearchSession searchSession = Search.session(em);
                            final SearchIndexingPlan indexingPlan = searchSession.indexingPlan();
                            for (Object hsRow : hsRows) {
                                if (OperationType.DELETE.equals(operationType)) {
                                    indexingPlan.delete(hsRow);
                                } else {
                                    indexingPlan.addOrUpdate(hsRow);
                                }
                            }
                            indexingPlan.execute();
                        }
                    } catch (final Exception e) {
                        LOGGER.error("Index {} for related entity {} (objectRef: {}) failed!", operationType.toString(), relatedEntityName, objectRef, e);
                    }
                }
            }
        }
    }
}
