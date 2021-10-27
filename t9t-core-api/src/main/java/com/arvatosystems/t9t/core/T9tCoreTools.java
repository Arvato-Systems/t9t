/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.core;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bucket.BucketCounterDTO;
import com.arvatosystems.t9t.bucket.BucketCounterKey;
import com.arvatosystems.t9t.bucket.BucketCounterRef;

public final class T9tCoreTools {
    private T9tCoreTools() { }

    public static String getRequestId(final CannedRequestRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof CannedRequestKey) {
            return ((CannedRequestKey)ref).getRequestId();
        }
        if (ref instanceof CannedRequestDTO) {
            return ((CannedRequestDTO)ref).getRequestId();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "CannedRequestRef of type " + ref.getClass().getCanonicalName());
    }


    public static String getBucketId(final BucketCounterRef ref) {
        if (ref == null)
            return null;
        if (ref instanceof BucketCounterKey) {
            return ((BucketCounterKey)ref).getQualifier();
        }
        if (ref instanceof BucketCounterDTO) {
            return ((BucketCounterDTO)ref).getQualifier();
        }
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "BucketCounterRef of type " + ref.getClass().getCanonicalName());
    }
}
