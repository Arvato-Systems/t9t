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
package com.arvatosystems.t9t.io.jpa.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest;
import com.arvatosystems.t9t.out.services.ISinkToCamelProducer;

import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;

public class ProcessCamelRouteRequestHandler extends AbstractRequestHandler<ProcessCamelRouteRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessCamelRouteRequestHandler.class);
    protected static final String GZIP_EXTENSION = ".gz";

    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    private final ISinkToCamelProducer sinkToCamelProducer = Jdp.getRequired(ISinkToCamelProducer.class);
    private final ISinkEntityResolver sinkResolver = Jdp.getRequired(ISinkEntityResolver.class);
    private final IDataSinkDTOMapper dataSinkMapper = Jdp.getRequired(IDataSinkDTOMapper.class);
    private final ISinkDTOMapper sinkMapper = Jdp.getRequired(ISinkDTOMapper.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ProcessCamelRouteRequest rq) throws Exception {
        final SinkEntity sink = sinkResolver.getEntityDataForKey(rq.getSinkRef());
        if (sink.getCamelTransferStatus() == ExportStatusEnum.RESPONSE_OK) {
            LOGGER.info("Sink {} for {} was already transferred - skipping", rq.getSinkRef(), sink.getFileOrQueueName());
            return ok();
        }
        String absolutePath = fileUtil.getAbsolutePathForTenant(ctx.tenantId, sink.getFileOrQueueName());

        if (sink.getDataSink().getCompressed()) {
            // if it's compressed but the file name doesn't seem to have GZIP extension, append it
            if (!absolutePath.toLowerCase().endsWith(GZIP_EXTENSION)) {
                absolutePath += GZIP_EXTENSION;
            }
        }
        LOGGER.info("Transferring sink {} for path {}", rq.getSinkRef(), absolutePath);
        try {
            final DataSinkDTO dataSinkDto = dataSinkMapper.mapToDto(sink.getDataSink());
            final SinkDTO sinkDto = sinkMapper.mapToDto(sink);

            if (rq.getTargetFileName() != null && rq.getTargetFileName().length() > 0
              && rq.getTargetCamelRoute() != null && rq.getTargetCamelRoute().length() > 0) {
                sinkToCamelProducer.sendSinkOverCamelUsingTargetFileNameAndTargetCamelRoute(sinkDto, dataSinkDto, rq.getTargetFileName(),
                        rq.getTargetCamelRoute());
            } else if (rq.getTargetFileName() != null && rq.getTargetFileName().length() > 0) {
                sinkToCamelProducer.sendSinkOverCamelUsingTargetFileName(sinkDto, dataSinkDto,
                        rq.getTargetFileName());
            } else if (rq.getTargetCamelRoute() != null && rq.getTargetCamelRoute().length() > 0) {
                sinkToCamelProducer.sendSinkOverCamelUsingTargetCamelRoute(sinkDto, dataSinkDto,
                        rq.getTargetCamelRoute());
            } else {
                sinkToCamelProducer.sendSinkOverCamel(sinkDto, dataSinkDto);
            }
            // assuming the transfer was successful if we did not get an exception here, mark the sink as transferred
            sink.setCamelTransferStatus(ExportStatusEnum.RESPONSE_OK);
            sinkResolver.flush(); // to solve missing camel transfer status on aynsc request
            LOGGER.debug("Setting Sink {} to camelTransferStatus: {} ", sink.getObjectRef(), sink.getCamelTransferStatus());
        } catch (final Exception e) {
            LOGGER.error("Camel transfer failed: {}", ExceptionUtil.causeChain(e));
            sink.setCamelTransferStatus(ExportStatusEnum.RESPONSE_ERROR);
            LOGGER.debug("Setting Sink {} to camelTransferStatus: {} ", sink.getObjectRef(), sink.getCamelTransferStatus());
            return ok(T9tIOException.NOT_TRANSFERRED);
        }
        return ok();
    }
}
