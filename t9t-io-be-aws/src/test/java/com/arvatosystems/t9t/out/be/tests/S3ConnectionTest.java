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
package com.arvatosystems.t9t.out.be.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class S3ConnectionTest {
    @Disabled
    @Test
    public void testLocalBucket() throws URISyntaxException {
        final URI uri = new URI("http://localhost:9000");
        final AwsCredentials credentials = AwsBasicCredentials.create("DTjjwrvFnPqzudWFevoN", "1BEpS3HAX9cUFbNyRYnnfEQEhVSU1OOKUjGt1JVx");
        final AwsCredentialsProvider credentialProvider = StaticCredentialsProvider.create(credentials);
        final S3Client customS3Client = S3Client
                .builder()
                .forcePathStyle(true)
                .credentialsProvider(credentialProvider)
                .endpointOverride(uri)
                .build();

        // determine the target
        final String bucket = "test";
        final String path = "/documents/invoice/2023-08/invoice_2400000158.pdf";

        final GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(path)
                .build();
        final ResponseInputStream<GetObjectResponse> o = customS3Client.getObject(getObjectRequest);
        assertNotNull(o);
    }
}
