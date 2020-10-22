/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.PasswordUtil
import com.arvatosystems.t9t.auth.RoleRef
import com.arvatosystems.t9t.auth.SessionDTO
import com.arvatosystems.t9t.auth.jpa.PermissionEntryInt
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.authc.api.TenantDescription
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.auth.PermissionEntry
import com.arvatosystems.t9t.base.services.RequestContext
import com.google.common.collect.ImmutableList
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.dp.Inject
import de.jpaw.dp.Provider
import de.jpaw.dp.Singleton
import java.util.ArrayList
import java.util.List
import java.util.UUID
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.persistence.TypedQuery
import org.joda.time.Instant

@AddLogger
@Singleton
class AuthPersistenceAccess implements IAuthPersistenceAccess, T9tConstants {
    static final List<PermissionEntry> EMPTY_PERMISSION_LIST = ImmutableList.of();

    @Inject Provider<PersistenceProviderJPA> jpaContextProvider

    @Inject IUserEntityResolver userEntityResolver

    @Inject PasswordChangeService passwordChangeService

    // return the unfiltered permissions from DB
    // unfiltered means:
    // - permission min/max is not yet applied
    // - backend restrictions from JWT not yet applied
    override getAllDBPermissions(JwtInfo jwtInfo) {

        // the strategy depends on whether a role restriction has been defined (then only use the role in question) and if a resource restriction has been defined
        if (jwtInfo.roleRef === null) {
            return getAllPermissionsNoRole(jwtInfo)
        } else {
            return getAllPermissionsForRole(jwtInfo)
        }
    }

    def protected List<PermissionEntry> getAllPermissionsForRole(JwtInfo jwtInfo) {
        val em = jpaContextProvider.get.entityManager
        var TypedQuery<PermissionEntryInt> query

        try {
            // select role to permission only
            query = em.createQuery("SELECT NEW com.arvatosystems.t9t.auth.jpa.PermissionEntryInt("
                                 + "rtp.permissionId, rtp.permissionSet) FROM RoleToPermissionEntity rtp"
                                 + " WHERE rtp.roleRef = :roleRef", PermissionEntryInt)
            query.setParameter("roleRef", jwtInfo.roleRef)
            return query.resultList.map[toExt]
        } catch (Exception e) {
            LOGGER.error("JPA exception {} while reading permissions for userId {} for tenantId {}, roleRestriction {}: {}",
                e.class.simpleName, jwtInfo.userId, jwtInfo.tenantId, jwtInfo.roleRef, e.message);
            return EMPTY_PERMISSION_LIST
        }
    }

    def static protected PermissionEntry toExt(PermissionEntryInt it) {
        return new PermissionEntry(resourceId, new Permissionset(permissions))
    }

    def protected List<PermissionEntry> getAllPermissionsNoRole(JwtInfo jwtInfo) {
        val em = jpaContextProvider.get.entityManager
        var TypedQuery<PermissionEntryInt> query

        try {
            // no role restriction - join userTenantRole with the RoleToPermission. Requires grouping to avoid having entries for the same resource multiple times (but must be done in Java as SQL does not know bit operations)
            query = em.createQuery("SELECT NEW com.arvatosystems.t9t.auth.jpa.PermissionEntryInt(rtp.permissionId, rtp.permissionSet)"
                                 + " FROM RoleToPermissionEntity rtp, UserTenantRoleEntity utr"
                                 + " WHERE rtp.roleRef = utr.roleRef"
                                 + " AND rtp.tenantRef IN :tenants"
                                 + " AND utr.tenantRef IN :tenants"
                                 + " AND utr.userRef = :userRef"
                                 + " ORDER BY rtp.permissionId", PermissionEntryInt)
            val tenants = if (GLOBAL_TENANT_REF42.equals(jwtInfo.tenantRef)) #[ GLOBAL_TENANT_REF42 ] else #[ GLOBAL_TENANT_REF42, jwtInfo.tenantRef ]
            query.setParameter("tenants", tenants)
            query.setParameter("userRef", jwtInfo.userRef)
            val results = query.resultList

            // postprocess: remove double entries (can this be done by the DB?)
            val resultsNoDups = new ArrayList<PermissionEntry>(results.size)
            var PermissionEntry pending = null
            for (e : results) {
                if (pending === null) {
                    pending = e.toExt
                } else {
                    if (pending.resourceId == e.resourceId) {
                        // duplicate entry: combine permissions
                        pending.permissions.unifyWith(new Permissionset(e.permissions))
                    } else {
                        // store pending and work on new
                        resultsNoDups.add(pending)
                        pending = e.toExt
                    }
                }
            }
            if (pending !== null) {
                resultsNoDups.add(pending)
            }
            return resultsNoDups
        } catch (Exception e) {
            LOGGER.error("JPA exception {} while reading permissions for userId {} for tenantId {}: {}",
                e.class.simpleName, jwtInfo.userId, jwtInfo.tenantId, e.message);
            return EMPTY_PERMISSION_LIST
        }
    }




