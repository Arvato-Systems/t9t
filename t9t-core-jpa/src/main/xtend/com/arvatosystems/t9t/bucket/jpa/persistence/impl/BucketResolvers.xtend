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
package com.arvatosystems.t9t.bucket.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.active.AutoResolver42
import com.arvatosystems.t9t.bucket.BucketCounterRef
import com.arvatosystems.t9t.bucket.BucketEntryKey
import com.arvatosystems.t9t.bucket.jpa.entities.BucketCounterEntity
import com.arvatosystems.t9t.bucket.jpa.entities.BucketEntryEntity

@AutoResolver42
class BucketResolvers {
    def BucketCounterEntity           getBucketCounterEntity(BucketCounterRef key) { return null; }
    def BucketCounterEntity           findByQualifier       (boolean onlyActive,   String qualifier)   { return null; }
    def BucketEntryEntity             getBucketEntryEntity  (BucketEntryKey   key) { return null; }
}
