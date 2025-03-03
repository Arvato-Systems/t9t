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
package com.arvatosystems.t9t.aws;

import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.api.ConfigurationReader;
import de.jpaw.util.ConfigurationReaderFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.auth.credentials.SystemPropertyCredentialsProvider;
import software.amazon.awssdk.auth.credentials.WebIdentityTokenFileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

public final class AwsClientBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(AwsClientBuilder.class);

    public static final ConfigurationReader CONFIG_READER = ConfigurationReaderFactory.getConfigReaderForName("t9t.s3", null);

    private static final String S3_URI = "t9t.s3.uri";
    private static final String S3_PATH_SYTLE = "t9t.s3.pathstyle";
    private static final String S3_KEY = "t9t.s3.key";
    private static final String S3_SECRET = "t9t.s3.secret";
    private static final String S3_REGION = "t9t.s3.region";
    private static final String S3_IRSA = "t9t.s3.irsa";

    private AwsClientBuilder() { }

    public static S3ClientBuilder createCustomizedS3ClientBuilder() {

        final S3ClientBuilder s3ClientBuilder = S3Client.builder();

        try {
            final String forcePathStyle = CONFIG_READER.getProperty(S3_PATH_SYTLE, null);
            if (forcePathStyle != null) {
                s3ClientBuilder.forcePathStyle(true);
            }

            final String uri = CONFIG_READER.getProperty(S3_URI, null);
            if (uri != null) {
                URI s3Uri = new URI(uri);
                s3ClientBuilder.endpointOverride(s3Uri);
            }

            // check for Kubernetes IRSA
            final String needIrsa = CONFIG_READER.getProperty(S3_IRSA, null);
            final String key = CONFIG_READER.getProperty(S3_KEY, null);
            final String secret = CONFIG_READER.getProperty(S3_SECRET, null);
            if (key != null && secret != null) {
                final AwsCredentials credentials = AwsBasicCredentials.create(key, secret);
                final AwsCredentialsProvider credentialProvider = StaticCredentialsProvider.create(credentials);

                s3ClientBuilder.credentialsProvider(credentialProvider);
            } else if (needIrsa != null) {
                final AwsCredentialsProviderChain.Builder cpc = AwsCredentialsProviderChain.builder();

                cpc.addCredentialsProvider(WebIdentityTokenFileCredentialsProvider.create()); // kubernetes IRSA
                cpc.addCredentialsProvider(SystemPropertyCredentialsProvider.create());
                cpc.addCredentialsProvider(EnvironmentVariableCredentialsProvider.create());
                cpc.addCredentialsProvider(ProfileCredentialsProvider.create());

                s3ClientBuilder.credentialsProvider(cpc.build());
            }

            final String region = CONFIG_READER.getProperty(S3_REGION, null);
            if (region != null) {
                final Region s3Region = Region.of(region);
                s3ClientBuilder.region(s3Region);
            }

        } catch (Exception e) {
            LOGGER.error("Exception on s3 client customization. Use default.", e);
        }

        return s3ClientBuilder;
    }
}
