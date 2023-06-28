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
package com.arvatosystems.t9t.out.be.impl.output;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.CamelExecutionScheduleType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.services.IFileToCamelProducer;
import com.arvatosystems.t9t.out.services.IOutputResource;
import com.google.common.base.Objects;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;
import de.jpaw.util.ExceptionUtil;

@Named("FILE")
@Dependent
public class OutputResourceFile implements IOutputResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputResourceFile.class);

    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    protected Charset encoding;
    protected OutputStream os;
    protected String absolutePath;
    protected Charset cs;
    protected DataSinkDTO sinkCfg;
    protected MediaTypeDescriptor mediaType;
    protected String effectiveFilename = null;

    @Override
    public void close() {
        try {
            os.close();
            os = null;
        } catch (IOException e) {
            LOGGER.error("Exception during close: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.CANNOT_CLOSE_SINK, e.getMessage());
        }
        // in case of a file export with an additional camel route set, the file is transfered over this route,
        // but only if this should be done within the transaction
        if ((sinkCfg.getCamelRoute() != null && (sinkCfg.getCamelExecution() == null
            || Objects.equal(sinkCfg.getCamelExecution(), CamelExecutionScheduleType.IN_TRANSACTION)))) {
            final IFileToCamelProducer fileToCamelProducer = Jdp.<IFileToCamelProducer>getRequired(IFileToCamelProducer.class);
            fileToCamelProducer.sendFileOverCamel(absolutePath, mediaType, sinkCfg);
        }
    }

    @Override
    public String getEffectiveFilename() {
        return effectiveFilename;
    }

    @Override
    public OutputStream getOutputStream() {
        return os;
    }

    @Override
    public void open(final DataSinkDTO config, final OutputSessionParameters params, final Long sinkRef,
            final String targetName, final MediaTypeDescriptor xmediaType, final Charset xencoding) {
        this.encoding = xencoding;
        this.mediaType = xmediaType;
        sinkCfg = config;  // save for later when camel routing may be done
        absolutePath = fileUtil.getAbsolutePathForTenant(ctxProvider.get().tenantId, targetName);

        if (fileUtil.needGzipExtension(targetName, config.getCompressed())) {
            absolutePath += IFileUtil.GZIP_EXTENSION;
            effectiveFilename = targetName + IFileUtil.GZIP_EXTENSION;
        }

        final File myFile = new File(absolutePath);
        LOGGER.info("Writing to absolute path {}", absolutePath);

        if (!myFile.isAbsolute()) {
            throw new T9tException(T9tIOException.OUTPUT_FILE_PATH_NOT_ABSOLUTE, absolutePath);
        }

        fileUtil.createFileLocation(absolutePath); // create folders for file

        if (myFile.isDirectory()) {
            throw new T9tException(T9tIOException.OUTPUT_FILE_IS_DIRECTORY, absolutePath);
        }

        try {
            os = new FileOutputStream(myFile);
            if (config.getCompressed()) {
                os = new GZIPOutputStream(os);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage() + ": " + absolutePath, ex);
            throw new T9tException(T9tIOException.OUTPUT_FILE_OPEN_EXCEPTION, absolutePath);
        }
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final byte[] buffer, final int offset, final int len, final boolean isDataRecord) {
        try {
            os.write(buffer, offset, len);
        } catch (final IOException e) {
            LOGGER.error("Exception during write: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.IO_EXCEPTION, e.getMessage());
        }
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final String data) {
        try {
            if (data != null) {
                final byte[] bytes = data.getBytes(cs);
                os.write(bytes);
            }
        } catch (final IOException e) {
            LOGGER.error("Exception during write: {}", ExceptionUtil.causeChain(e));
            throw new T9tException(T9tIOException.IO_EXCEPTION, e.getMessage());
        }
    }
}
