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
package com.arvatosystems.t9t.in.be.camel;

import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.SimplePatternEvaluator;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import de.jpaw.dp.Jdp;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultProducer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CamelT9tProducer extends DefaultProducer {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelT9tProducer.class);

    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    public CamelT9tProducer(Endpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        final DataSinkDTO dataSinkDTO = exchange.getProperty("dataSinkDTO", DataSinkDTO.class);
        final CamelT9tEndpoint endpoint = (CamelT9tEndpoint) getEndpoint();
        final String apiKey = endpoint.getApiKey();
        LOGGER.info("Processor invoked for dataSinkDTO {}", dataSinkDTO);

        final String importFilename = deriveImportFilename(dataSinkDTO, exchange);

        // process a file / input stream, which can be binary
        final IInputSession inputSession    = Jdp.getRequired(IInputSession.class);
        inputSession.open(dataSinkDTO.getDataSinkId(), UUID.fromString(apiKey), importFilename, null);

        InputStream inputStream = null;

        try {

            if (dataSinkDTO.getStoreImportUsingFilepattern() != null) {

                final File storageFile = new File(fileUtil.getAbsolutePathForTenant(inputSession.getTenantId(), importFilename));
                storeImport(exchange.getIn().getBody(InputStream.class), storageFile);
                inputStream = new BufferedInputStream(new FileInputStream(storageFile));
            } else {
                inputStream = exchange.getIn().getBody(InputStream.class);
            }

            inputSession.process(inputStream);
        } catch (Exception e) {
            LOGGER.error("Processor error for dataSinkDTO {}: {}", dataSinkDTO, e);
            throw e;
        } finally {
            if (inputSession != null)
                inputSession.close();

            if (inputStream != null)
                inputStream.close();
        }
    }

    private String deriveImportFilename(final DataSinkDTO dataSinkDTO, final Exchange e) throws IOException {
        final String importAsFilepattern = dataSinkDTO.getStoreImportUsingFilepattern();

        String filename = e.getIn().getHeader(Exchange.FILE_NAME, String.class);
        final String ext;
        final String basename;

        if (filename == null) {
            ext = dataSinkDTO.getCommFormatName();
            basename = dataSinkDTO.getDataSinkId() + '_' + e.getExchangeId();
            filename = basename + '.' + ext;
        } else {
            filename =  FilenameUtils.getName(filename);
            ext = FilenameUtils.getExtension(filename);
            basename = FilenameUtils.getBaseName(filename);
        }

        if (importAsFilepattern == null) {
            return filename;
        }

        final Map<String, Object> patternReplacements = new HashMap<>();
        final LocalDateTime       now                 = LocalDateTime.now();
        patternReplacements.put("now", now);
        patternReplacements.put("today", formatDate(now, "yyyy-MM-dd"));
        patternReplacements.put("year", formatDate(now, "yyyy"));
        patternReplacements.put("month", formatDate(now, "MM"));
        patternReplacements.put("day", formatDate(now, "dd"));
        patternReplacements.put("timestamp", formatDate(now, "yyyyMMddHHmmssSSS"));
        patternReplacements.put("id", e.getExchangeId());
        patternReplacements.put("filename", filename);
        patternReplacements.put("extension", ext);
        patternReplacements.put("basename", basename);

        return SimplePatternEvaluator.evaluate(importAsFilepattern, patternReplacements);
    }

    protected void storeImport(final InputStream in, final File storageFile) throws IOException {
        final File storageDir  = storageFile.getParentFile();

        if (!storageDir.exists() && !storageDir.mkdirs()) {
            throw new IOException("Error creating storage dir for import file " + storageFile);
        }

        try {
            FileUtils.copyToFile(in, storageFile);
        } finally {
            in.close();
        }
    }

    protected static String formatDate(final LocalDateTime dt, final String datePattern) {
        if (dt == null) {
            return datePattern.replaceAll("\\w", "0");
        }
        return dt.format(DateTimeFormatter.ofPattern(datePattern));
    }
}
