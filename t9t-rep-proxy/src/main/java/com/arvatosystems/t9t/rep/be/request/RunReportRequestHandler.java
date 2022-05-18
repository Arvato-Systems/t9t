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
package com.arvatosystems.t9t.rep.be.request;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.SimpleCallOutExecutor;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.request.FileDownloadRequest;
import com.arvatosystems.t9t.io.request.FileDownloadResponse;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;
import com.arvatosystems.t9t.rep.request.RunReportRequest;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

public class RunReportRequestHandler extends AbstractRequestHandler<RunReportRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RunReportRequestHandler.class);

    private final IForeignRequest remoteCaller;
    private final IOutPersistenceAccess outPersistenceAccess = Jdp.getRequired(IOutPersistenceAccess.class);
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    /** Constructor to initialize the callout executor. */
    public RunReportRequestHandler() {
        final UplinkConfiguration reportServerConfig = ConfigProvider.getConfiguration().getUplinkConfiguration();
        remoteCaller = new SimpleCallOutExecutor(reportServerConfig.getUrl());
    }

    @Override
    public ServiceResponse execute(final RequestContext ctx, final RunReportRequest request) throws Exception {
        LOGGER.debug("Calling remote runReportRequest");
        final ServiceResponse remoteResponse = remoteCaller.execute(ctx, request);
        LOGGER.debug("Remote runReportRequest returned code {}, and type {}", remoteResponse.getReturnCode(), remoteResponse.getClass().getSimpleName());
        if (ApplicationException.isOk(remoteResponse.getReturnCode()) && remoteResponse instanceof SinkCreatedResponse) {
            // a report has been created. Now transfer it to this node
            final SinkCreatedResponse sinkResponse = (SinkCreatedResponse)remoteResponse;
            final Long sinkRef = sinkResponse.getSinkRef();
            // first, get the full SinkDTO record, to retrieve the path
            final SinkDTO sink = outPersistenceAccess.getSink(sinkRef);
            final boolean isFile = sink.getCommTargetChannelType() == CommunicationTargetChannelType.FILE;
            if (!isFile) {
                LOGGER.debug("Report output was written to channel {} - no transfer triggered", sink.getCommTargetChannelType());
            } else {
                final String absolutePath = fileUtil.getAbsolutePathForTenant(ctx.tenantId, sink.getFileOrQueueName());
                final File myFile = new File(absolutePath);
                final boolean fileAlreadyExists = myFile.exists();
                LOGGER.debug("File path is {} ({})", absolutePath, fileAlreadyExists ? "already exists on local FS" : "does not exist in local FS");
                if (!fileAlreadyExists) {
                    fileUtil.createFileLocation(absolutePath);
                    // transfer it
                    try (FileOutputStream os = new FileOutputStream(myFile)) {
                        final Long size = transferFile(ctx, os, sinkRef);
                        LOGGER.debug("Transferred report of length {} from remote to local FS", size);
                    } catch (final IOException ex) {
                        LOGGER.error(ex.getMessage() + ": " + absolutePath, ex);
                        throw new T9tException(T9tIOException.OUTPUT_FILE_OPEN_EXCEPTION, absolutePath);
                    }
                }
            }
        }

        return remoteResponse;
    }

    protected Long transferFile(final RequestContext ctx, final FileOutputStream os, final Long sinkRef) throws IOException {
        long offset = 0;
        // set given chunkSizeInBytes as limit or default 8 MB
        final int limit = 8 * 1024 * 1024;
        // is needed for the loop breakup
        boolean hasMore = true;

        while (hasMore) {
            final FileDownloadResponse fileDownloadResponse = execFileDownloadRequest(ctx, sinkRef, offset, limit);

            // Get the data
            final byte[] data = fileDownloadResponse.getData().getBytes();
            hasMore = fileDownloadResponse.getHasMore();
            LOGGER.debug("Download: Chunk/content-length:{}. Has-more-chunks:{} (offset:{}/limit:{})", data.length, hasMore, offset, limit);

            // Collect data
            os.write(data);

            // calculate new offset and limit --> if hasMore == false the calculation has no effect
            offset = offset + limit;
        }
        return offset;
    }

    private FileDownloadResponse execFileDownloadRequest(final RequestContext ctx, final Long sinkObjectRef, final long offset, final int limit) {
        final FileDownloadRequest fileDownloadRequest = new FileDownloadRequest();
        fileDownloadRequest.setSinkRef(sinkObjectRef);
        fileDownloadRequest.setOffset(offset);
        fileDownloadRequest.setLimit(limit);
        final FileDownloadResponse fileDownloadResponse = (FileDownloadResponse)remoteCaller.execute(ctx, fileDownloadRequest);
        return fileDownloadResponse;
    }
}
