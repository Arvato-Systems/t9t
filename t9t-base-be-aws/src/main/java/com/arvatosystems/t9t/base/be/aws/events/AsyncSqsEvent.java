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
package com.arvatosystems.t9t.base.be.aws.events;

import com.amazonaws.services.sqs.AmazonSQSClient;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.IEventImpl;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
@Named("SQS")
public class AsyncSqsEvent implements IEventImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncSqsEvent.class);

    private static final String DEFAULT_ENDPOINT = "https://sqs.eu-central-1.amazonaws.com";
    private final AmazonSQSClient sqsClient;

    private final T9tServerConfiguration cfg = Jdp.getRequired(T9tServerConfiguration.class);

    public AsyncSqsEvent() {
        final String endpoint;
        if (cfg.getAwsConfiguration() == null || cfg.getAwsConfiguration().getSqsEndpoint() == null) {
            endpoint = DEFAULT_ENDPOINT;
        } else {
            endpoint = cfg.getAwsConfiguration().getSqsEndpoint();
        }

        LOGGER.info("Setting up AWS SQS data sink for endpoint {}", endpoint);
        sqsClient = new AmazonSQSClient();
        sqsClient.setEndpoint(endpoint);
    }

    @Override
    public void asyncEvent(final String address, final MediaData data, final MediaTypeDescriptor description) {
        LOGGER.debug("Storing media type {} into SQS queue {}", data.getMediaType().name(), address);
        final String payload;

        if (data.getMediaType() != null && MediaType.BONAPARTE == data.getMediaType().getBaseEnum()) {
            // control characters exist and clear text is not fine for SQS: base64 encode it!
            payload = Base64.getEncoder().encodeToString(data.getText().getBytes(StandardCharsets.UTF_8));
        } else if (description.getIsText()) {
            payload = data.getText();
        } else {
            payload = data.getRawData().asBase64();
        }
        try {
            final String queueUrl = sqsClient.getQueueUrl(address).getQueueUrl();
            sqsClient.sendMessage(queueUrl, payload);
        } catch (Exception e) {
            LOGGER.error("Could not send SQS message of type {} to {}: {}: {}", data.getMediaType().name(), address, e.getClass().getSimpleName(),
                    e.getMessage());
            throw new T9tException(T9tException.SQS_WRITE_ERROR, address);
        }
    }
}
