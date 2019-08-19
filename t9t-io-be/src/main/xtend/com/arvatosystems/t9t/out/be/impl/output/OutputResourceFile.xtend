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
package com.arvatosystems.t9t.out.be.impl.output

import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.services.IFileUtil
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.T9tIOException
import com.arvatosystems.t9t.out.services.IFileToCamelProducer
import com.arvatosystems.t9t.out.services.IOutputResource
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor
import de.jpaw.dp.Dependent
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import de.jpaw.dp.Named
import de.jpaw.dp.Provider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPOutputStream
import com.arvatosystems.t9t.io.CamelExecutionScheduleType
import com.arvatosystems.t9t.base.T9tException

@AddLogger
@Named("FILE")  // name of CommunicationTargetChannelType instance
@Dependent
class OutputResourceFile implements IOutputResource {
    @Inject IFileUtil fileUtil
    @Inject Provider<RequestContext> ctxProvider

    protected static final String GZIP_EXTENSION = ".gz";

    protected Charset encoding;
    protected OutputStream os;
    protected String absolutePath;
    protected Charset cs
    protected DataSinkDTO sinkCfg
    protected MediaTypeDescriptor mediaType;
    protected String effectiveFilename = null;

    override close() {
        os.close
        os = null
        // in case of a file export with an additional camel route set, the file is transfered over this route,
        // but only if this should be done within the transaction
        if (sinkCfg.camelRoute !== null && (sinkCfg.camelExecution === null || sinkCfg.camelExecution == CamelExecutionScheduleType.IN_TRANSACTION)) {
            val fileToCamelProducer = Jdp.getRequired(IFileToCamelProducer);
            fileToCamelProducer.sendFileOverCamel(absolutePath, mediaType, sinkCfg);
        }
    }

    override getEffectiveFilename() {
        return this.effectiveFilename
    }

    override getOutputStream() {
        return os
    }

    override open(DataSinkDTO config, OutputSessionParameters params, Long sinkRef, String targetName, MediaTypeDescriptor mediaType, Charset encoding) {
        this.encoding = encoding;
        this.mediaType = mediaType;
        sinkCfg = config  // save for later when camel routing may be done
        absolutePath = fileUtil.getAbsolutePathForTenant(ctxProvider.get().tenantId, targetName);

        if (config.compressed) {
            // if it's compressed but the file name doesn't seem to have GZIP extension, append it
            if (!absolutePath.toLowerCase().endsWith(GZIP_EXTENSION)) {
                absolutePath += GZIP_EXTENSION;
                effectiveFilename = targetName + GZIP_EXTENSION;
            }
        }

        val myFile = new File(absolutePath);
        LOGGER.info("Writing to absolute path {}", absolutePath)

        if (!myFile.isAbsolute()) {
            throw new T9tException(T9tIOException.OUTPUT_FILE_PATH_NOT_ABSOLUTE, absolutePath);
        }

        fileUtil.createFileLocation(absolutePath); // create folders for file

        if (myFile.isDirectory()) {
            throw new T9tException(T9tIOException.OUTPUT_FILE_IS_DIRECTORY, absolutePath);
        }

        try {
            os = new FileOutputStream(myFile);
            if (config.compressed) {
                os = new GZIPOutputStream(os);
            }
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage() + ": " + absolutePath, ex);
            throw new T9tException(T9tIOException.OUTPUT_FILE_OPEN_EXCEPTION, absolutePath);
        }
    }

    override write(byte[] buffer, int offset, int len, boolean isDataRecord) {
        os.write(buffer, offset, len)
    }

    override write(String data) {
        if (data !== null) {
            val bytes = data.getBytes(cs)
            os.write(bytes)
        }
    }
}
