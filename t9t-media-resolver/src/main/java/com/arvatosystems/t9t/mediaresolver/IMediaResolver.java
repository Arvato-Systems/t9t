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
package com.arvatosystems.t9t.mediaresolver;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Interface for implementations which resolve a lazy MediaData object, i.e. turn a lazy one into one with actual contents.
 * There is one unnamed implementation which is called by services needing this method, and it delegates to an appropriate @Named
 * implementation for the specific data source of <code>IMediaResolverSub</code>.
 */
public interface IMediaResolver {

    /** Resolves the contents of a lazy MediaData structure. */
    @Nonnull MediaData resolveLazy(@Nonnull MediaData in);

    /** Checks if the object is lazy. */
    default boolean isLazy(@Nullable MediaData in) {
        return in != null && in.getMediaStorageLocation() != null;
    }
}
