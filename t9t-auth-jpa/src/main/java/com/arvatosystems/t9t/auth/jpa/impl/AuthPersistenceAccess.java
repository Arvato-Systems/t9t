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

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.PasswordUtil;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.jpa.IPasswordChangeService;
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService;
import com.arvatosystems.t9t.auth.jpa.PermissionEntryInt;
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity;
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity;
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;

@Singleton
public class AuthPersistenceAccess implements IAuthPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPersistenceAccess.class);
    private static final List<PermissionEntry> EMPTY_PERMISSION_LIST = ImmutableList.of();
    private static final List<TenantDescription> NO_TENANTS = ImmutableList.of();

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final IUserEntityResolver userEntityResolver = Jdp.getRequired(IUserEntityResolver.class);
    protected final IPasswordChangeService passwordChangeService = Jdp.getRequired(IPasswordChangeService.class);
    protected final IPasswordSettingService passwordSettingService = Jdp.getRequired(IPasswordSettingService.class);

    // return the unfiltered permissions from DB, unfiltered means:
    // - permission min/max is not yet applied
    // - backend restrictions from JWT not yet applied
    @Override
    public List<PermissionEntry> getAllDBPermissions(final JwtInfo jwtInfo) {
        // the strategy depends on whether a role restriction has been defined (then
        // only use the role in question) and if a resource restriction has been defined
        if (jwtInfo.getRoleRef() == null) {
            return getAllPermissionsNoRole(jwtInfo);
        }
        return getAllPermissionsForRole(jwtInfo);
    }

    protected List<PermissionEntry> getAllPermissionsNoRole(final JwtInfo jwtInfo) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();

        try {
            // no role restriction - join userTenantRole with the RoleToPermission.
            // Requires grouping to avoid having entries for the same resource multiple times (but must be done in Java as SQL does not know bit operations)
            final TypedQuery<PermissionEntryInt> query = em.createQuery(
              "SELECT NEW " + PermissionEntryInt.class.getCanonicalName() + "(rtp.permissionId, rtp.permissionSet)"
                 + " FROM RoleToPermissionEntity rtp, UserTenantRoleEntity utr"
                 + " WHERE rtp.roleRef = utr.roleRef"
                 + " AND rtp.tenantRef IN :tenants"
                 + " AND utr.tenantRef IN :tenants"
                 + " AND utr.userRef = :userRef"
                 + " ORDER BY rtp.permissionId", PermissionEntryInt.class);
            final List<Long> tenants;

            if (T9tConstants.GLOBAL_TENANT_REF42.equals(jwtInfo.getTenantRef())) {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_REF42);
            } else {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_REF42, jwtInfo.getTenantRef());
            }

            query.setParameter("tenants", tenants);
            query.setParameter("userRef", jwtInfo.getUserRef());
            final List<PermissionEntryInt> results = query.getResultList();

            // postprocess: remove double entries (can this be done by the DB?)
            final List<PermissionEntry> resultsNoDups = new ArrayList<>(results.size());
            PermissionEntry pending = null;
            for (PermissionEntryInt e : results) {
                if (pending == null) {
                    pending = toExt(e);
                } else {
                    if (pending.getResourceId().equals(e.getResourceId())) {
                        // duplicate entry: combine permissions
                        pending.getPermissions().unifyWith(new Permissionset(e.getPermissions()));
                    } else {
                        // store pending and work on new
                        resultsNoDups.add(pending);
                        pending = toExt(e);
                    }
                }
            }
            if (pending != null) {
                resultsNoDups.add(pending);
            }
            return resultsNoDups;
        } catch (Exception e) {
            LOGGER.error("JPA exception {} while reading permissions for userId {} for tenantId {}: {}",
                e.getClass().getSimpleName(), jwtInfo.getUserId(), jwtInfo.getTenantId(), e.getMessage());
            return EMPTY_PERMISSION_LIST;
        }
    }

    protected List<PermissionEntry> getAllPermissionsForRole(JwtInfo jwtInfo) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TypedQuery<PermissionEntryInt> query;

        try {
            // select role to permission only
            query = em.createQuery(
              "SELECT NEW " + PermissionEntryInt.class.getCanonicalName() + "("
                 + "rtp.permissionId, rtp.permissionSet) FROM RoleToPermissionEntity rtp"
                 + " WHERE rtp.roleRef = :roleRef", PermissionEntryInt.class);
            query.setParameter("roleRef", jwtInfo.getRoleRef());
            List<PermissionEntryInt> results = query.getResultList();
            List<PermissionEntry> permissions = new ArrayList<>(results.size());
            for (PermissionEntryInt p : results) {
                permissions.add(toExt(p));
            }
            return permissions;
        } catch (Exception e) {
            LOGGER.error("JPA exception {} while reading permissions for userId {} for tenantId {}, roleRestriction {}: {}",
                e.getClass().getSimpleName(), jwtInfo.getUserId(), jwtInfo.getTenantId(), jwtInfo.getRoleRef(), e.getMessage());
            return EMPTY_PERMISSION_LIST;
        }
    }

    protected static PermissionEntry toExt(PermissionEntryInt permissionEntryInt) {
        return new PermissionEntry(permissionEntryInt.getResourceId(), new Permissionset(permissionEntryInt.getPermissions()));
    }

    @Override
    public DataWithTrackingW<UserDTO, FullTrackingWithVersion> getUserById(String userId) {
        final UserEntity userEntity = getUserIgnoringTenant(userId);
        if (userEntity == null) {
            return null;
        }
        final DataWithTrackingW<UserDTO, FullTrackingWithVersion> dwt = new DataWithTrackingW<>();
        dwt.setData(userEntity.ret$Data());
        dwt.setTenantRef(userEntity.getTenantRef());
        dwt.setTracking(userEntity.ret$Tracking());
        return dwt;
    }

    protected UserEntity getUserIgnoringTenant(String userId) {
        final TypedQuery<UserEntity> query = userEntityResolver.getEntityManager()
                .createQuery("SELECT e FROM UserEntity e WHERE e.userId = :userId", UserEntity.class);
        query.setParameter(UserDTO.meta$$userId.getName(), userId);
        final List<UserEntity> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        return results.get(0);
    }

    @Override
    public AuthIntermediateResult getByApiKey(Instant now, UUID key) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();

        final TypedQuery<ApiKeyEntity> query = em.createQuery("SELECT e FROM ApiKeyEntity e WHERE e.apiKey = :apiKey", ApiKeyEntity.class);
        query.setParameter("apiKey", key);

        final ApiKeyEntity apiKeyEntity;
        try {
            apiKeyEntity = query.getSingleResult();
        } catch (NoResultException ex) {
            LOGGER.info("Authentication request by API Key denied, no key configured for {}...", key.toString().substring(0, 18));
            return null;
        }
        if (!apiKeyEntity.getIsActive()) {
            LOGGER.info("Authentication request by API Key denied, key {}... is set to inactive", key.toString().substring(0, 18));
            return null;
        }

        final UserStatusEntity userStatus = updateUserStatusEntityForApiKeyLogin(em, apiKeyEntity, now);
        final ApiKeyDTO dto = apiKeyEntity.ret$Data();
        dto.setUserRef(apiKeyEntity.getUser().ret$Data());
        if (apiKeyEntity.getRoleRef() != null) {
            dto.setRoleRef(new RoleRef(apiKeyEntity.getRoleRef()));
        }

        final AuthIntermediateResult resp = new AuthIntermediateResult();
        resp.setAuthExpires(apiKeyEntity.getPermissions() == null ? null : apiKeyEntity.getPermissions().getValidTo());
        resp.setApiKey(dto);
        resp.setUser(apiKeyEntity.getUser().ret$Data());
        resp.setTenantRef(apiKeyEntity.getTenantRef());
        resp.setUserStatus(userStatus.ret$Data());
        return resp;
    }

    protected UserStatusEntity updateUserStatusEntityForApiKeyLogin(final EntityManager em, final ApiKeyEntity apiKeyEntity, final Instant now) {
        UserStatusEntity userStatus = em.find(UserStatusEntity.class, apiKeyEntity.getUserRef());
        if (apiKeyEntity.getPermissions() != null && UserLogLevelType.STEALTH == apiKeyEntity.getPermissions().getLogLevel()) {
            return userStatus;  // no updates
        }
        if (userStatus == null) {
            // create a new one
            userStatus = new UserStatusEntity();
            userStatus.setCurrentPasswordSerialNumber(0);
            userStatus.setNumberOfIncorrectAttempts(0);
            userStatus.setObjectRef(apiKeyEntity.getUserRef());
            em.persist(userStatus);
        }
        userStatus.setPrevLogin(userStatus.getLastLogin());
        userStatus.setPrevLoginByApiKey(userStatus.getLastLoginByApiKey());
        userStatus.setLastLogin(now);
        userStatus.setPrevLoginByApiKey(now);
        return userStatus;
    }

    @Override
    public AuthIntermediateResult getByUserIdAndPassword(Instant now, String userId, String password, String newPassword) {
        final UserEntity userEntity = getUserIgnoringTenant(userId); // first find the user
        if (userEntity == null) {
            throw new T9tException(T9tException.USER_NOT_FOUND);
        }

        // are UserTenantRoles even necessary in a jwt token context?
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TypedQuery<UserStatusEntity> userStatusQuery = em.createQuery("SELECT u FROM UserStatusEntity u WHERE u.objectRef = ?1", UserStatusEntity.class);
        userStatusQuery.setParameter(1, userEntity.getObjectRef());
        final UserStatusEntity userStatus;
        try {
            userStatus = userStatusQuery.getSingleResult(); // then check the user status if the user is frozen
            if (userStatus.getAccountThrottledUntil() != null && userStatus.getAccountThrottledUntil().isAfter(now)) {
                throw new T9tException(T9tException.ACCOUNT_TEMPORARILY_FROZEN);
            }
        } catch (NoResultException e) {
            throw new T9tException(T9tException.USER_STATUS_NOT_FOUND);
        }

        final PasswordEntity passwordEntity;
        // now check password validity
        final TypedQuery<PasswordEntity> passwordQuery = em.createQuery("SELECT p FROM PasswordEntity p WHERE p.objectRef = ?1 AND p.passwordSerialNumber = ?2",
                PasswordEntity.class);
        passwordQuery.setParameter(1, userEntity.getObjectRef());
        passwordQuery.setParameter(2, userStatus.getCurrentPasswordSerialNumber());
        try {
            passwordEntity = passwordQuery.getSingleResult(); // first retrieve password
        } catch (NoResultException e) {
            throw new T9tException(T9tException.PASSWORD_NOT_FOUND);
        }

        userStatus.setAccountThrottledUntil(null); // reset throttling
        final AuthIntermediateResult resp = new AuthIntermediateResult();
        resp.setUser(userEntity.ret$Data());
        resp.setTenantRef(userEntity.getTenantRef());
        if (userEntity.getRoleRef() != null)
            resp.getUser().setRoleRef(new RoleRef(userEntity.getRoleRef()));

        final ByteArray hash = PasswordUtil.createPasswordHash(userId, password);
        // password is correct & not expired OR password is correct, expired,
        // newPassword provided
        if (passwordEntity.getPasswordHash().equals(hash)) {
            // auth was successful
            // OK if password is still valid, or if user is providing a new one now (this
            // means if the password is no longer valid, the user MUST change it while
            // authenticating)
            final boolean gotANewPasswordNow = newPassword != null && !newPassword.trim().isEmpty();
            if (passwordEntity.getPasswordExpiry().isAfter(now) || gotANewPasswordNow) {
                if (gotANewPasswordNow) {
                    // we want to change our password: reject if the new one does not satisfy
                    // checking criteria, otherwise accept
                    passwordChangeService.changePassword(newPassword, userEntity, userStatus);
                }
                // login success
            } else {
                // password has expired and no new one was supplied
                resp.setReturnCode(T9tException.PASSWORD_EXPIRED); // must change password
            }
            userStatus.setNumberOfIncorrectAttempts(0); // reset attempt counter
            userStatus.setPrevLogin(userStatus.getLastLogin());
            userStatus.setPrevLoginByPassword(userStatus.getLastLoginByPassword());
            userStatus.setLastLogin(now);
            userStatus.setLastLoginByPassword(now);
            resp.setUserStatus(userStatus.ret$Data());
            resp.setAuthExpires(passwordEntity.getPasswordExpiry());
            return resp;
        } else {
            // incorrect auth: increment attemptCounter
            userStatus.setNumberOfIncorrectAttempts(userStatus.getNumberOfIncorrectAttempts() + 1);
            if (userStatus.getNumberOfIncorrectAttempts() >= 5) // TODO: configurable
                userStatus.setAccountThrottledUntil(now.plusSeconds(5 * 60)); // 5 minutes
            resp.setReturnCode(T9tException.WRONG_PASSWORD);
            resp.setUserStatus(userStatus.ret$Data());
            return resp;
        }
    }

    @Override
    public void storeSession(SessionDTO session) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.put$Data(session);
        sessionEntity.put$Key(session.getObjectRef());
        em.persist(sessionEntity);
    }

    private static TenantDescription mapToTenantDescription(TenantEntity t) {
        final TenantDescription it = new TenantDescription();
        it.setTenantRef(t.getObjectRef());
        it.setTenantId(t.getTenantId());
        it.setName(t.getName());
        it.setIsActive(t.getIsActive());
        return it;
    }

    @Override
    public List<TenantDescription> getAllTenantsForUser(RequestContext ctx, Long userRef) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final UserEntity userEntity = em.find(UserEntity.class, userRef);
        if (userEntity == null)
            return ImmutableList.of();
        if (!userEntity.getTenantRef().equals(T9tConstants.GLOBAL_TENANT_REF42)) {
            // single tenant, defined by user
            final TenantEntity tenantEntity = em.find(TenantEntity.class, userEntity.getTenantRef());
            if (tenantEntity == null) {
                LOGGER.error("User {} maps to non existent tenant of ref {}", userEntity.getUserId(), userEntity.getTenantRef());
                return NO_TENANTS;
            } else {
                LOGGER.debug("Single possible tenant for userId {} is {} due to setting on UserEntity", userEntity.getUserId(), tenantEntity.getTenantId());
                return ImmutableList.of(mapToTenantDescription(tenantEntity));
            }
        }
        if (userEntity.getRoleRef() != null) {
            // assignment of a fixed role but no tenant: this role is valid for all tenants,
            // unless the role defines the tenant
            final RoleEntity roleEntity = em.find(RoleEntity.class, userEntity.getRoleRef());
            if (roleEntity == null) {
                LOGGER.error("User {} maps to non existent role of ref {}", userEntity.getUserId(), userEntity.getRoleRef());
                return NO_TENANTS;
            }
            if (!roleEntity.getTenantRef().equals(T9tConstants.GLOBAL_TENANT_REF42)) {
                // single tenant, defined by role
                final TenantEntity tenantEntity = em.find(TenantEntity.class, roleEntity.getTenantRef());
                if (tenantEntity == null) {
                    LOGGER.error("User {} maps to role {} maps to non existent tenant of ref {}", userEntity.getUserId(), roleEntity.getRoleId(),
                            roleEntity.getTenantRef());
                    return NO_TENANTS;
                } else {
                    LOGGER.debug("Single possible tenant for userId {} is {} due to assignment to role {} on UserEntity", userEntity.getUserId(),
                            tenantEntity.getTenantId(), roleEntity.getRoleId());
                    return ImmutableList.of(mapToTenantDescription(tenantEntity));
                }
            }
            LOGGER.debug("Access to all tenants allowed for userId {} due to assignment to global role {} on UserEntity", userEntity.getUserId(),
                    roleEntity.getRoleId());
            return listOfAllTenants(em);
        }
        // approach using userTenantRole
        LOGGER.debug("Obtaining valid tenants for userId {} via UserTenantRole assignment", userEntity.getUserId());
        final TypedQuery<TwoTenantRefs> query = em.createQuery(
                "SELECT NEW " + TwoTenantRefs.class.getCanonicalName() + "(utr.tenantRef, r.tenantRef) FROM UserTenantRoleEntity utr, RoleEntity r "
                + "WHERE utr.userRef = :userRef " + "AND utr.roleRef = r.objectRef",
                TwoTenantRefs.class);
        query.setParameter("userRef", userRef);

        // build the set of allowed tenants.
        // the set consists of all tenants if at least one assignment is the global
        // tenant for both references
        final List<TwoTenantRefs> results = query.getResultList();

        LOGGER.debug("Found {} user / tenant / role assignments", results.size());

        final Set<Long> allValidTenantRefs = new HashSet<>(results.size());
        for (TwoTenantRefs r : results) {
            LOGGER.trace("Found tenant pair {}", r);

            if (r.isDoubleGlobal()) {
                LOGGER.debug("Access to all tenants allowed for userId {} due to unrestricted assignment to global role", userEntity.getUserId());
                return listOfAllTenants(em);
            }

            allValidTenantRefs.add(r.effectiveTenantRef());
        }

        if (LOGGER.isDebugEnabled()) {
            final String tenantList = String.join(", ", allValidTenantRefs.stream().map(i -> i.toString()).collect(Collectors.toList()));
            LOGGER.debug("UserId {} has selective access to tenants {} due to specific role assignments", userEntity.getUserId(), tenantList);
        }

        if (allValidTenantRefs.isEmpty()) {
            // no tenant can be accessed via roles. The user is the global tenant. If
            // resources have been defined via "resourceIsWildcard", then also all tenants
            // apply (admin)
            if (Boolean.TRUE.equals(userEntity.getPermissions() == null ? false : userEntity.getPermissions().getResourceIsWildcard())) {
                return listOfAllTenants(em);
            }
            // not the case "resourceIsWildcard": In this case we assume a user "under
            // construction" with no roles / tenants assigned yet
            return NO_TENANTS;
        }
        final TypedQuery<TenantEntity> query2 = em.createQuery("SELECT t FROM TenantEntity t WHERE t.objectRef IN :tenants", TenantEntity.class);
        query2.setParameter("tenants", allValidTenantRefs);
        final List<TenantEntity> result = query2.getResultList();
        final List<TenantDescription> tenantDescriptions = new ArrayList<>(result.size());
        for (TenantEntity t : result) {
            tenantDescriptions.add(mapToTenantDescription(t));
        }
        return tenantDescriptions;
    }

    private static List<TenantDescription> listOfAllTenants(EntityManager em) {
        final TypedQuery<TenantEntity> query = em.createQuery("SELECT t FROM TenantEntity t", TenantEntity.class);
        final List<TenantEntity> result = query.getResultList();
        final List<TenantDescription> descriptions = new ArrayList<>(result.size());
        for (TenantEntity t : result) {
            descriptions.add(mapToTenantDescription(t));
        }
        return descriptions;
    }

    @Override
    public Map<String, Object> getUserZ(final Long userRef) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final UserEntity userEntity = em.find(UserEntity.class, userRef);
        return userEntity == null ? null : userEntity.getZ();
    }

    @Override
    public Map<String, Object> getTenantZ(final Long tenantRef) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TenantEntity tenantEntity = em.find(TenantEntity.class, tenantRef);
        return tenantEntity == null ? null : tenantEntity.getZ();
    }

    @Override
    public String assignNewPasswordIfEmailMatches(final RequestContext ctx, final String userId, final String emailAddress) {
        final UserEntity userEntity = getUserIgnoringTenant(userId);
        if (userEntity == null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // user does not exist
        }

        // validate that the user is active and that the email address matches
        if (!userEntity.getIsActive() || !emailAddress.equalsIgnoreCase(userEntity.getEmailAddress())) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // wrong email address
        }

        // checks OK, proceed
        // The request creates an initial random password for the specified user, or
        // creates a new password for that user.
        final String newPassword = PasswordUtil.generateRandomPassword(T9tConstants.DEFAULT_RANDOM_PASS_LENGTH);

        passwordSettingService.setPasswordForUser(ctx, userEntity, newPassword);
        return newPassword;
    }
}
