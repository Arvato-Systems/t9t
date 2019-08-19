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
package com.arvatosystems.t9t.auth.be.impl

import com.arvatosystems.t9t.auth.services.IAuthPersistenceAccess
import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.auth.PermissionEntry
import com.arvatosystems.t9t.base.auth.PermissionType
import com.arvatosystems.t9t.server.services.IAuthorize
import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.collect.ImmutableList
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import java.util.ArrayList
import java.util.Collections
import java.util.List
import java.util.concurrent.TimeUnit

/** Central location to compute if a user may do a certain operation or not.
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
@AddLogger
class Authorization implements IAuthorize {
    private static final List<PermissionEntry> EMPTY_PERMISSION_LIST = ImmutableList.of();
    private static final Cache<Long, List<PermissionEntry>> permissionCache = CacheBuilder.newBuilder.maximumSize(1000L).expireAfterWrite(5L, TimeUnit.MINUTES).build

    @Inject IAuthPersistenceAccess authPersistenceAccess

    public new() {
        NO_PERMISSIONS.freeze   // make the set immutable
        ALL_PERMISSIONS.freeze  // make the set immutable
        EXEC_PERMISSION.freeze  // make the set immutable
    }

    override getPermissions(JwtInfo jwtInfo, PermissionType permissionType, String resource1) {
        LOGGER.debug("Permission requested for user {}, tenant {}, type {}, resource {}", jwtInfo.userId, jwtInfo.tenantId, permissionType, resource1);

        val resource =
            if (permissionType == PermissionType.BACKEND) {
                MessagingUtil.toPerm(resource1)  // token + possible appendix of request
            } else {
                permissionType.token + "." + resource1
            }

        // possible cases:
        // resourceIsWildcard    resourceId
        //        true              null              general supervisor: all permissions on everything
        //        false             null              use permissions as in DB via user -> role -> permissions
        //        true              non-null          use permissions as defined in JWT (may be comma separated, if an empty section exists it adds to the DB)
        //        false             non-null          use permissions as intersection of what has been defined in JWT and in DB (used to define API-KEYs for subsets of permissions)
        var boolean useDb = jwtInfo.resource.nullOrEmpty
        if (useDb && Boolean.TRUE == jwtInfo.resourceIsWildcard)
            return jwtInfo.permissionsMin ?: NO_PERMISSIONS

        if (!useDb) {
            // some contents in resource. If we conclude that useDb is set within this block, then authorization falls back to the DB process
            if (Boolean.TRUE == jwtInfo.resourceIsWildcard) {
                // positive checks. See if we find permissions within the JWT
                if (jwtInfo.resource.indexOf(',') < 0) {
                    // simple case, single field, no split required. This single fields decides about yes or no
                    if (resource.startsWith(jwtInfo.resource))
                        return jwtInfo.permissionsMin ?: EXEC_PERMISSION
                } else {
                    // iterate list of patterns. An empty entry adds the DB as possible resource
                    val patterns = jwtInfo.resource.split(",", -1)
                    for (p : patterns) {
                        if (p.length == 0)
                            useDb = true
                        else if (resource.startsWith(p))
                            return jwtInfo.permissionsMin ?: EXEC_PERMISSION
                    }
                }
            } else {
                // AND condition of JWT resources with DB setup. The JWT limits what has been configured in the DB (API-Key for subset of functionality)
                if (jwtInfo.resource.indexOf(',') < 0) {
                    // simple case, single field, no split required
                    if (!resource.startsWith(jwtInfo.resource))
                        return NO_PERMISSIONS
                } else {
                    val patterns = jwtInfo.resource.split(",", -1)
                    for (p : patterns) {
                        if (p.length > 0) {
                            if (resource.startsWith(p))
                                useDb = true
                        }
                    }
                }
            }
            if (!useDb)
                return NO_PERMISSIONS
        }

        // no restriction rule in JWT. Also no grant. Must check permissions assignment from cache or DB
        // after the call, the permissions contain everything which is not conflicting with the JWT
        val permissions = getAllPermissionsSub(jwtInfo)

        // now find the longest substring of resource in the list
        // TODO: tune via binary search, as the list is sorted!
        for (p : permissions) {
            if (resource.startsWith(p.resourceId)) {
                if (jwtInfo.permissionsMin === null) {
                    return p.permissions
                } else {
                    val pp = new Permissionset(p.permissions.bitmap)
                    pp.unifyWith(jwtInfo.permissionsMin)
                    return pp
                }
            }
        }
        return NO_PERMISSIONS
    }


    override getAllPermissions(JwtInfo jwtInfo, PermissionType permissionType) {
        LOGGER.debug("Full permission list requested for user {}, tenant {}, type {}", jwtInfo.userId, jwtInfo.tenantId, permissionType);

        val perms = jwtInfo.permissionsMin ?: NO_PERMISSIONS
        if (jwtInfo.resource.nullOrEmpty) {
            if (Boolean.TRUE == jwtInfo.resourceIsWildcard) {
                LOGGER.debug("Resource null and isWildcard set, means ADMIN for user {}, tenant {}", jwtInfo.userId, jwtInfo.tenantId);
                return #[
                    new PermissionEntry(permissionType.token + ".", perms)
                ]
            } else {
                LOGGER.debug("Resource null and isWildcard not set, means QUERY DB for user {}, tenant {}", jwtInfo.userId, jwtInfo.tenantId);
                // all from DB, which relate to the specified permissionType
                return getAllPermissionsSub(jwtInfo).filter[resourceId.startsWith(permissionType.token)].toList
            }
        } else {
            if (Boolean.TRUE == jwtInfo.resourceIsWildcard) {
                LOGGER.debug("Resource not null ({}) and isWildcard set, means use JWT settings for user {}, tenant {}", jwtInfo.resource, jwtInfo.userId, jwtInfo.tenantId);
                // create a list of resources from the JWT, which are relevant
                val jwtParts = jwtInfo.resource.split(',', -1)
                // an empty part means the standard permissions are in
                val part1 = if (jwtParts.contains(""))
                    getAllPermissionsSub(jwtInfo).filter[resourceId.startsWith(permissionType.token)].map[ret$MutableClone(true, true)].toList
                else
                    new ArrayList<PermissionEntry>(jwtParts.size)
                // add the parts from the JWT as mutable items
                for (p : jwtParts) {
                    if (p.length > 0 && p.startsWith(permissionType.token))
                        part1.add(new PermissionEntry(p, new Permissionset(perms.bitmap)))
                }
                // sort the list again and apply recursive permissions
                return sortedAndMerged(part1, jwtInfo)
            } else {
                LOGGER.debug("Resource not null ({}) and isWildcard not set, means use DB for user {}, tenant {} (*: experimental)", jwtInfo.resource, jwtInfo.userId, jwtInfo.tenantId);
                // (*): TODO: filter / intersect with Jwt data must be verified, not fully implemented / tested, marked as experimental feature
                return getAllPermissionsSub(jwtInfo).filter[resourceId.startsWith(permissionType.token)].toList
            }
        }
    }

    /** Merge down means that if we have permissions ABC for resource q and only a subset for resource q.z, then unify them.
     * The returned index is strictly greater than the input parameter i, and at most n (which means we are done with merging).
     * The entry at the returned index is not a subresource of the entry at the input index.
     * The list of permissions is mutable and its entries will be modified. The list structure (ordering) stays as it is.
     * Entries which are processed will be frozen.
     *
     * Every call will process at least one entry (and freeze it).
    */
    def protected int mergeDownPermissions(List<PermissionEntry> permissions, int i, int n, Permissionset minPermissions, Permissionset maxPermissions) {
        val permissionEntry = permissions.get(i);

        // process current entry: apply min / max
        val modset = permissionEntry.permissions
        modset.unifyWith(minPermissions)
        modset.intersectWith(maxPermissions)
        permissionEntry.freeze

        // modset is the new min
        var int j = i + 1
        while (j < n) {
            val pe2 = permissions.get(j)
            if (pe2.resourceId.startsWith(permissionEntry.resourceId)) {
                // cascade down. the recursion only alters the minpermissions - we skip entries at this level which have been processed in a subrouting
                // therefore this process is linear time in number of list entries
                j = permissions.mergeDownPermissions(j, n, modset, maxPermissions)
            } else {
                return j;  // no merging required => return to caller with next entry
            }
        }
        return j
    }

    def protected List<PermissionEntry> getAllFilteredPermissions(JwtInfo jwtInfo) {
        val rawData = authPersistenceAccess.getAllDBPermissions(jwtInfo)
        // filter off any restricted entries
        val filteredData = if (Boolean.TRUE == jwtInfo.resourceIsWildcard || jwtInfo.resource.isNullOrEmpty) {
            rawData
        } else {
            // use resources if they are not a backend resource, or they start with the restriction
            rawData.filter[!resourceId.startsWith(PermissionType.BACKEND.token) || resourceId.startsWith(jwtInfo.resource)].toList
        }

        return sortedAndMerged(filteredData, jwtInfo)
    }

    def protected sortedAndMerged(List<PermissionEntry> filteredData, JwtInfo jwtInfo) {
        if (filteredData.isEmpty)
            return EMPTY_PERMISSION_LIST        // immutable list!

        // at least one entry => postprocess (apply min/max from Jwt)
        val minP = jwtInfo.permissionsMin ?: NO_PERMISSIONS
        val maxP = jwtInfo.permissionsMax ?: ALL_PERMISSIONS

        if (filteredData.size == 1) {
            // single entry - no sorting / merging required
            val pe = filteredData.get(0)
            val p = pe.permissions
            p.unifyWith(minP)
            p.intersectWith(maxP)
            pe.permissions = p
            pe.freeze
            return ImmutableList.of(pe)
        }
        // sort the entries by resourceId in ascending order
        // before, copy the list to ensure the list is modifyable
        val mutableList = new ArrayList<PermissionEntry>(filteredData)
        Collections.sort(mutableList, [ a, b | a.resourceId.compareTo(b.resourceId) ])

        // postprocess: apply permissions to all included subsets
        val n = mutableList.length
        var int i = 0
        while (i < n) {
            i = mutableList.mergeDownPermissions(i, n, minP, maxP)
        }

        return Collections.unmodifiableList(mutableList)
    }

    def protected List<PermissionEntry> getAllPermissionsSub(JwtInfo jwtInfo) {

        if (jwtInfo.sessionRef === null) {
            // no session defined - get from DB
            return getAllFilteredPermissions(jwtInfo)
        } else {
            return permissionCache.get(jwtInfo.sessionRef) [ getAllFilteredPermissions(jwtInfo) ]
        }
    }
}
