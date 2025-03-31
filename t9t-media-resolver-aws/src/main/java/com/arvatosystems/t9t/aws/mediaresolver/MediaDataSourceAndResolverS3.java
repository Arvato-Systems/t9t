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
package com.arvatosystems.t9t.aws.mediaresolver;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.aws.AwsClientBuilder;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.mediaresolver.IMediaDataSource;
import com.arvatosystems.t9t.mediaresolver.IMediaResolverSub;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

// file download handler implementation for S3 buckets
@Singleton
@Named("S3")
public class MediaDataSourceAndResolverS3 implements IMediaDataSource, IMediaResolverSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaDataSourceAndResolverS3.class);
    private static final char DELIMITER = ':';

    @Override
    public InputStream open(final String targetName) throws Exception {
        final int ind = targetName.indexOf(DELIMITER);
        if (ind < 1 || ind == targetName.length() - 1) {
            LOGGER.error("file pattern not good, expected (something):(something else), got {}", targetName);
            throw new T9tException(T9tException.BAD_S3_BUCKET_NAME, targetName);
        }

        final S3ClientBuilder s3ClientBuilder = AwsClientBuilder.createCustomizedS3ClientBuilder();
        final S3Client s3Client = s3ClientBuilder.build();

        // determine the target
        final String bucket = targetName.substring(0, ind).trim();
        final String path = targetName.substring(ind + 1).trim();

        final GetObjectRequest getObjectRequest = GetObjectRequest.builder().bucket(bucket).key(path).build();
        final ResponseInputStream<GetObjectResponse> o = s3Client.getObject(getObjectRequest);
        return o;
    }

    @Override
    public ByteArray resolveLazy(final String source) {
        try {
            return ByteArray.fromInputStream(open(source), 0);
        } catch (final Exception e) {
            LOGGER.error("Failed to resolve S3 path {} for lazy MediaData: {}", source, e);
            throw new T9tException(T9tException.FAILED_TO_RESOLVE_MEDIADATA, "S3");  // do not expose internal path
        }
    }
}
