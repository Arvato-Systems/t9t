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
package com.arvatosystems.t9t.base.be.aws.events

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.services.sqs.AmazonSQSClient
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.IEventImpl
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.media.MediaData
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor
import de.jpaw.dp.Inject
import de.jpaw.dp.Named
import de.jpaw.dp.Singleton
import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets
import java.util.Base64

@Singleton
@Named("S3")
@AddLogger
class AsyncS3Event implements IEventImpl {
    private static final char DELIMITER = ':'

    private final AmazonS3Client s3Client = new AmazonS3Client  // AmazonS3ClientBuilder.defaultClient()   // requires 1.11.x JARs

    override asyncEvent(String address, MediaData data, MediaTypeDescriptor description) {
        LOGGER.debug("Storing media type {} into S3 bucket {}", data.mediaType.name, address);

        val ind = address?.indexOf(DELIMITER)
        if (ind < 1 || ind == address.length-1)
            throw new T9tException(T9tException.BAD_S3_BUCKET_NAME, address)
        // determine the target
        val bucket = address.substring(0, ind).trim
        val path = address.substring(ind+1).trim
        var PutObjectResult result
        try {
            if (data.rawData !== null) {
                val stream  = data.rawData.asByteArrayInputStream
                val meta    = new ObjectMetadata => [
                    contentType     = description.mimeType
                    contentLength   = data.rawData.length
                ]
                result = s3Client.putObject(bucket, path, stream, meta)
            } else {
                // upload text: convert to UTF-8 byte stream
                val binary  = data.text.getBytes(StandardCharsets.UTF_8)  // ugly to create a possibly big object just to stream it away...
                val stream  = new ByteArrayInputStream(binary)
                val meta    = new ObjectMetadata => [
                    contentType     = description.mimeType
                    contentLength   = binary.length
                    contentEncoding = "UTF-8"
                ]
                result = s3Client.putObject(bucket, path, stream, meta)
            }
            LOGGER.debug("Object uploaded, created Etag {}", result.ETag)
        } catch (Exception e) {
            LOGGER.error("Could not store S3 data of type {} to {}: {}: {}", data.mediaType.name, address, e.class.simpleName, e.message)
            throw new T9tException(T9tException.S3_WRITE_ERROR, bucket + ":" + path)
        }
    }
}

@Singleton
@Named("SQS")
@AddLogger
class AsyncSqsEvent implements IEventImpl {
    private static final String DEFAULT_ENDPOINT = "https://sqs.eu-central-1.amazonaws.com"
    private final AmazonSQSClient sqsClient

    @Inject T9tServerConfiguration cfg

    public new() {
        val endpoint = cfg.awsConfiguration?.sqsEndpoint ?: DEFAULT_ENDPOINT
        LOGGER.info("Setting up AWS SQS data sink for endpoint {}", endpoint)
        sqsClient = new AmazonSQSClient
        sqsClient.endpoint = endpoint
    }

    override asyncEvent(String address, MediaData data, MediaTypeDescriptor description) {
        LOGGER.debug("Storing media type {} into SQS queue {}", data.mediaType.name, address);

        var String payload
        if (MediaType.BONAPARTE === data.mediaType?.baseEnum) {
            // control characters exist and clear text is not fine for SQS: base64 encode it!
             payload = Base64.getEncoder().encodeToString(data.text.getBytes(StandardCharsets.UTF_8));
        } else if (description.isText) {
             payload = data.text
        } else {
             payload = data.rawData.asBase64
        }
        try {
            val queueUrl = sqsClient.getQueueUrl(address).queueUrl
            sqsClient.sendMessage(queueUrl, payload)
        } catch (Exception e) {
            LOGGER.error("Could not send SQS message of type {} to {}: {}: {}", data.mediaType.name, address, e.class.simpleName, e.message)
            throw new T9tException(T9tException.SQS_WRITE_ERROR, address)
        }
    }
}
