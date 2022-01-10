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
package com.arvatosystems.t9t.base.be.aws.events;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEventImpl;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("S3")
public class AsyncS3Event implements IEventImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncS3Event.class);

    private static final char DELIMITER = ':';

    private final AmazonS3Client s3Client = new AmazonS3Client();  // AmazonS3ClientBuilder.defaultClient()   // requires 1.11.x JARs

    @Override
    public void asyncEvent(final String address, final MediaData data, final MediaTypeDescriptor description) {
        LOGGER.debug("Storing media type {} into S3 bucket {}", data.getMediaType().name(), address);

        final int ind = address == null ? -1 : address.indexOf(DELIMITER);
        if (ind < 1 || ind == address.length() - 1) {
            throw new T9tException(T9tException.BAD_S3_BUCKET_NAME, address);
        }
        // determine the target
        final String bucket = address.substring(0, ind).trim();
        final String path = address.substring(ind + 1).trim();
        final PutObjectResult result;
        try {
            if (data.getRawData() != null) {
                final ByteArrayInputStream stream = data.getRawData().asByteArrayInputStream();
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentType(description.getMimeType());
                meta.setContentLength(data.getRawData().length());
                result = s3Client.putObject(bucket, path, stream, meta);
            } else {
                // upload text: convert to UTF-8 byte stream
                final byte[] binary  = data.getText().getBytes(StandardCharsets.UTF_8);  // ugly to create a possibly big object just to stream it away...
                final ByteArrayInputStream stream  = new ByteArrayInputStream(binary);
                final ObjectMetadata meta = new ObjectMetadata();
                meta.setContentType(description.getMimeType());
                meta.setContentLength(binary.length);
                meta.setContentEncoding("UTF-8");
                result = s3Client.putObject(bucket, path, stream, meta);
            }
            LOGGER.debug("Object uploaded, created Etag {}", result.getETag());
        } catch (Exception e) {
            LOGGER.error("Could not store S3 data of type {} to {}: {}: {}", data.getMediaType().name(), address, e.getClass().getSimpleName(), e.getMessage());
            throw new T9tException(T9tException.S3_WRITE_ERROR, bucket + ":" + path);
        }
    }
}
