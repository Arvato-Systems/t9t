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
    public void sendFileOverCamel(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, null, null);
    }

    @Override
    public void sendFileOverCamelUsingTargetFileName(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetFileName) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, targetFileName, null);
    }

    @Override
    public void sendFileOverCamelUsingTargetCamelRoute(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetCamelRoute) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, null, targetCamelRoute);
    }

    @Override
    public void sendFileOverCamelUsingTargetFileNameAndTargetCamelRoute(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetFileName, String targetCamelRoute) {
        doSendFileOverCamel(fileName, fileType, sinkCfg, targetFileName, targetCamelRoute);
    }

    public void doSendFileOverCamel(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg, String targetFileName, String targetCamelRoute) {

        ProducerTemplate producerTemplate = camelContext.get().createProducerTemplate();
        File file = new File(fileName);
        GenericFile<File> genericFile = FileConsumer.asGenericFile("test", file, sinkCfg.getOutputEncoding(), false);
        Map<String, Object> headerMap = new HashMap<String, Object>();
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
        } catch (CamelExecutionException e) {
            failedRoutingPostProcessing(fileName, fileType, sinkCfg);

            LOGGER.error("CamelExecutionException", e);

            // Wrap camel execution error into Application error in order to prevent rollback
            ApplicationException applicationException = new ApplicationException(T9tIOException.NOT_TRANSFERRED, e.getMessage());
            applicationException.setStackTrace(e.getStackTrace());
            applicationException.addSuppressed(e);
            throw applicationException;
        }
    }

    // stuff for Camel
    private static String resolveSimpleFileName(String filePath) {
        String normalizedPath = filePath.replaceAll("\\\\", "/");
        int startIdx = normalizedPath.lastIndexOf("/");
        int endIdx = normalizedPath.lastIndexOf(".");

        if ((startIdx > -1) && (endIdx > -1)) {
            return normalizedPath.substring(startIdx + 1, endIdx);
        }

        return "";
    }

    private static String resolveFileExtension(String filePath) {
        if (filePath.contains(".")) {
            return filePath.substring(filePath.lastIndexOf(".") + 1);
        }

        return "";
    }

    private static String expandForCamel(String srcFilePath) {
        return SimplePatternEvaluator.evaluate(srcFilePath, ImmutableMap.of(
                "fileExtension", resolveFileExtension(srcFilePath),
                "simpleFileName", resolveSimpleFileName(srcFilePath))
        );
    }

    private void successfulRoutingPostProcessing(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg) {
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
                String dstFileName = expandForCamel(sinkCfg.getSuccessDestPattern());
                moveFile(fileName, dstFileName);
            } else {
                LOGGER.error("SuccessRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }

    private void failedRoutingPostProcessing(String fileName, MediaTypeDescriptor fileType, DataSinkDTO sinkCfg) {
        LOGGER.error("Post processing after camel failed routing");
        LOGGER.error("Filename: {}, FileType: {}",fileName, fileType);
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
                String dstFileName = expandForCamel(sinkCfg.getFailureDestPattern());
                moveFile(fileName, dstFileName);
            } else {
                LOGGER.error("SuccessRoutingStrategy is set to {} but SuccessDestPath is null", CamelPostProcStrategy.MOVE);
            }
            break;
        default:
            break;

        }
    }

    private void moveFile(String fileName, String destination) {
        createDestinationFolder(destination);
        Path srcFile = Paths.get(fileName);
        Path targetFile = Paths.get(destination);

        try {
            Files.move(srcFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.error("File {} couldn't be moved!", fileName);
            LOGGER.error("An error occurred while moving file.", e);
        }
    }

    private void createDestinationFolder(String path) {
        File file = new File(path);

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    private void deleteFile(String fileName) {
        try {
            Files.deleteIfExists(Paths.get(fileName));
        } catch (IOException e) {
            LOGGER.error("File {} couldn't be deleted!", fileName);
            LOGGER.error("An error occurred while deleting file.", e);
        }
    }
}
