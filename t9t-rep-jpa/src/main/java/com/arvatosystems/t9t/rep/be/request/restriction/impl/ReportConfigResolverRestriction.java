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
package com.arvatosystems.t9t.rep.be.request.restriction.impl;

import java.util.ArrayList;
import java.util.List;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.rep.ReportConfigRef;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportConfigResolverRestriction;
import com.arvatosystems.t9t.rep.jpa.entities.ReportConfigEntity;
import com.arvatosystems.t9t.rep.jpa.persistence.IReportConfigEntityResolver;
import com.arvatosystems.t9t.server.services.IAuthorize;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;


/**
 * Implementation of {@linkplain IReportConfigResolverRestriction}
 * @author RREN001
 */
@Singleton
public class ReportConfigResolverRestriction implements IReportConfigResolverRestriction {
    protected final IReportConfigEntityResolver resolver = Jdp.getRequired(IReportConfigEntityResolver.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
//    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    /**
     * method check if the user has permission to the reportConfig by it's reportConfig's reference
     * @param ref : the reportConfig ref (in here we are using reportConfigRef.getObjectRef())
     * @return the same (reportConfig) ref that's passed as an input
     * @throws T9tException.RESTRICTED_ACCESS to specify that the user is trying to access restricted report
     */
    @Override
    public Long apply(Long ref) {
        ReportConfigEntity reportConfigEntity = resolver.findActive(ref, true);
        checkUserAccessOk(reportConfigEntity.getReportConfigId());
        return ref;
    }

    //We are having objectRef, thus restriction will be handled by apply(Long ref)
    @Override
    public ReportConfigRef apply(ReportConfigRef ref) {
        throw new UnsupportedOperationException();
    }

    /**
     * method check if the user has permission to the reportConfig by it's reportConfig entity
     * @param ref : the entity of reportConfig
     * @return the same (reportConfig) entity that's passed as an input
     * @throws T9tException.RESTRICTED_ACCESS to specify that the user is trying to access restricted report
     */
    @Override
    public ReportConfigEntity apply(ReportConfigEntity entity) {
        checkUserAccessOk(entity.getReportConfigId());
        return entity;
    }

    private void checkUserAccessOk(String id) {
        RequestContext ctx = Jdp.getRequired(RequestContext.class);
        Permissionset perms = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.REPORTING, id);
        if (!perms.contains(OperationType.EXECUTE)) {
            throw new T9tException(T9tException.RESTRICTED_ACCESS, id);
        }
    }

    /**
     * resolve all of the permissionId that's allowed for the user, using {@link QueryPermissionsRequest}
     */
    @Override
    public List<String> getPermissionIdList(RequestContext ctx) {
        final List<PermissionEntry> permissions = authorizer.getAllPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.REPORTING);
        final List<String> reportIdPermissions = new ArrayList<>(permissions.size());
        for (PermissionEntry perm: permissions) {
            if (perm.getResourceId().startsWith("R.") && perm.getPermissions().contains(OperationType.EXECUTE))
                reportIdPermissions.add(perm.getResourceId().substring(2));
        }
        if (reportIdPermissions.contains(""))
            return ImmutableList.<String>of(); // no restriction
        return reportIdPermissions;
    }
}
