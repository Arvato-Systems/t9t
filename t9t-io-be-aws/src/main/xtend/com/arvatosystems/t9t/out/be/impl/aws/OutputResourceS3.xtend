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
package com.arvatosystems.t9t.out.be.impl.aws

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.T9tIOException
import com.arvatosystems.t9t.out.services.IOutputResource
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor
import de.jpaw.dp.Dependent
import de.jpaw.dp.Named
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.Charset

@AddLogger
@Named("S3")  // name of CommunicationTargetChannelType instance
@Dependent
class OutputResourceS3 implements IOutputResource {
    static final char DELIMITER = ':'

    protected static final String GZIP_EXTENSION = ".gz";

    protected final AmazonS3Client s3Client = new AmazonS3Client
    protected Charset encoding;
    protected ByteArrayOutputStream os;
    protected Charset cs
    protected DataSinkDTO sinkCfg
    protected MediaTypeDescriptor mediaType;
    protected String effectiveFilename = null;

    override close() {
        LOGGER.debug("Closing S3 bucket write stream for {}", effectiveFilename);

        val targetName = effectiveFilename

        val ind = targetName.indexOf(DELIMITER)
        if (ind < 1 || ind == targetName.length-1) {
            LOGGER.error("file pattern not good, expected (something):(something else), got {}", targetName)
            throw new T9tException(T9tException.BAD_S3_BUCKET_NAME, targetName)
        }

        // determine the target
        val bucket = targetName.substring(0, ind).trim
        val path = targetName.substring(ind+1).trim

        try {
            val bytes   = os.toByteArray
            val stream  = new ByteArrayInputStream(bytes)
            val meta    = new ObjectMetadata => [
                contentType     = mediaType.mimeType
                contentLength   = bytes.length
            ]
            LOGGER.debug("Object upload start: bucket {}, path {}", bucket, path)
            val result = s3Client.putObject(new PutObjectRequest(bucket, path, stream, meta))
            LOGGER.debug("Object uploaded, created Etag {}", result.ETag)
        } catch (Throwable e) {
            LOGGER.error("Could not store S3 data of type {} to {}: {}: {}", mediaType.mediaType.name, targetName, e.class.simpleName, e.message)
            throw new T9tException(T9tException.S3_WRITE_ERROR, bucket + ":" + path)
        }

        LOGGER.debug("closing temp stream")
        os.close
        os = null
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

        LOGGER.debug("Storing media type {} into S3 bucket {}", mediaType.mediaType.name, targetName);

        effectiveFilename = targetName

        try {
            os = new ByteArrayOutputStream  // FIXME: there is currently no way to write to S3 via stream. Has to buffer and upload then
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
            throw new T9tException(T9tIOException.OUTPUT_FILE_OPEN_EXCEPTION);
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
