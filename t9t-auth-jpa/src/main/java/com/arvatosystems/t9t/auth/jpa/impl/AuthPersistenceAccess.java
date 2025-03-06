/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.auth.OidClaims;
import com.arvatosystems.t9t.auth.PasswordUtil;
import com.arvatosystems.t9t.auth.RoleRef;
import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.auth.UserDTO;
import com.arvatosystems.t9t.auth.jpa.IPasswordChangeService;
import com.arvatosystems.t9t.auth.jpa.IPasswordSettingService;
import com.arvatosystems.t9t.auth.jpa.PermissionEntryInt;
import com.arvatosystems.t9t.auth.jpa.entities.ApiKeyEntity;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordBlacklistEntity;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.RoleEntity;
import com.arvatosystems.t9t.auth.jpa.entities.SessionEntity;
import com.arvatosystems.t9t.auth.jpa.entities.TenantEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserStatusEntity;
import com.arvatosystems.t9t.auth.jpa.mapping.IUserEntity2UserDataMapper;
import com.arvatosystems.t9t.auth.jpa.persistence.IUserEntityResolver;
import com.arvatosystems.t9t.auth.services.AuthIntermediateResult;
import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.auth.services.IExternalTokenValidation;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.authc.api.UserData;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.ExternalTokenAuthenticationParam;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.OidConfiguration;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.jpa.refs.PersistenceProviderJPA;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.OperationTypes;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

