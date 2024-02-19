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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.mediaresolver.IMediaResolver;
import com.arvatosystems.t9t.mediaresolver.IMediaResolverSub;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;

@Singleton
public class MediaResolver implements IMediaResolver {

    @Override
    public MediaData resolveLazy(final MediaData in) {
        if (in.getMediaStorageLocation() == null) {
            return in;  // was already resolved
        }
        // check that we have a valid path
        if (in.getText() == null || in.getRawData() != null) {
            throw new T9tException(T9tException.INVALID_LAZY_MEDIADATA_PARAMETERS, in.getMediaStorageLocation());
        }
        final IMediaResolverSub implementation = Jdp.getRequired(IMediaResolverSub.class, in.getMediaStorageLocation().name());
        final ByteArray data = implementation.resolveLazy(in.getText());
        final MediaData resolved = new MediaData(in.getMediaType());
        resolved.setZ(in.getZ());
        resolved.setRawData(data);
        return resolved;
    }
}
