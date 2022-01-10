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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess;
import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.server.services.IAuthorize;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Central location to compute if a user may do a certain operation or not.
 * For this, in the general case the database is checked, all entries in the database are added.
 * Information in the JWT is combined with the database entries.
 * In some cases, for BACKEND permissions, it is not required to query the DB, because all information is contained in the JWT itself.
 * The following properties hold:
 * - No permission returned will contain a bit which is not set in the JWT's permissionMax.
 * - If an operation is allowed, then the minimum permissions will be at least as defined in the JWT's permissionMin.
 * - The JWT field resource, if not empty, contains a list of permissions, separated by comma (','). If the list ends with a comma,
 *    the list is understood as addition to database entries.
 *   a) If resourceIsWildcard is TRUE, then a match of any components will grant permissionMin to the caller.
 *   b) If resourceIsWildcard is not TRUE, then a match of no component will mean access is denied.
 */

@Singleton
public class Authorization implements IAuthorize {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authorization.class);
    private static final List<PermissionEntry> EMPTY_PERMISSION_LIST = ImmutableList.of();
    private static final Cache<Long, List<PermissionEntry>> PERMISSION_CACHE = CacheBuilder.newBuilder().maximumSize(1000L)
            .expireAfterWrite(5L, TimeUnit.MINUTES).build();
    private final IAuthPersistenceAccess authPersistenceAccess = Jdp.getRequired(IAuthPersistenceAccess.class);

    public Authorization() { // make the sets immutable
        NO_PERMISSIONS.freeze();
        ALL_PERMISSIONS.freeze();
        EXEC_PERMISSION.freeze();
    }

    @Override
    public List<PermissionEntry> getAllPermissions(final JwtInfo jwtInfo, final PermissionType permissionType) {
        LOGGER.debug("Full permission list requested for user {}, tenant {}, type {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType);

        Permissionset perms = jwtInfo.getPermissionsMin() == null ? NO_PERMISSIONS : jwtInfo.getPermissionsMin();
        if (jwtInfo.getResource() == null || jwtInfo.getResource().isEmpty()) {
            if (Boolean.TRUE.equals(jwtInfo.getResourceIsWildcard())) {
                LOGGER.debug("Resource null and isWildcard set, means ADMIN for user {}, tenant {}", jwtInfo.getUserId(), jwtInfo.getTenantId());
                return Collections.singletonList(new PermissionEntry(permissionType.getToken() + ".", perms));
            }
            LOGGER.debug("Resource null and isWildcard not set, means QUERY DB for user {}, tenant {}", jwtInfo.getUserId(), jwtInfo.getTenantId());
            // all from DB, which relate to the specified permissionType
            return filterPermissionsBasedOnType(getAllPermissionsSub(jwtInfo), permissionType);
        }
        if (Boolean.TRUE.equals(jwtInfo.getResourceIsWildcard())) {
            LOGGER.debug("Resource not null ({}) and isWildcard set, means use JWT settings for user {}, tenant {}",
                    jwtInfo.getResource(), jwtInfo.getUserId(), jwtInfo.getTenantId());
            // create a list of resources from the JWT, which are relevant
            String[] jwtParts = jwtInfo.getResource().split(",", -1);
            // an empty part means the standard permissions are in
            List<PermissionEntry> part1 = new ArrayList<PermissionEntry>(jwtParts.length);

            for (String jwtPart : jwtParts) {
                if (jwtPart.isEmpty()) {
                    final List<PermissionEntry> permissions = filterPermissionsBasedOnType(getAllPermissionsSub(jwtInfo), permissionType);
                    for (PermissionEntry p : permissions) {
                        part1.add(p.ret$MutableClone(true, true));
                    }
                    break;
                }
            }

            // an empty part means the standard permissions are in
            for (final String p : jwtParts) {
                if (p.length() > 0 && p.startsWith(permissionType.getToken())) {
                    final Permissionset permissionset = new Permissionset(perms.getBitmap());
                    final PermissionEntry permissionEntry = new PermissionEntry(p, permissionset);
                    part1.add(permissionEntry);
                }
            }
            return sortedAndMerged(part1, jwtInfo);
        }
        LOGGER.debug("Resource not null ({}) and isWildcard not set, means use DB for user {}, tenant {} (*: experimental)",
                jwtInfo.getResource(), jwtInfo.getUserId(), jwtInfo.getTenantId());
        // (*): TODO: filter / intersect with Jwt data must be verified, not fully implemented / tested, marked as experimental feature
        return filterPermissionsBasedOnType(getAllPermissionsSub(jwtInfo), permissionType);
    }

    private List<PermissionEntry> filterPermissionsBasedOnType(List<PermissionEntry> permissions, PermissionType permissionType) {
        final List<PermissionEntry> filteredPermissions = new ArrayList<>(permissions.size());
        for (PermissionEntry permissionEntry : permissions) {
            if (permissionEntry.getResourceId().startsWith(permissionType.getToken())) {
                filteredPermissions.add(permissionEntry);
            }
        }
        return filteredPermissions;
    }

    @Override
    public Permissionset getPermissions(final JwtInfo jwtInfo, final PermissionType permissionType, final String resource1) {
        LOGGER.trace("Permission requested for user {}, tenant {}, type {}, resource {}", jwtInfo.getUserId(), jwtInfo.getTenantId(), permissionType,
                resource1);

        final String resource;
        if (permissionType == PermissionType.BACKEND) {
            resource = MessagingUtil.toPerm(resource1); // token + possible appendix of request
        } else {
            resource = permissionType.getToken() + "." + resource1;
        }

        // possible cases:
        // resourceIsWildcard    resourceId
        //        true              null              general supervisor: all permissions on everything
        //        false             null              use permissions as in DB via user -> role -> permissions
        //        true              non-null          use permissions as defined in JWT (may be comma separated, if an empty section exists it adds to the DB)
        //        false             non-null          use permissions as intersection of what has been defined in JWT and in DB (used to define API-KEYs for
        //                                            subsets of permissions)
        boolean useDb = jwtInfo.getResource() == null || jwtInfo.getResource().isEmpty();
        if (useDb && Boolean.TRUE.equals(jwtInfo.getResourceIsWildcard()))
            return jwtInfo.getPermissionsMin() == null ? NO_PERMISSIONS : jwtInfo.getPermissionsMin();

        if (!useDb) {
            // some contents in resource. If we conclude that useDb is set within this block, then authorization falls back to the DB process
            if (Boolean.TRUE.equals(jwtInfo.getResourceIsWildcard())) {
                // positive checks. See if we find permissions within the JWT
                if (jwtInfo.getResource().indexOf(',') < 0) {
                    // simple case, single field, no split required. This single fields decides about yes or no
                    if (resource.startsWith(jwtInfo.getResource()))
                        return jwtInfo.getPermissionsMin() == null ? EXEC_PERMISSION : jwtInfo.getPermissionsMin();
                } else {
                    // iterate list of patterns. An empty entry adds the DB as possible resource
                    final String[] patterns = jwtInfo.getResource().split(",", -1);
                    for (String p : patterns) {
                        if (p.length() == 0)
                            useDb = true;
                        else if (resource.startsWith(p))
                            return jwtInfo.getPermissionsMin() == null ? EXEC_PERMISSION : jwtInfo.getPermissionsMin();
                    }
                }
            } else {
                // AND condition of JWT resources with DB setup. The JWT limits what has been configured in the DB (API-Key for subset of functionality)
                if (jwtInfo.getResource().indexOf(',') < 0) {
                    // simple case, single field, no split required
                    if (!resource.startsWith(jwtInfo.getResource()))
                        return NO_PERMISSIONS;
                } else {
                    final String[] patterns = jwtInfo.getResource().split(",", -1);
                    for (String p : patterns) {
                        if (p.length() > 0) {
                            if (resource.startsWith(p))
                                useDb = true;
                        }
                    }
                }
            }
            if (!useDb)
                return NO_PERMISSIONS;
        }

        // no restriction rule in JWT. Also no grant. Must check permissions assignment from cache or DB
        // after the call, the permissions contain everything which is not conflicting with the JWT
        List<PermissionEntry> permissions = getAllPermissionsSub(jwtInfo);
        // now find the longest substring of resource in the list
        // TODO: tune via binary search, as the list is sorted!
        for (PermissionEntry p : permissions) {
            if (resource.startsWith(p.getResourceId())) {
                if (jwtInfo.getPermissionsMin() == null) {
                    return p.getPermissions();
                }
                final Permissionset pp = new Permissionset(p.getPermissions().getBitmap());
                pp.unifyWith(jwtInfo.getPermissionsMin());
                return pp;
            }
        }

        return NO_PERMISSIONS;

    }

    protected List<PermissionEntry> getAllPermissionsSub(final JwtInfo jwtInfo) {

        List<PermissionEntry> entries = null;
        if (jwtInfo.getSessionRef() == null) {
            // no session defined - get from DB
            entries = getAllFilteredPermissions(jwtInfo);
        } else {
            try {
                entries = PERMISSION_CACHE.get(jwtInfo.getSessionRef(), () -> getAllFilteredPermissions(jwtInfo));
            } catch (ExecutionException e) {
                LOGGER.error("Error while filtering permissions: {}", e.getMessage());
            }
        }
        return entries;
    }

    protected List<PermissionEntry> getAllFilteredPermissions(final JwtInfo jwtInfo) {
        final List<PermissionEntry> rawData = authPersistenceAccess.getAllDBPermissions(jwtInfo);
        // filter off any restricted entries
        final List<PermissionEntry> filteredData;
        if (Boolean.TRUE.equals(jwtInfo.getResourceIsWildcard()) || (jwtInfo.getResource() == null || jwtInfo.getResource().isEmpty())) {
            filteredData = rawData;
        } else {
            // use resources if they are not a backend resource, or they start with the restriction
            filteredData = rawData.stream()
                    .filter(i -> !i.getResourceId().startsWith(PermissionType.BACKEND.getToken()) || i.getResourceId().startsWith(jwtInfo.getResource()))
                    .collect(Collectors.toList());
        }

        return sortedAndMerged(filteredData, jwtInfo);
    }

    protected List<PermissionEntry> sortedAndMerged(final List<PermissionEntry> filteredData, final JwtInfo jwtInfo) {
        if (filteredData.isEmpty())
            return EMPTY_PERMISSION_LIST;        // immutable list!

        // at least one entry => postprocess (apply min/max from Jwt)
        final Permissionset minP = jwtInfo.getPermissionsMin() == null ? NO_PERMISSIONS : jwtInfo.getPermissionsMin();
        final Permissionset maxP = jwtInfo.getPermissionsMax() == null ? ALL_PERMISSIONS : jwtInfo.getPermissionsMax();

        if (filteredData.size() == 1) {
            // single entry - no sorting / merging required
            PermissionEntry pe = filteredData.get(0);
            Permissionset p = pe.getPermissions();
            p.unifyWith(minP);
            p.intersectWith(maxP);
            pe.setPermissions(p);
            pe.freeze();
            return ImmutableList.of(pe);
        }
        // sort the entries by resourceId in ascending order
        // before, copy the list to ensure the list is modifyable
        final List<PermissionEntry> mutableList = new ArrayList<PermissionEntry>(filteredData);
        Collections.sort(mutableList, ((a, b) -> a.getResourceId().compareTo(b.getResourceId())));

        // postprocess: apply permissions to all included subsets
        int n = mutableList.size();
        int i = 0;
        while (i < n) {
            i = mergeDownPermissions(mutableList, i, n, minP, maxP);
        }

        return Collections.unmodifiableList(mutableList);
    }

    /** Merge down means that if we have permissions ABC for resource q and only a subset for resource q.z, then unify them.
     * The returned index is strictly greater than the input parameter i, and at most n (which means we are done with merging).
     * The entry at the returned index is not a subresource of the entry at the input index.
     * The list of permissions is mutable and its entries will be modified. The list structure (ordering) stays as it is.
     * Entries which are processed will be frozen.
     *
     * Every call will process at least one entry (and freeze it).
    */
    protected int mergeDownPermissions(final List<PermissionEntry> permissions, final int i, final int n,
            final Permissionset minPermissions, final Permissionset maxPermissions) {
        PermissionEntry permissionEntry = permissions.get(i);

        // process current entry: apply min / max
        Permissionset modset = permissionEntry.getPermissions();
        modset.unifyWith(minPermissions);
        modset.intersectWith(maxPermissions);
        permissionEntry.freeze();

        // modset is the new min
        int j = i + 1;
        while (j < n) {
            PermissionEntry pe2 = permissions.get(j);
            if (pe2.getResourceId().startsWith(permissionEntry.getResourceId())) {
                // cascade down. the recursion only alters the minpermissions - we skip entries at this level which have been processed in a subrouting
                // therefore this process is linear time in number of list entries
                j = mergeDownPermissions(permissions, j, n, modset, maxPermissions);
            } else {
                return j;  // no merging required => return to caller with next entry
            }
        }
        return j;
    }
}