@Singleton
public class AuthPersistenceAccess implements IAuthPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthPersistenceAccess.class);
    private static final List<PermissionEntry> EMPTY_PERMISSION_LIST = ImmutableList.of();
    private static final List<TenantDescription> NO_TENANTS = ImmutableList.of();
    private static final List<UserData> EMPTY_USER_LIST = ImmutableList.of();
    private static final String GET_CURRENT_PASSWORD = "SELECT pwd FROM " + UserStatusEntity.class.getSimpleName() + " us JOIN "
        + PasswordEntity.class.getSimpleName() + " pwd ON us.objectRef = pwd.objectRef AND us.currentPasswordSerialNumber = pwd.passwordSerialNumber"
        + " WHERE us.objectRef = :userRef";

    protected final OidConfiguration oidConfiguration = ConfigProvider.getConfiguration().getOidConfiguration();

    protected final Provider<PersistenceProviderJPA> jpaContextProvider = Jdp.getProvider(PersistenceProviderJPA.class);
    protected final IUserEntityResolver userEntityResolver = Jdp.getRequired(IUserEntityResolver.class);
    protected final IPasswordChangeService passwordChangeService = Jdp.getRequired(IPasswordChangeService.class);
    protected final IPasswordSettingService passwordSettingService = Jdp.getRequired(IPasswordSettingService.class);
    protected final IExternalTokenValidation externalTokenAuthentication = Jdp.getRequired(IExternalTokenValidation.class);
    protected final IUserEntity2UserDataMapper userEntity2UserDataMapper = Jdp.getRequired(IUserEntity2UserDataMapper.class);

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
                  + " AND rtp.tenantId IN :tenants"
                  + " AND utr.tenantId IN :tenants"
                  + " AND utr.userRef = :userRef"
                  + " ORDER BY rtp.permissionId", PermissionEntryInt.class);
            final List<String> tenants;

            if (T9tConstants.GLOBAL_TENANT_ID.equals(jwtInfo.getTenantId())) {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_ID);
            } else {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_ID, jwtInfo.getTenantId());
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
    public DataWithTrackingS<UserDTO, FullTrackingWithVersion> getUserById(String userId) {
        final UserEntity userEntity = getUserByUserIdIgnoringTenant(userId, false);
        if (userEntity == null) {
            return null;
        }
        final DataWithTrackingS<UserDTO, FullTrackingWithVersion> dwt = new DataWithTrackingS<>();
        dwt.setData(userEntity.ret$Data());
        dwt.setTenantId(userEntity.getTenantId());
        dwt.setTracking(userEntity.ret$Tracking());
        return dwt;
    }

    protected UserEntity getUserByUserIdIgnoringTenant(@Nonnull final String userId, final boolean useExtId) {
        final String sql = useExtId
            ? "SELECT e FROM UserEntity e WHERE e.userIdExt = :userId AND e.isActive = :isActive"
            : "SELECT e FROM UserEntity e WHERE e.userId = :userId";
        final TypedQuery<UserEntity> query = userEntityResolver.getEntityManager()
                .createQuery(sql, UserEntity.class);
        query.setParameter("userId", userId);
        if (useExtId) {
            query.setParameter("isActive", Boolean.TRUE);
        }
        final List<UserEntity> results = query.getResultList();
        if (results.isEmpty()) {
            return null;
        }
        if (results.size() > 1) {
            // can only occur for search by userIdExt
            LOGGER.error("nonunique active external user ID {}", userId);
            return null;
        }
        return results.get(0);
    }

    /** Perform unessential updates of claims such as name / phone / email. */
    protected void updateUserEntity(final UserEntity userEntity, final OidClaims externalTokenData) {
        final String nameClaim = externalTokenData.getName();
        if (nameClaim != null && Boolean.TRUE.equals(oidConfiguration.getUpdateName() && !nameClaim.equals(userEntity.getName()))) {
            LOGGER.info("Updating name of user {} to {}", userEntity.getUserId(), nameClaim);
            userEntity.setName(MessagingUtil.truncField(nameClaim, 80));
        }
        final String emailClaim = externalTokenData.getEmailAddress();
        if (emailClaim != null && Boolean.TRUE.equals(oidConfiguration.getUpdateEmail() && !emailClaim.equals(userEntity.getEmailAddress()))) {
            LOGGER.info("Updating email of user {} to {}", userEntity.getUserId(), emailClaim);
            userEntity.setEmailAddress(emailClaim);
        }
    }

    @Override
    public AuthIntermediateResult getByExternalToken(final Instant now, final ExternalTokenAuthenticationParam authParam) {
        final OidClaims externalTokenData = externalTokenAuthentication.validateToken(authParam.getAccessToken());
        if (externalTokenData == null) {
            LOGGER.debug("Authentication request by external token is denied, because token is invalid");
            return null;
        }

        UserEntity userEntity = null;
        // first, attempt to obtain user by OID
        final String oidClaim = externalTokenData.getOid();
        if (oidClaim != null) {
            userEntity = getUserByUserIdIgnoringTenant(oidClaim, true);
        }

        // if no user found, or oid was not provided, attempt via userId / upn
        final String upnClaim = externalTokenData.getUpn();
        if (userEntity == null && upnClaim != null && upnClaim.indexOf('@') > 0) {
            // attempt via primary user ID
            final int atInUpnClaim = upnClaim.indexOf('@');
            final String userIdInUpn = upnClaim.substring(0, atInUpnClaim);
            userEntity = getUserByUserIdIgnoringTenant(userIdInUpn, false);
            if (userEntity != null) {
                // must match either email or idp!
                if (userEntity.getIdentityProvider() != null) {
                    // in this case ignore email!
                    if (!userEntity.getIdentityProvider().equals(externalTokenData.getIdp())) {
                        LOGGER.warn("Authentication rejected for userId {} because idp not matching: {} vs {}",
                            userEntity.getUserId(), userEntity.getIdentityProvider(), externalTokenData.getIdp());
                        return null;
                    }
                } else if (userEntity.getEmailAddress() != null) {
                    // fallback: no idp configured, attempt match by email domain
                    final int atInEmail = userEntity.getEmailAddress().indexOf('@');
                    if (atInEmail > 0 && atInEmail < userEntity.getEmailAddress().length() - 3) {
                        final String domainOfEmail = userEntity.getEmailAddress().substring(atInEmail + 1);
                        final String domainOfUpn = upnClaim.substring(atInUpnClaim + 1);
                        if (!domainOfUpn.equals(domainOfEmail)) {
                            LOGGER.warn("Authentication rejected for userId {} because email domain mismatches", userEntity.getUserId());
                            return null;
                        }
                    } else {
                        LOGGER.warn("Authentication rejected for userId {} because email configured without domain", userEntity.getUserId());
                        return null;
                    }
                } else {
                    LOGGER.warn("Authentication rejected for userId {} because no idp or email configured for this user", userEntity.getUserId());
                    return null;
                }
            }
        }
        if (userEntity == null) {
            LOGGER.debug("No user found for oid {} / upn {}", oidClaim, upnClaim);
            return null;
        }
        if (!Boolean.TRUE.equals(userEntity.getExternalAuth())) {
            LOGGER.debug("Authentication request by external token for user {} is denied, because user is not allowed for external authentication",
                userEntity.getUserId());
            return null;
        }
        // further updated / checks
        if (userEntity.getIdentityProvider() == null) {
            if (oidConfiguration.getUpdateIdp()) {
                userEntity.setIdentityProvider(externalTokenData.getIdp());
            }
        } else if (oidConfiguration.getMustMatchIdp()) {
            // similar code before only covered the userId matching case
            if (!userEntity.getIdentityProvider().equals(externalTokenData.getIdp())) {
                LOGGER.warn("Authentication rejected for userId {} because idp not matching: {} vs {}",
                    userEntity.getUserId(), userEntity.getIdentityProvider(), externalTokenData.getIdp());
                return null;
            }
        }

        if (oidConfiguration != null) {
            if (userEntity.getUserIdExt() == null && oidClaim != null && oidConfiguration.getUpdateOid()) {
                if (oidClaim.length() > 36) {
                    LOGGER.warn("Cannot update userIdExt of user {} with OID {} (OID too long)", userEntity.getUserId(), oidClaim);
                } else {
                    LOGGER.info("Updating userIdExt of user {} with OID {}", userEntity.getUserId(), oidClaim);
                    userEntity.setUserIdExt(oidClaim);
                }
            }
            updateUserEntity(userEntity, externalTokenData);
        }

        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final UserStatusEntity userStatus = updateUserStatusEntityForExternalTokenLogin(em, userEntity.getObjectRef(), now);

        final AuthIntermediateResult resp = new AuthIntermediateResult();
        resp.setUser(userEntity.ret$Data());
        resp.setTenantId(userEntity.getTenantId());
        if (userEntity.getRoleRef() != null) {
            resp.getUser().setRoleRef(new RoleRef(userEntity.getRoleRef()));
        }
        resp.setUserStatus(userStatus.ret$Data());

        return resp;
    }

    @Override
    public AuthIntermediateResult getByApiKey(final Instant now, final UUID key) {
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
        resp.setTenantId(apiKeyEntity.getTenantId());
        resp.setUserStatus(userStatus.ret$Data());
        return resp;
    }

    protected UserStatusEntity updateUserStatusEntityForApiKeyLogin(final EntityManager em, final ApiKeyEntity apiKeyEntity, final Instant now) {
        UserStatusEntity userStatus = em.find(UserStatusEntity.class, apiKeyEntity.getUserRef());
        if (apiKeyEntity.getPermissions() != null && UserLogLevelType.STEALTH == apiKeyEntity.getPermissions().getLogLevel()) {
            return userStatus; // no updates
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
        userStatus.setLastLoginByApiKey(now);
        return userStatus;
    }

    protected UserStatusEntity updateUserStatusEntityForExternalTokenLogin(final EntityManager em, final Long userRef, final Instant now) {
        UserStatusEntity userStatus = em.find(UserStatusEntity.class, userRef);
        if (userStatus == null) {
            // create a new one
            userStatus = new UserStatusEntity();
            userStatus.setCurrentPasswordSerialNumber(0);
            userStatus.setNumberOfIncorrectAttempts(0);
            userStatus.setObjectRef(userRef);
            em.persist(userStatus);
        }
        userStatus.setPrevLogin(userStatus.getLastLogin());
        userStatus.setPrevLoginByX509(userStatus.getLastLoginByX509());
        userStatus.setLastLogin(now);
        userStatus.setLastLoginByX509(now);
        return userStatus;
    }

    @Override
    public AuthIntermediateResult getByUserIdAndPassword(final Instant now, final String userId, final String password, final String newPassword) {
        final UserEntity userEntity = getUserByUserIdIgnoringTenant(userId, false); // first find the user
        if (userEntity == null) {
            throw new T9tException(T9tException.USER_NOT_FOUND);
        }
        if (Boolean.TRUE.equals(userEntity.getOnlyExternalAuth())) {
            LOGGER.warn("Authentication via internal password rejected for user {}, because only external auth allowed", userId);
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
        resp.setTenantId(userEntity.getTenantId());
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
            updateUserStatusEntityForSuccessPasswordLogin(userStatus, now);
            // Clear generated reset password, if there is any. Because user login with correct password.
            passwordEntity.setResetPasswordHash(null);
            passwordEntity.setWhenLastPasswordReset(null);
            resp.setUserStatus(userStatus.ret$Data());
            resp.setAuthExpires(passwordEntity.getPasswordExpiry());
            return resp;
        } else if (isResetPasswordMatch(passwordEntity, hash, now)) {
            // Password match with the reset password. Set it as a new password.
            final PasswordEntity newPasswordEntity = passwordSettingService.setPasswordForUser(now, userEntity, password,
                    userEntity.getObjectRef());
            if (!newPasswordEntity.getPasswordExpiry().isAfter(now)) {
                resp.setReturnCode(T9tException.PASSWORD_EXPIRED); // reset password is expired
            }
            updateUserStatusEntityForSuccessPasswordLogin(userStatus, now);
            resp.setUserStatus(userStatus.ret$Data());
            resp.setAuthExpires(newPasswordEntity.getPasswordExpiry());
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
        if (!userEntity.getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)) {
            // single tenant, defined by user
            final TenantEntity tenantEntity = em.find(TenantEntity.class, userEntity.getTenantId());
            if (tenantEntity == null) {
                LOGGER.error("User {} maps to non existent tenant of ref {}", userEntity.getUserId(), userEntity.getTenantId());
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
            if (!roleEntity.getTenantId().equals(T9tConstants.GLOBAL_TENANT_ID)) {
                // single tenant, defined by role
                final TenantEntity tenantEntity = em.find(TenantEntity.class, roleEntity.getTenantId());
                if (tenantEntity == null) {
                    LOGGER.error("User {} maps to role {} maps to non existent tenant of ref {}", userEntity.getUserId(), roleEntity.getRoleId(),
                            roleEntity.getTenantId());
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
        final TypedQuery<TwoTenantIds> query = em.createQuery(
                "SELECT NEW " + TwoTenantIds.class.getCanonicalName() + "(utr.tenantId, r.tenantId) FROM UserTenantRoleEntity utr, RoleEntity r "
                + "WHERE utr.userRef = :userRef " + "AND utr.roleRef = r.objectRef",
                TwoTenantIds.class);
        query.setParameter("userRef", userRef);

        // build the set of allowed tenants.
        // the set consists of all tenants if at least one assignment is the global
        // tenant for both references
        final List<TwoTenantIds> results = query.getResultList();

        LOGGER.debug("Found {} user / tenant / role assignments", results.size());

        final Set<String> allValidTenantIds = new HashSet<>(results.size());
        for (TwoTenantIds r : results) {
            LOGGER.trace("Found tenant pair {}", r);

            if (r.isDoubleGlobal()) {
                LOGGER.debug("Access to all tenants allowed for userId {} due to unrestricted assignment to global role", userEntity.getUserId());
                return listOfAllTenants(em);
            }

            allValidTenantIds.add(r.effectiveTenantId());
        }

        if (LOGGER.isDebugEnabled()) {
            final String tenantList = String.join(", ", allValidTenantIds.stream().map(i -> i.toString()).collect(Collectors.toList()));
            LOGGER.debug("UserId {} has selective access to tenants {} due to specific role assignments", userEntity.getUserId(), tenantList);
        }

        if (allValidTenantIds.isEmpty()) {
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
        query2.setParameter("tenants", allValidTenantIds);
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
    public Map<String, Object> getTenantZ(final String tenantId) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TenantEntity tenantEntity = em.find(TenantEntity.class, tenantId);
        return tenantEntity == null ? null : tenantEntity.getZ();
    }

    @Override
    public String assignNewPasswordIfEmailMatches(final RequestContext ctx, final String userId, final String emailAddress) {
        final UserEntity userEntity = getUserByUserIdIgnoringTenant(userId, false);
        if (userEntity == null) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // user does not exist
        }

        // validate that the user is allowed for internal auth
        if (Boolean.TRUE.equals(userEntity.getOnlyExternalAuth())) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // user is allowed to log in via SSO only
        }
        // validate that the user is active and that the email address matches
        if (!userEntity.getIsActive() || !emailAddress.equalsIgnoreCase(userEntity.getEmailAddress())) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // wrong email address
        }

        final PasswordEntity passwordEntity = getCurrentPasswordEntity(userEntity.getObjectRef());
        if (passwordEntity != null && passwordEntity.getWhenLastPasswordReset() != null
            && passwordEntity.getWhenLastPasswordReset().plus(T9tConstants.RESET_PASSWORD_REQUEST_LIMIT, ChronoUnit.MINUTES).isAfter(ctx.executionStart)) {
            throw new T9tException(T9tException.NOT_AUTHENTICATED); // reset password request is throttled
        }
        // checks OK, proceed
        // The request creates an initial random password for the specified user, or
        // creates a new password for that user.
        final String newPassword = PasswordUtil.generateRandomPassword(T9tConstants.DEFAULT_RANDOM_PASS_LENGTH);

        if (passwordEntity != null) {
            passwordEntity.setResetPasswordHash(PasswordUtil.createPasswordHash(userEntity.getUserId(), newPassword));
            passwordEntity.setWhenLastPasswordReset(ctx.executionStart);
        } else {
            passwordSettingService.setPasswordForUser(ctx, userEntity, newPassword);
        }
        return newPassword;
    }

    private PasswordEntity getCurrentPasswordEntity(final Long userRef) {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final TypedQuery<PasswordEntity> query = em.createQuery(GET_CURRENT_PASSWORD, PasswordEntity.class);
        query.setParameter("userRef", userRef);
        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    private boolean isResetPasswordMatch(final PasswordEntity passwordEntity, final ByteArray passwordHash, final Instant now) {
        return passwordEntity.getResetPasswordHash() != null && passwordEntity.getResetPasswordHash().equals(passwordHash)
            && passwordEntity.getWhenLastPasswordReset() != null
            && passwordEntity.getWhenLastPasswordReset().plus(T9tConstants.RESET_PASSWORD_VALIDITY, ChronoUnit.HOURS).isAfter(now);
    }

    protected void updateUserStatusEntityForSuccessPasswordLogin(final UserStatusEntity userStatus, final Instant now) {
        userStatus.setNumberOfIncorrectAttempts(0); // reset attempt counter
        userStatus.setPrevLogin(userStatus.getLastLogin());
        userStatus.setPrevLoginByPassword(userStatus.getLastLoginByPassword());
        userStatus.setLastLogin(now);
        userStatus.setLastLoginByPassword(now);
    }

    @Override
    public void deletePasswordBlacklist() {
        final EntityManager em = jpaContextProvider.get().getEntityManager();
        final Query query = em.createQuery("DELETE FROM " + PasswordBlacklistEntity.class.getSimpleName());
        final int count = query.executeUpdate();
        LOGGER.debug("Deletion of passwordBlacklist is done: {} entries were deleted!", count);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<UserData> getUsersWithPermission(final JwtInfo jwtInfo, final PermissionType permissionType, final String resourceId,
            final OperationTypes operationTypes) {

        final String permissionId = permissionType.getToken() + "." + resourceId;

        final String queryPermission = "SELECT per.role_ref FROM p42_cfg_role_to_permissions per "
                + " WHERE per.tenant_id IN :tenants AND :permissionId LIKE per.permission_id || '%' AND (per.permission_set &  :bitmap) = :bitmap";

        final String queryUser = "SELECT u FROM UserEntity u, UserTenantRoleEntity utr, RoleEntity r "
                + " WHERE u.objectRef = utr.userRef AND utr.roleRef =  r.objectRef AND r.objectRef IN :roleRefs"
                + " AND u.tenantId IN :tenants AND u.isActive = :isActive AND utr.tenantId IN :tenants"
                + " AND r.tenantId IN :tenants AND r.isActive = :isActive";

        try {
            final EntityManager em = jpaContextProvider.get().getEntityManager();
            final List<String> tenants;

            if (T9tConstants.GLOBAL_TENANT_ID.equals(jwtInfo.getTenantId())) {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_ID);
            } else {
                tenants = ImmutableList.of(T9tConstants.GLOBAL_TENANT_ID, jwtInfo.getTenantId());
            }

            final Query permQuery = em.createNativeQuery(queryPermission, Long.class).setParameter("tenants", tenants)
                    .setParameter("permissionId", permissionId).setParameter("bitmap", operationTypes.getBitmap());

            final List<Long> roleRefs = permQuery.getResultList();

            final List<UserEntity> users = em.createQuery(queryUser, UserEntity.class).setParameter("tenants", tenants).setParameter("isActive", Boolean.TRUE)
                    .setParameter("roleRefs", roleRefs).getResultList();

            return userEntity2UserDataMapper.mapToUserData(users);

        } catch (final Exception e) {
            LOGGER.error("JPA exception {} while reading users for tenantId {}, for permissionId {}, operationTypes {}: {}", e.getClass().getSimpleName(),
                    jwtInfo.getTenantId(), permissionId, operationTypes, e.getMessage());
            return EMPTY_USER_LIST;
        }
    }
}
