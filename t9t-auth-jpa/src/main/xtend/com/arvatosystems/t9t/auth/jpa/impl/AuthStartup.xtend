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

import com.arvatosystems.t9t.auth.PasswordKey
import com.arvatosystems.t9t.auth.PasswordUtil
import com.arvatosystems.t9t.auth.PermissionsDTO
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserTenantRoleEntity
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.event.InvalidateCacheEvent
import com.arvatosystems.t9t.event.jpa.entities.SubscriberConfigEntity
import com.arvatosystems.t9t.server.services.IAuthorize
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Startup
import de.jpaw.dp.StartupOnly
import javax.persistence.EntityManager
import org.joda.time.Instant

/** The class implements a check if we are started on an empty database, and in that case, creates the global tenant and the admin user with a default password,
 * for bootstrap reasons.
 *
 * Execution of the class requires the Startup context to be active.
 */
@Startup(50010)
@AddLogger
class AuthStartup implements StartupOnly, T9tConstants {
    static final long ONE_DAY_IN_MS = 1000L * 24L * 3600L;

    @Inject Provider<PersistenceProviderJPA> jpaContextProvider

    override onStartup() {
        LOGGER.info("Auth module startup - checking for empty database")

        // need a context in case we write the the DB - entity listeners would access it
        jpaContextProvider.get => [
            entityManager => [
                // check for root tenant
                if (find(TenantEntity, GLOBAL_TENANT_REF42) === null) {
                    LOGGER.info("No global tenant exists - setting up basic configuration")
                    createGlobalTenant
                    createStartupUser
                    createAdminUser
                    createAdminRole
                    createCacheInvalidationListener
                }
            ]
            commit          // commit the transaction
        ]
    }

    def protected void createGlobalTenant(EntityManager em) {
        val tenant = new TenantEntity => [
            objectRef           = GLOBAL_TENANT_REF42
            tenantId            = GLOBAL_TENANT_ID
            name                = "Default tenant"
            isActive            = true
            permissions         = new PermissionsDTO => [
                logLevel        = UserLogLevelType.REQUESTS
                logLevelErrors  = UserLogLevelType.REQUESTS
            ]
        ]
        em.persist(tenant)
    }

    def protected void createStartupUser(EntityManager em) {
        LOGGER.info("Creating initial user {} with ref {}", STARTUP_USER_ID, STARTUP_USER_REF42)

        val user = new UserEntity => [
            objectRef               = STARTUP_USER_REF42
            userId                  = STARTUP_USER_ID
            name                    = "System startup"
            isActive                = true
            tenantRef               = GLOBAL_TENANT_REF42
            permissions             = new PermissionsDTO => [
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
                resourceRestriction = "-"       // no external access
                minPermissions      = new Permissionset(0)
                maxPermissions      = new Permissionset(0)
            ]
        ]
        em.persist(user)
    }

    def protected void createAdminUser(EntityManager em) {
        LOGGER.info("Creating initial user {} with ref {}", ADMIN_USER_ID, ADMIN_USER_REF42)

        val user = new UserEntity => [
            objectRef               = ADMIN_USER_REF42
            userId                  = ADMIN_USER_ID
            name                    = "Administrator"
            isActive                = true
            tenantRef               = GLOBAL_TENANT_REF42
            permissions             = new PermissionsDTO => [
                logLevel            = UserLogLevelType.REQUESTS
                logLevelErrors      = UserLogLevelType.REQUESTS
                resourceIsWildcard  = Boolean.TRUE
                resourceRestriction = ""       // all web services
                minPermissions      = IAuthorize.ALL_PERMISSIONS
                maxPermissions      = IAuthorize.ALL_PERMISSIONS
            ]
        ]
        em.persist(user)

        // set up an initial password
        val userStatus = new UserStatusEntity => [
            objectRef                 = ADMIN_USER_REF42
            currentPasswordSerialNumber = 1
        ]
        em.persist(userStatus)

        val now = System.currentTimeMillis
        val password = new PasswordEntity => [
            put$Key(new PasswordKey => [
                objectRef              = ADMIN_USER_REF42
                passwordSerialNumber = 1
            ])
            passwordSetByUser       = ADMIN_USER_REF42
            passwordCreation        = new Instant(now)
            passwordExpiry          = new Instant(now + 30 * ONE_DAY_IN_MS)
            userExpiry              = new Instant(now + 3650 * ONE_DAY_IN_MS)
            passwordHash            = PasswordUtil.createPasswordHash(ADMIN_USER_ID, "changeMe")
        ]
        em.persist(password)
    }

    def protected void createAdminRole(EntityManager em) {
        LOGGER.info("Creating initial role {} with ref {} and granting it to {}", ADMIN_ROLE_ID, ADMIN_ROLE_REF42, ADMIN_USER_ID)

        val role = new RoleEntity => [
            tenantRef               = GLOBAL_TENANT_REF42
            objectRef               = ADMIN_ROLE_REF42
            roleId                  = ADMIN_ROLE_ID
            name                    = "Administrator role"
            isActive                = true
        ]
        em.persist(role)

        val utr = new UserTenantRoleEntity => [
            tenantRef               = GLOBAL_TENANT_REF42
            userRef                 = ADMIN_USER_REF42
            roleRef                 = ADMIN_ROLE_REF42
        ]
        em.persist(utr)
    }

    def protected void createCacheInvalidationListener(EntityManager em) {
        LOGGER.info("Creating event listener for cache invalidation")
        val listener = new SubscriberConfigEntity => [
            tenantRef           = GLOBAL_TENANT_REF42
            objectRef           = GLOBAL_TENANT_REF42 + 1
            eventID             = InvalidateCacheEvent.BClass.INSTANCE.pqon // "t9t.base.event.InvalidateCacheEvent"
            handlerClassName    = "cacheInvalidation"
            isActive            = true
        ]
        em.persist(listener)
    }
}
