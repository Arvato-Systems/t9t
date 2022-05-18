/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.PasswordKey;
import com.arvatosystems.t9t.auth.PasswordUtil;
import com.arvatosystems.t9t.auth.PermissionsDTO;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity;
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserTenantRoleEntity;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent;
import com.arvatosystems.t9t.event.jpa.entities.SubscriberConfigEntity;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;
import java.time.Instant;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Startup(50010)
public class AuthStartup implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthStartup.class);

    private final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);

    @Override
    public void onStartup() {
        LOGGER.info("Auth module startup - checking for empty database");

        // need a context in case we write the the DB - entity listeners would access it
        final PersistenceProviderJPA ppJPA = jpaContextProvider.get();
        final EntityManager em = ppJPA.getEntityManager();
        if (em.find(TenantEntity.class, T9tConstants.GLOBAL_TENANT_REF42) == null) {
            LOGGER.info("No global tenant exists - setting up basic configuration");
            createGlobalTenant(em);
            createStartupUser(em);
            createAdminUser(em);
            createAdminRole(em);
            createCacheInvalidationListener(em);
        }

        try {
            ppJPA.commit(); // commit the transaction
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error while commit the transaction: {}", e.getMessage());
        }
    }

    protected void createGlobalTenant(final EntityManager em) {
        final TenantEntity tenant = new TenantEntity();
        tenant.setObjectRef(T9tConstants.GLOBAL_TENANT_REF42);
        tenant.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        tenant.setName("Default tenant");
        tenant.setIsActive(true);
        final PermissionsDTO permissions = new PermissionsDTO();
        permissions.setLogLevel(UserLogLevelType.REQUESTS);
        permissions.setLogLevelErrors(UserLogLevelType.REQUESTS);
        tenant.setPermissions(permissions);
        em.persist(tenant);
    }

    protected void createStartupUser(final EntityManager em) {
        LOGGER.info("Creating initial user {} with ref {}", T9tConstants.STARTUP_USER_ID, T9tConstants.STARTUP_USER_REF42);

        final UserEntity user = new UserEntity();
        user.setObjectRef(T9tConstants.STARTUP_USER_REF42);
        user.setUserId(T9tConstants.STARTUP_USER_ID);
        user.setName("System startup");
        user.setIsActive(true);
        user.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        final PermissionsDTO permissions = new PermissionsDTO();
        permissions.setLogLevel(UserLogLevelType.REQUESTS);
        permissions.setLogLevelErrors(UserLogLevelType.REQUESTS);
        permissions.setResourceRestriction("-");
        permissions.setMinPermissions(new Permissionset(0));
        permissions.setMaxPermissions(new Permissionset(0));
        user.setPermissions(permissions);
        em.persist(user);
    }

    protected void createAdminUser(final EntityManager em) {
        LOGGER.info("Creating initial user {} with ref {}", T9tConstants.ADMIN_USER_ID, T9tConstants.ADMIN_USER_REF42);

        final UserEntity user = new UserEntity();
        user.setObjectRef(T9tConstants.ADMIN_USER_REF42);
        user.setUserId(T9tConstants.ADMIN_USER_ID);
        user.setName("Administrator");
        user.setIsActive(true);
        user.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        final PermissionsDTO permissions = new PermissionsDTO();
        permissions.setLogLevel(UserLogLevelType.REQUESTS);
        permissions.setLogLevelErrors(UserLogLevelType.REQUESTS);
        permissions.setResourceIsWildcard(Boolean.TRUE);
        permissions.setResourceRestriction(""); // all web services
        permissions.setMinPermissions(IAuthorize.ALL_PERMISSIONS);
        permissions.setMaxPermissions(IAuthorize.ALL_PERMISSIONS);
        user.setPermissions(permissions);
        em.persist(user);

        // set up an initial password
        final UserStatusEntity userStatus = new UserStatusEntity();
        userStatus.setObjectRef(T9tConstants.ADMIN_USER_REF42);
        userStatus.setCurrentPasswordSerialNumber(1);
        em.persist(userStatus);

        final PasswordEntity password = new PasswordEntity();
        password.put$Key(new PasswordKey(T9tConstants.ADMIN_USER_REF42, 1));
        password.setPasswordSetByUser(T9tConstants.ADMIN_USER_REF42);
        password.setPasswordCreation(Instant.now());
        password.setPasswordExpiry(password.getPasswordCreation().plusSeconds(30 * T9tConstants.ONE_DAY_IN_S));
        password.setUserExpiry(password.getPasswordCreation().plusSeconds(3650 * T9tConstants.ONE_DAY_IN_S));
        password.setPasswordHash(PasswordUtil.createPasswordHash(T9tConstants.ADMIN_USER_ID, "changeMe"));

        em.persist(password);
    }

    protected void createAdminRole(final EntityManager em) {
        LOGGER.info("Creating initial role {} with ref {} and granting it to {}", T9tConstants.ADMIN_ROLE_ID,
                T9tConstants.ADMIN_ROLE_REF42, T9tConstants.ADMIN_USER_ID);

        final RoleEntity role = new RoleEntity();
        role.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        role.setObjectRef(T9tConstants.ADMIN_ROLE_REF42);
        role.setRoleId(T9tConstants.ADMIN_ROLE_ID);
        role.setName("Administrator role");
        role.setIsActive(true);

        em.persist(role);

        final UserTenantRoleEntity tenantRole = new UserTenantRoleEntity();
        tenantRole.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        tenantRole.setUserRef(T9tConstants.ADMIN_USER_REF42);
        tenantRole.setRoleRef(T9tConstants.ADMIN_ROLE_REF42);

        em.persist(tenantRole);
    }

    protected void createCacheInvalidationListener(final EntityManager em) {
        LOGGER.info("Creating event listener for cache invalidation");

        final SubscriberConfigEntity listener = new SubscriberConfigEntity();
        listener.setTenantRef(T9tConstants.GLOBAL_TENANT_REF42);
        listener.setObjectRef(T9tConstants.GLOBAL_TENANT_REF42 + 1);
        listener.setEventID(InvalidateCacheEvent.BClass.INSTANCE.getPqon());
        listener.setHandlerClassName("cacheInvalidation");
        listener.setIsActive(true);
        em.persist(listener);
    }
}