    override getByApiKey(Instant now, UUID key) {
        val em = jpaContextProvider.get.entityManager

        var TypedQuery<ApiKeyEntity> query = em.createQuery("SELECT e FROM ApiKeyEntity e WHERE e.apiKey = :apiKey", ApiKeyEntity);
        query.setParameter("apiKey", key);

        var ApiKeyEntity a
        try {
            a = query.getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.info("Authentication request by API Key denied, no key configured for {}...", key.toString.substring(0, 18))
            return null;
        }
        if (!a.isActive) {
            LOGGER.info("Authentication request by API Key denied, key {}... is set to inactive", key.toString.substring(0, 18))
            return null;
        }

        val userStatus = updateUserStatusEntityForApiKeyLogin(em, a, now)
        val dto = a.ret$Data
        dto.userRef = a.user.ret$Data
        if (a.roleRef !== null)
            dto.roleRef = new RoleRef(a.roleRef)

        val resp = new AuthIntermediateResult
        resp.authExpires   = a.permissions?.validTo
        resp.apiKey        = dto
        resp.user          = a.user.ret$Data
        resp.tenantRef     = a.tenantRef
        resp.userStatus    = userStatus.ret$Data
        return resp
    }

    /** Update the timestamp in the user status with the current login data. */
    def protected updateUserStatusEntityForApiKeyLogin(EntityManager em, ApiKeyEntity a, Instant now) {
        var UserStatusEntity userStatus = em.find(UserStatusEntity, a.userRef)
        if (userStatus === null) {
            // create a new one
            userStatus = new UserStatusEntity
            userStatus.currentPasswordSerialNumber = 0
            userStatus.numberOfIncorrectAttempts   = 0
            userStatus.objectRef = a.userRef
            em.persist(userStatus)
        }
        userStatus.prevLogin                 = userStatus.lastLogin
        userStatus.prevLoginByApiKey         = userStatus.lastLoginByApiKey
        userStatus.lastLogin                 = now
        userStatus.prevLoginByApiKey         = now
        return userStatus
    }

    def protected UserEntity getUserIgnoringTenant(String userId) {
        val query = userEntityResolver.getEntityManager().createQuery("SELECT e FROM UserEntity e WHERE e.userId = :userId", UserEntity);
        query.setParameter("userId", userId);
        val results = query.getResultList();
        if (results.empty) {
            return null;
        }
        return results.get(0);
    }

    override getUserById(String userId) {
        val userEntity = getUserIgnoringTenant(userId)
        if (userEntity === null) {
            return null;
        }
        return Pair.of(userEntity.tenantRef, userEntity.ret$Data)
    }

