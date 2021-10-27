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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.file.FileConsumer;
import org.apache.camel.component.file.GenericFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.SimplePatternEvaluator;
import com.arvatosystems.t9t.io.CamelPostProcStrategy;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.services.IFileToCamelProducer;
import com.google.common.collect.ImmutableMap;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.util.ApplicationException;

/**
 *
 * Class for producing a message for file export to the generic camel export route 'direct:outputFile';
 */
@Dependent
public class FileToCamelProducer implements IFileToCamelProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileToCamelProducer.class);

    private final Provider<CamelContext> camelContext = Jdp.getProvider(CamelContext.class);
    /**
     * Produces a message containing file information to the camel export route.
     *
     * @param fileName
     *            file name
     * @param fileType
     *            file type
     * @param sinkCfg
     *            configuration parameters
     */
    @Override
    public void sendFileOverCamel(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, null, null);
    }

    @Override
    public void sendFileOverCamelUsingTargetFileName(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg,
      final String targetFileName) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, targetFileName, null);
    }

    @Override
    public void sendFileOverCamelUsingTargetCamelRoute(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg,
      final String targetCamelRoute) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, null, targetCamelRoute);
    }

    @Override
    public void sendFileOverCamelUsingTargetFileNameAndTargetCamelRoute(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg,
      final String targetFileName, final String targetCamelRoute) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, targetFileName, targetCamelRoute);
    }

    public void doSendFileOverCamel(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg, final String targetFileName,
      final String targetCamelRoute) {

        final ProducerTemplate producerTemplate = camelContext.get().createProducerTemplate();
        final File file = new File(fileName);
        final GenericFile<File> genericFile = FileConsumer.asGenericFile("test", file, sinkCfg.getOutputEncoding(), false);
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
            headerMap.put(Exchange.FILE_NAME, genericFile.getFileNameOnly());
        }

        if (sinkCfg.getCamelFormatIsFmtRoute() != null) {
            headerMap.put("camelRouteIsValidCamelRoute", sinkCfg.getCamelFormatIsFmtRoute());
        } else {
            headerMap.put("camelRouteIsValidCamelRoute", false);
        }


        try {
            producerTemplate.sendBodyAndHeaders("direct:outputFile", genericFile, headerMap);
            successfulRoutingPostProcessing(fileName, fileType, sinkCfg);
        } catch (final CamelExecutionException e) {
            failedRoutingPostProcessing(fileName, fileType, sinkCfg);

            LOGGER.error("CamelExecutionException", e);

            // Wrap camel execution error into Application error in order to prevent rollback
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
        return SimplePatternEvaluator.evaluate(srcFilePath, ImmutableMap.of(
                "fileExtension", resolveFileExtension(srcFilePath),
                "simpleFileName", resolveSimpleFileName(srcFilePath))
        );
    }

    private void successfulRoutingPostProcessing(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg) {
        LOGGER.debug("Post processing after camel successful routing");

        if (sinkCfg.getSuccessRoutingStrategy() == null) {
            return;
        }

        switch (sinkCfg.getSuccessRoutingStrategy()) {
        case DELETE:
            deleteFile(fileName);
            break;
        case MOVE:
            if (sinkCfg.getSuccessDestPattern() != null) {
                final String dstFileName = expandForCamel(sinkCfg.getSuccessDestPattern());
                moveFile(fileName, dstFileName);
            } else {
                LOGGER.error("SuccessRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }

    private void failedRoutingPostProcessing(final String fileName, final MediaTypeDescriptor fileType, final DataSinkDTO sinkCfg) {
        LOGGER.error("Post processing after camel failed routing");
        LOGGER.error("Filename: {}, FileType: {}", fileName, fileType);
        LOGGER.error("SinkCfg: {}", sinkCfg);
        if (sinkCfg.getFailedRoutingStrategy() == null) {
            return;
        }

        switch (sinkCfg.getFailedRoutingStrategy()) {
        case DELETE:
            deleteFile(fileName);
            break;
        case MOVE:
            if (sinkCfg.getFailureDestPattern() != null) {
                final String dstFileName = expandForCamel(sinkCfg.getFailureDestPattern());
                moveFile(fileName, dstFileName);
            } else {
                LOGGER.error("SuccessRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }

    private void moveFile(final String fileName, final String destination) {
        createDestinationFolder(destination);
        final Path srcFile = Paths.get(fileName);
        final Path targetFile = Paths.get(destination);

        try {
            Files.move(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (final IOException e) {
            LOGGER.error("File {} couldn't be moved!", fileName);
            LOGGER.error("An error occurred while moving file.", e);
        }
    }

    private void createDestinationFolder(final String path) {
        final File file = new File(path);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private void deleteFile(final String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (final IOException e) {
            LOGGER.error("File {} couldn't be deleted!", fileName);
            LOGGER.error("An error occurred while deleting file.", e);
        }
    }
}
