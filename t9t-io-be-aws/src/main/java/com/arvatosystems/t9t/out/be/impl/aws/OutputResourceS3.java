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
package com.arvatosystems.t9t.out.be.impl.aws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.aws.AwsClientBuilder;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.io.CamelExecutionScheduleType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.services.IIOHook;
import com.arvatosystems.t9t.out.services.IOutputResource;
import com.google.common.base.Objects;

import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nullable;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

@Named("S3")  // name of CommunicationTargetChannelType instance
@Dependent
public class OutputResourceS3 implements IOutputResource {
    private static final char DELIMITER = ':';
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputResourceS3.class);

    protected static final String GZIP_EXTENSION = ".gz";

    protected final IIOHook ioHook = Jdp.getOptional(IIOHook.class);

    protected Charset encoding;
    protected ByteArrayOutputStream inMemoryOutputStream;
    protected OutputStream os;
    protected Charset cs;
    protected DataSinkDTO sinkCfg;
    @Nullable
    protected MediaTypeDescriptor mediaType;
    protected String mediaTypeString = null;
    protected String effectiveFilename = null;

    @Override
    public void close() {
        LOGGER.debug("Closing S3 bucket write stream for {}", effectiveFilename);

        final String targetName = effectiveFilename;

        final int ind = targetName.indexOf(DELIMITER);
        if (ind < 1 || ind == targetName.length() - 1) {
            LOGGER.error("file pattern not good, expected (something):(something else), got {}", targetName);
            throw new T9tException(T9tException.BAD_S3_BUCKET_NAME, targetName);
        }

        // determine the target
        final String bucket = targetName.substring(0, ind).trim();
        final String path = targetName.substring(ind + 1).trim();

        final S3ClientBuilder s3ClientBuilder = AwsClientBuilder.createCustomizedS3ClientBuilder();
        final S3Client s3Client = s3ClientBuilder.build();

        try {
            final byte[] bytes   = inMemoryOutputStream.toByteArray();
            LOGGER.debug("Object upload start: bucket {}, path {}", bucket, path);
            final PutObjectRequest putRq = PutObjectRequest.builder().bucket(bucket).key(path).build();
            final PutObjectResponse result = s3Client.putObject(putRq, RequestBody.fromBytes(bytes));
            LOGGER.debug("Object uploaded, created Etag {}", result.eTag());
        } catch (final Throwable e) {
            LOGGER.error("Could not store S3 data of type {} to {}: {}: {}",
                mediaTypeString, targetName, e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tException.S3_WRITE_ERROR, bucket + ":" + path);
        }

        LOGGER.debug("closing temp stream");
        try {
            os.close();
        } catch (final IOException ex) {
            LOGGER.error("Cannot close stream for {}: {}", sinkCfg.getDataSinkId(), ex.getMessage());
        }
        os = null;

        if ((sinkCfg.getCamelRoute() != null
                && (sinkCfg.getCamelExecution() == null || Objects.equal(sinkCfg.getCamelExecution(), CamelExecutionScheduleType.IN_TRANSACTION)))) {
            LOGGER.error("Cannot transfer resource in transaction for {}", sinkCfg.getDataSinkId());
        }
    }

    @Override
    public String getEffectiveFilename() {
      return this.effectiveFilename;
    }

    @Override
    public OutputStream getOutputStream() {
      return this.os;
    }

    @Override
    public void open(DataSinkDTO config, OutputSessionParameters params, Long sinkRef, String targetName, MediaTypeDescriptor xmediaType, Charset xencoding) {
        this.encoding = xencoding;
        this.mediaType = xmediaType;
        this.mediaTypeString = mediaType != null ? mediaType.getMediaType().name() : "?";
        sinkCfg = config;  // save for later when camel routing may be done

        LOGGER.debug("Storing into S3 bucket {} of type {}", targetName, mediaTypeString);

        effectiveFilename = targetName;

        inMemoryOutputStream = new ByteArrayOutputStream();  // FIXME: there is currently no way to write to S3 via stream. Has to buffer and upload then
        if (ioHook != null) {
            os = ioHook.getEncryptionStream(inMemoryOutputStream, config);
            os = ioHook.getCompressionStream(os, config);
        }
    }

    @Override
    public void write(final String partitionKey, final String recordKey, final byte[] buffer, final int offset, final int len, final boolean isDataRecord) {
        try {
            this.os.write(buffer, offset, len);
        } catch (final IOException e) {
            throw new T9tException(T9tIOException.IO_EXCEPTION);
        }
    }

    @Override
    public void write(String partitionKey, String recordKey, String data) {
        if (data != null) {
            final byte[] bytes = data.getBytes(cs);
            try {
                os.write(bytes);
            } catch (final IOException ex) {
                LOGGER.error("Write exception for {}: {} {}", sinkCfg.getDataSinkId(), ex.getMessage(), ExceptionUtil.causeChain(ex));
                throw new T9tException(T9tIOException.IO_EXCEPTION);
            }
        }
    }
}