    override getByUserIdAndPassword(Instant now, String userId, String password, String newPassword) {
        val userEntity = getUserIgnoringTenant(userId) // first find the user
        if (userEntity === null) {
            throw new T9tException(T9tException.USER_NOT_FOUND)
        }

        // are UserTenantRoles even necessary in a jwt token context?

        val em = jpaContextProvider.get.entityManager
        val TypedQuery<UserStatusEntity> userStatusQuery = em.createQuery("SELECT u FROM UserStatusEntity u WHERE u.objectRef = ?1", UserStatusEntity)
        userStatusQuery.setParameter(1, userEntity.objectRef)
        var UserStatusEntity userStatus
        try {
            userStatus = userStatusQuery.singleResult // then check the user status if the user is frozen
            if (userStatus.accountThrottledUntil !== null && userStatus.accountThrottledUntil.isAfterNow) {
                throw new T9tException(T9tException.ACCOUNT_TEMPORARILY_FROZEN)
            }
        } catch (NoResultException e) {
            throw new T9tException(T9tException.USER_STATUS_NOT_FOUND)
        }

        var PasswordEntity passwordEntity
        // now check password validity
        val TypedQuery<PasswordEntity> passwordQuery = em.createQuery("SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 AND p.passwordSerialNumber = ?2", PasswordEntity)
        passwordQuery.setParameter(1, userEntity.objectRef)
        passwordQuery.setParameter(2, userStatus.currentPasswordSerialNumber)
        try {
            passwordEntity = passwordQuery.singleResult  // first retrieve password
        } catch (NoResultException e) {
            throw new T9tException(T9tException.PASSWORD_NOT_FOUND)
        }

        userStatus.accountThrottledUntil = null   // reset throttling
        val resp = new AuthIntermediateResult
        resp.user          = userEntity.ret$Data
        resp.tenantRef     = userEntity.tenantRef
        if (userEntity.roleRef !== null)
            resp.user.roleRef  = new RoleRef(userEntity.roleRef)

        val hash = PasswordUtil.createPasswordHash(userId, password)
        // password is correct & not expired OR password is correct, expired, newPassword provided
        if (passwordEntity.getPasswordHash.equals(hash)) {
            // auth was successful
            // OK if password is still valid, or if user is providing a new one now (this means if the password is no longer valid, the user MUST change it while authenticating)
            val gotANewPasswordNow = newPassword !== null && !newPassword.trim.isEmpty
            if (passwordEntity.passwordExpiry.afterNow || gotANewPasswordNow) {
                if (gotANewPasswordNow) {
                    // we want to change our password: reject if the new one does not satisfy checking criteria, otherwise accept
                    passwordChangeService.changePassword(newPassword, userEntity, userStatus)
                }
                // login success
            } else {
                // password has expired and no new one was supplied
                resp.returnCode                  = T9tException.PASSWORD_EXPIRED;  // must change password
            }
            userStatus.numberOfIncorrectAttempts = 0;  // reset attempt counter
            userStatus.prevLogin                 = userStatus.lastLogin
            userStatus.prevLoginByPassword       = userStatus.lastLoginByPassword
            userStatus.lastLogin                 = now
            userStatus.lastLoginByPassword       = now
            resp.userStatus                      = userStatus.ret$Data
            resp.authExpires                     = passwordEntity.passwordExpiry
            return resp
        } else {
            // incorrect auth: increment attemptCounter
            userStatus.numberOfIncorrectAttempts = userStatus.numberOfIncorrectAttempts + 1
            if (userStatus.numberOfIncorrectAttempts >= 5) // TODO: configurable
                userStatus.accountThrottledUntil = now.plus(5 * 60 * 1000)  // 5 minutes
            resp.returnCode                = T9tException.WRONG_PASSWORD
            resp.userStatus                = userStatus.ret$Data
            return resp
        }
    }

    override storeSession(SessionDTO session) {
        val em = jpaContextProvider.get.entityManager
        val sessionEntity = new SessionEntity
        sessionEntity.put$Data(session)
        sessionEntity.put$Key(session.objectRef)
        em.persist(sessionEntity)
    }

    def private static mapToTenantDescription(TenantEntity t) {
        val it = new TenantDescription
        tenantRef       = t.objectRef
        tenantId        = t.tenantId
        name            = t.name
        isActive        = t.isActive
        return it
    }

    def private static listOfAllTenants(EntityManager em) {
        val query = em.createQuery("SELECT t FROM TenantEntity t", TenantEntity)
        return query.resultList.map[mapToTenantDescription(it)].toList
    }

    static final List<TenantDescription> NO_TENANTS = ImmutableList.of()

