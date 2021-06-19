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
package com.arvatosystems.t9t.io.jpa.request;

import java.io.File;
import java.util.HashSet;
import java.util.List;

import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.PurgeOutdatedSinksAndFilesRequest;

import de.jpaw.dp.Jdp;

public class PurgeOutdatedSinksAndFilesRequestHandler extends AbstractRequestHandler<PurgeOutdatedSinksAndFilesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurgeOutdatedSinksAndFilesRequestHandler.class);
    private static final long MILLIS_PER_DAY = 86_400_000L;

    private final IDataSinkEntityResolver dataSinkResolver = Jdp.getRequired(IDataSinkEntityResolver.class);
    private final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    private final String SELECT_DATA_SINKS_SQL = "SELECT ds FROM " + dataSinkResolver.getBaseJpaEntityClass().getSimpleName()
        + " ds WHERE ds.tenantRef IN :tenantRefs AND (ds.retentionPeriodFiles IS NOT NULL OR ds.retentionPeriodSinks IS NOT NULL)";

    @Override
    public ServiceResponse execute(RequestContext ctx, PurgeOutdatedSinksAndFilesRequest request) throws Exception {
        final List<DataSinkEntity> dataSinks;
        if (request.getOnlyDataSinkId() != null) {
            // retrieve the specific data sink
            dataSinks = dataSinkResolver.findByDataSinkIdWithDefault(false, request.getOnlyDataSinkId());
            if (dataSinks.isEmpty()) {
                throw new T9tException(T9tIOException.RECORD_DOES_NOT_EXIST, request.getOnlyDataSinkId());
            }
        } else {
            // loop all data sinks which are relevant for deletion
            final TypedQuery<DataSinkEntity> q = dataSinkResolver.getEntityManager().createQuery(SELECT_DATA_SINKS_SQL, DataSinkEntity.class);
            HashSet<Long> tenantRefs = new HashSet<>();
            tenantRefs.add(T9tConstants.GLOBAL_TENANT_REF42);
            tenantRefs.add(dataSinkResolver.getSharedTenantRef());
            q.setParameter("tenantRefs", tenantRefs);
            dataSinks = q.getResultList();
            if (dataSinks.isEmpty()) {
                LOGGER.warn("No data sinks relevant for purging");
            }
        }
        for (DataSinkEntity ds: dataSinks) {
            purgeFiles(ctx, ds);
            purgeSinkEntries(ctx, ds);
        }

        return ok();
    }

    private final String SELECT_SINKS_SQL = "SELECT s.fileOrQueueName FROM " + sinkResolver.getBaseJpaEntityClass().getSimpleName()
        + " s WHERE s.tenantRef = :tenantRef AND s.cTimestamp < :cutoff AND s.dataSinkRef = :dataSinkRef";

    /** Method to delete all files which are referenced by sink entries older than x days. */
    private void purgeFiles(RequestContext ctx, DataSinkEntity dataSink) {
        if (dataSink.getRetentionPeriodFiles() == null) {
            // nothing to do
            return;
        }
        final TypedQuery<String> q = sinkResolver.getEntityManager().createQuery(SELECT_SINKS_SQL, String.class);
        q.setParameter("tenantRef", sinkResolver.getSharedTenantRef());
        q.setParameter("dataSinkRef", dataSink.getObjectRef());
        q.setParameter("cutoff", ctx.executionStart.minus(MILLIS_PER_DAY * (long)dataSink.getRetentionPeriodFiles()));
        final List<String> filesToDelete = q.getResultList();
        LOGGER.info("Data sink {}: Attempting to delete {} files older than {} days", dataSink.getDataSinkId(), filesToDelete.size(), dataSink.getRetentionPeriodFiles());
        for (String filename: filesToDelete) {
            (new File(fileUtil.getAbsolutePathForTenant(ctx.tenantId, filename))).delete();  // attempt to delete the file, ignore the result
        }
    }

    private final String DELETE_SINKS_SQL = "DELETE FROM " + sinkResolver.getBaseJpaEntityClass().getSimpleName()
        + " s WHERE s.tenantRef = :tenantRef AND s.cTimestamp < :cutoff AND s.dataSinkRef = :dataSinkRef";

    /** Method to delete all sink entries older than x days. */
    private void purgeSinkEntries(RequestContext ctx, DataSinkEntity dataSink) {
        if (dataSink.getRetentionPeriodSinks() == null) {
            // nothing to do
            return;
        }
        final TypedQuery<String> q = sinkResolver.getEntityManager().createQuery(DELETE_SINKS_SQL, String.class);
        q.setParameter("tenantRef", sinkResolver.getSharedTenantRef());
        q.setParameter("dataSinkRef", dataSink.getObjectRef());
        q.setParameter("cutoff", ctx.executionStart.minus(MILLIS_PER_DAY * (long)dataSink.getRetentionPeriodSinks()));
        final int recordsDeleted = q.executeUpdate();
        LOGGER.info("Data sink {}: Deleted {} sink entries older than {} days", dataSink.getDataSinkId(), recordsDeleted, dataSink.getRetentionPeriodSinks());
    }
}
