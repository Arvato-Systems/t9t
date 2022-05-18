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
package com.arvatosystems.t9t.bucket.jpa.request;

import java.util.List;

import jakarta.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity;
import com.arvatosystems.t9t.bucket.request.AbstractBucketExportRequest;
import com.arvatosystems.t9t.bucket.request.DeleteBucketRequest;
import com.arvatosystems.t9t.bucket.request.SwitchCurrentBucketNoRequest;

import de.jpaw.util.ApplicationException;

public abstract class AbstractBucketExportRequestHandler<T extends AbstractBucketExportRequest> extends AbstractBucketBaseExportRequestHandler<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractBucketExportRequestHandler.class);

    protected abstract void exportChunk(IOutputSession os, List<Long> refs, T request, String qualifier, int bucketNoToSelect);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final T rp) throws Exception {
        final String qualifier  = rp.getBucketId()   == null ? rp.ret$MetaData().getProperties().get("bucketId")   : rp.getBucketId();
        final String dataSinkId = rp.getDataSinkId() == null ? rp.ret$MetaData().getProperties().get("dataSinkId") : rp.getDataSinkId();
        if (qualifier == null || dataSinkId == null)
            throw new T9tException(T9tException.REQUEST_PARAMETER_BAD_INHERITANCE, "missing qualifier or dataSinkId on definition of " + rp.ret$PQON());

        final boolean switchBucket = !Boolean.FALSE.equals(rp.getSwitchBucket());  // switch unless explicitly told not to do so
        final boolean deleteBucket = !Boolean.FALSE.equals(rp.getDeleteBeforeSwitch());  // delete target bucket unless explicitly told not to do so
        final BucketCounterEntity counterEntity = counterResolver.findByQualifier(false, qualifier);
        final int oldBucketNo = counterEntity.getCurrentVal();
        int newBucketNo = oldBucketNo + 1;  // new number - but only valid if switching
        if (newBucketNo >= counterEntity.getMaxVal())
            newBucketNo = 0;  // restart
        final int bucketNoToSelect = rp.getBucketNo() != null ? rp.getBucketNo()
          : (switchBucket ? oldBucketNo : (oldBucketNo > 0 ? oldBucketNo - 1 : counterEntity.getMaxVal() - 1));

        // first, delete the target bucket, unless disabled
        if (deleteBucket && switchBucket) {
            ctx.statusText = "Deleting new bucket " + qualifier + ":" + newBucketNo;
            final DeleteBucketRequest dbrq = new DeleteBucketRequest();
            dbrq.setQualifier(qualifier);
            dbrq.setBucketNo(newBucketNo);
            final ServiceResponse deleteResp = autoExecutor.execute(ctx, dbrq);
            if (!ApplicationException.isOk(deleteResp.getReturnCode()))
                return deleteResp;
        }

        // next, switch the bucket
        if (switchBucket) {
            ctx.statusText = "Switching to new bucket " + qualifier + ":" + newBucketNo;
            final SwitchCurrentBucketNoRequest srq = new SwitchCurrentBucketNoRequest();
            srq.setQualifier(qualifier);
            srq.setDeleteBeforeSwitch(switchBucket);
            final ServiceResponse switchResp = autoExecutor.execute(ctx, srq);
            if (!ApplicationException.isOk(switchResp.getReturnCode()))
                return switchResp;

            // in case of switch, wait to flush the async writer queue
            ctx.statusText = "Waiting to allow the bucket writer to switch " + qualifier;
            Thread.sleep(2000L);
        }

        ctx.statusText = "Selecting bucket refs for bucket " + qualifier + ":" + bucketNoToSelect;
        final List<Long> refsToExport = getRefs(qualifier, bucketNoToSelect);
        final EntityManager em = entryResolver.getEntityManager();
        em.clear();

        try (IOutputSession os = splittingOutputSessionprovider.get(rp.getMaxRecordsPerFile())) {
            final OutputSessionParameters osp = new OutputSessionParameters();
            osp.setDataSinkId(dataSinkId);
            os.open(osp);

            Integer chunkSize = os.getChunkSize();
            if (chunkSize == null || chunkSize < 1 || chunkSize > MAX_CHUNK_SIZE)
                chunkSize = MAX_CHUNK_SIZE;
            final int chunks = (refsToExport.size() + (chunkSize - 1)) / chunkSize;

            for (int chunk = 0; chunk < chunks; ++chunk) {
                ctx.statusText = "Exporting chunk " + (chunk + 1) + " of " + chunks + " for bucket " + qualifier;
                final int endIndex = chunk < chunks - 1 ? (chunk + 1) * chunkSize : refsToExport.size();
                final List<Long> subListToProcess = refsToExport.subList(chunk * chunkSize, endIndex);  // a list of min 1 and max CHUNK_SIZE entries
                LOGGER.debug("Processing chunk of {} entries", subListToProcess.size());

                exportChunk(os, subListToProcess, rp, qualifier, bucketNoToSelect);
                em.flush();
                em.clear();
            }
        } catch (final Exception e) {
            LOGGER.error("Problem during export: ", e);
            throw e;
        }
        return ok();
    }
}