    // select all tenants for which the user has a role assigned to, either directly or UserTenantRole
    override getAllTenantsForUser(RequestContext ctx, Long userRef) {
        val em = jpaContextProvider.get.entityManager
        val userEntity = em.find(UserEntity, userRef)
        if (userEntity === null)
            return #[]
        if (userEntity.tenantRef != T9tConstants.GLOBAL_TENANT_REF42) {
            // single tenant, defined by user
            val tenantEntity = em.find(TenantEntity, userEntity.tenantRef)
            if (tenantEntity === null) {
                LOGGER.error("User {} maps to non existent tenant of ref {}", userEntity.userId, userEntity.tenantRef)
                return NO_TENANTS
            } else {
                LOGGER.debug("Single possible tenant for userId {} is {} due to setting on UserEntity", userEntity.userId, tenantEntity.tenantId)
                return #[ mapToTenantDescription(tenantEntity) ]
            }
        }
        if (userEntity.roleRef !== null) {
            // assignment of a fixed role but no tenant: this role is valid for all tenants, unless the role defines the tenant
            val roleEntity = em.find(RoleEntity, userEntity.roleRef)
            if (roleEntity === null) {
                LOGGER.error("User {} maps to non existent role of ref {}", userEntity.userId, userEntity.roleRef)
                return NO_TENANTS
            }
            if (roleEntity.tenantRef != T9tConstants.GLOBAL_TENANT_REF42) {
                // single tenant, defined by role
                val tenantEntity = em.find(TenantEntity, roleEntity.tenantRef)
                if (tenantEntity === null) {
                    LOGGER.error("User {} maps to role {} maps to non existent tenant of ref {}", userEntity.userId, roleEntity.roleId, roleEntity.tenantRef)
                    return NO_TENANTS
                } else {
                    LOGGER.debug("Single possible tenant for userId {} is {} due to assignment to role {} on UserEntity", userEntity.userId, tenantEntity.tenantId, roleEntity.roleId)
                    return #[ mapToTenantDescription(tenantEntity) ]
                }
            }
            LOGGER.debug("Access to all tenants allowed for userId {} due to assignment to global role {} on UserEntity", userEntity.userId, roleEntity.roleId)
            return em.listOfAllTenants
        }
        // approach using userTenantRole
        LOGGER.debug("Obtaining valid tenants for userId {} via UserTenantRole assignment", userEntity.userId)
        val query = em.createQuery(
            "SELECT NEW com.arvatosystems.t9t.auth.jpa.impl.TwoTenantRefs(utr.tenantRef, r.tenantRef) FROM UserTenantRoleEntity utr, RoleEntity r " +
            "WHERE utr.userRef = :userRef " +
            "AND utr.roleRef = r.objectRef", TwoTenantRefs)
        query.setParameter("userRef", userRef)

        // build the set of allowed tenants.
        // the set consists of all tenants if at least one assignment is the global tenant for both references
        val results = query.resultList

        LOGGER.debug("Found {} user / tenant / role assignments", results.size)
        for (r : results)
            LOGGER.trace("    Found tenant pair {}", r)

        if (results.exists[isDoubleGlobal]) {
            // return the list of all tenants
            LOGGER.debug("Access to all tenants allowed for userId {} due to unrestricted assignment to global role", userEntity.userId)
            return em.listOfAllTenants
        }
        val allValidTenantRefs = results.map[effectiveTenantRef].toSet
        if (LOGGER.isDebugEnabled) {
            val tenantList = allValidTenantRefs.join(', ')
            LOGGER.debug("UserId {} has selective access to tenants {} due to specific role assignments", userEntity.userId, tenantList)
        }
        if (allValidTenantRefs.empty) {
            // no tenant can be accessed via roles. The user is the global tenant. If resources have been defined via "resourceIsWildcard", then also all tenants apply (admin)
            if (Boolean.TRUE == userEntity.permissions?.resourceIsWildcard)
                return em.listOfAllTenants
            // not the case "resourceIsWildcard": In this case we assume a user "under construction" with no roles / tenants assigned yet
            return NO_TENANTS
        }
        val query2 = em.createQuery("SELECT t FROM TenantEntity t WHERE t.objectRef IN :tenants", TenantEntity)
        query2.setParameter("tenants", allValidTenantRefs)
        return query2.resultList.map[mapToTenantDescription(it)].toList
    }

    override getTenantZ(Long tenantRef) {
        val em = jpaContextProvider.get.entityManager
        val tenantEntity = em.find(TenantEntity, tenantRef)
        return tenantEntity?.z
    }

    override getUserZ(Long userRef) {
        val em = jpaContextProvider.get.entityManager
        val userEntity = em.find(UserEntity, userRef)
        return userEntity?.z
    }
}
