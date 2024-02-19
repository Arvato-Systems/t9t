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
package com.arvatosystems.t9t.mediaresolver.impl;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.mediaresolver.IMediaResolverSub;
import com.google.common.io.Files;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;

@Named("FILE") // MediaStorageLocation.FILE.name() is not a constant, apparently
@Singleton
public class MediaResolverFile implements IMediaResolverSub {
    private static final Logger LOGGER = LoggerFactory.getLogger(MediaResolverFile.class);

    @Override
    public ByteArray resolveLazy(final String source) {
        try {
            final File file = new File(source);
            return ByteArray.wrap(Files.asByteSource(file).read());
        } catch (final IOException ioe) {
            LOGGER.error("Failed to resolve FILE path {} for lazy MediaData: {}", source, ioe);
            throw new T9tException(T9tException.FAILED_TO_RESOLVE_MEDIADATA, "FILE");  // do not expose internal path
        }
    }
}
