package com.arvatosystems.t9t.plugins.jpa.impl;

import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.plugins.jpa.entities.LoadedPluginEntity;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import de.jpaw.util.ExceptionUtil;

@Startup(50073)
public class InitialPluginsLoader implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(InitialPluginsLoader.class);
    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    private final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    /** performs a standard query, but cross tenant. */
    @Override
    public void onStartup() {
        try {
            final PersistenceProviderJPA cp = jpaContextProvider.get();
            final TypedQuery<LoadedPluginEntity> query = cp.getEntityManager().createQuery(
                "SELECT p FROM " + LoadedPluginEntity.class.getSimpleName() + " p WHERE p.isActive = :isActive ORDER BY p.tenantRef, p.priority DESC",
                LoadedPluginEntity.class);
            query.setParameter("isActive", true);
            List<LoadedPluginEntity> plugins = query.getResultList();
            LOGGER.info("Preloading {} plugins", plugins.size());
            for (LoadedPluginEntity pe: plugins) {
                pluginManager.loadPlugin(pe.getTenantRef(), pe.getJarFile());
            }
            cp.getEntityManager().clear();  // get rid of big copies of JAR images inside cached JPA entities
        } catch (Exception e) {
            LOGGER.error("Could not load persisted plugins - {}", ExceptionUtil.causeChain(e));
        }
    }
}
