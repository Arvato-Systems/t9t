/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.services.SimplePatternEvaluator;
import com.arvatosystems.t9t.io.CamelPostProcStrategy;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.mediaresolver.IMediaDataSource;
import com.arvatosystems.t9t.out.services.ISinkToCamelProducer;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.util.ApplicationException;

/**
 *
 * Class for producing a message for file export to the generic camel export
 * route 'direct:outputFile';
 */
@Dependent
public class SinkToCamelProducer implements ISinkToCamelProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(SinkToCamelProducer.class);

    private final Provider<CamelContext> camelContext = Jdp.getProvider(CamelContext.class);
    protected final Provider<RequestContext> requestContextProvider = Jdp.getProvider(RequestContext.class);

    /**
     * Produces a message containing file information to the camel export route.
     *
     * @param fileName file name
     * @param fileType file type
     * @param sinkCfg  configuration parameters
     * @throws Exception
     */
    @Override
    public void sendSinkOverCamel(final SinkDTO sink, final DataSinkDTO sinkCfg) throws Exception {
        doSendSinkOverCamel(sink, sinkCfg, null, null);
    }

    @Override
    public void sendSinkOverCamelUsingTargetFileName(final SinkDTO sink, final DataSinkDTO sinkCfg, final String targetFileName) throws Exception {
        doSendSinkOverCamel(sink, sinkCfg, targetFileName, null);
    }

    @Override
    public void sendSinkOverCamelUsingTargetCamelRoute(final SinkDTO sink, final DataSinkDTO sinkCfg, final String targetCamelRoute) throws Exception {
        doSendSinkOverCamel(sink, sinkCfg, null, targetCamelRoute);
    }

    @Override
    public void sendSinkOverCamelUsingTargetFileNameAndTargetCamelRoute(final SinkDTO sink, final DataSinkDTO sinkCfg, final String targetFileName,
            final String targetCamelRoute) throws Exception {
        doSendSinkOverCamel(sink, sinkCfg, targetFileName, targetCamelRoute);
    }

    public void doSendSinkOverCamel(final SinkDTO sink, final DataSinkDTO sinkCfg, final String targetFileName, final String targetCamelRoute)
            throws Exception {
        final MediaTypeDescriptor fileType = MediaTypeInfo.getFormatByType(sink.getCommFormatType());

        final IMediaDataSource srcHandler = Jdp.getOptional(IMediaDataSource.class, sink.getCommTargetChannelType().name());

        final ProducerTemplate producerTemplate = camelContext.get().createProducerTemplate();

        final String fileName = FilenameUtils.getName(sink.getFileOrQueueName());

        final Map<String, Object> headerMap = new HashMap<>();
        headerMap.put("fileName", fileName);
        headerMap.put("fileType", fileType);

        if (targetCamelRoute != null && targetCamelRoute.length() > 0) {
            headerMap.put("camelRoute", targetCamelRoute);
        } else {
            headerMap.put("camelRoute", sinkCfg.getCamelRoute());
        }

        if (targetFileName != null && targetFileName.length() > 0) {
            headerMap.put(Exchange.FILE_NAME, targetFileName);
        } else {
            headerMap.put(Exchange.FILE_NAME, fileName);
        }

        if (sinkCfg.getCamelFormatIsFmtRoute() != null) {
            headerMap.put("camelRouteIsValidCamelRoute", sinkCfg.getCamelFormatIsFmtRoute());
        } else {
            headerMap.put("camelRouteIsValidCamelRoute", false);
        }

        try {
            final String filePath = srcHandler.getAbsolutePathForTenant(sink.getFileOrQueueName(), requestContextProvider.get().tenantId);
            producerTemplate.sendBodyAndHeaders("direct:outputFile", srcHandler.open(filePath), headerMap);

            successfulRoutingPostProcessing(sink, fileType, sinkCfg);
        } catch (final CamelExecutionException e) {
            failedRoutingPostProcessing(sink, fileType, sinkCfg);

            LOGGER.error("CamelExecutionException", e);

            // Wrap camel execution error into Application error in order to prevent
            // rollback
            final ApplicationException applicationException = new ApplicationException(T9tIOException.NOT_TRANSFERRED, e.getMessage());
            applicationException.setStackTrace(e.getStackTrace());
            applicationException.addSuppressed(e);
            throw applicationException;
        }
    }

    // stuff for Camel
    private static String resolveSimpleFileName(final String filePath) {
        final String normalizedPath = filePath.replaceAll("\\\\", "/");
        final int startIdx = normalizedPath.lastIndexOf("/");
        final int endIdx = normalizedPath.lastIndexOf(".");

        if ((startIdx > -1) && (endIdx > -1)) {
            return normalizedPath.substring(startIdx + 1, endIdx);
        }

        return "";
    }

    private static String resolveFileExtension(final String filePath) {
        if (filePath.contains(".")) {
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        }

        return "";
    }

    private static String expandForCamel(final String srcFilePath) {
        return SimplePatternEvaluator.evaluate(srcFilePath,
                ImmutableMap.of("fileExtension", resolveFileExtension(srcFilePath), "simpleFileName", resolveSimpleFileName(srcFilePath)));
    }

    private void successfulRoutingPostProcessing(final SinkDTO sink, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg) {
        LOGGER.debug("Post processing after camel successful routing");

        if (sinkCfg.getSuccessRoutingStrategy() == null) {
            return;
        }

        switch (sinkCfg.getSuccessRoutingStrategy()) {
        case DELETE:
            LOGGER.error("SuccessRoutingStrategy is set to {} but its not implemented yet", CamelPostProcStrategy.DELETE);
            break;
        case MOVE:
            if (sinkCfg.getSuccessDestPattern() != null) {
                final String dstFileName = expandForCamel(sinkCfg.getSuccessDestPattern());
                LOGGER.error("SuccessRoutingStrategy is set to {} but its not implemented yet", CamelPostProcStrategy.MOVE);
            } else {
                LOGGER.error("SuccessRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }

    private void failedRoutingPostProcessing(final SinkDTO sink, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg) {
        LOGGER.error("Post processing after camel failed routing");
        LOGGER.error("Sink: {}, FileType: {}", sink, fileType);
        LOGGER.error("SinkCfg: {}", sinkCfg);
        if (sinkCfg.getFailedRoutingStrategy() == null) {
            return;
        }

        switch (sinkCfg.getFailedRoutingStrategy()) {
        case DELETE:
            LOGGER.error("FailedRoutingStrategy is set to {} but its not implemented yet", CamelPostProcStrategy.DELETE);
            break;
        case MOVE:
            if (sinkCfg.getFailureDestPattern() != null) {
                final String dstFileName = expandForCamel(sinkCfg.getFailureDestPattern());
                LOGGER.error("FailedRoutingStrategy is set to {} but its not implemented yet", CamelPostProcStrategy.MOVE);
            } else {
                LOGGER.error("FailedRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }
}
