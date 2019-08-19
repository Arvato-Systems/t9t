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
package com.arvatosystems.t9t.bucket.services;

import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.bucket.request.GenericBucketExportRequest;

/**
 * Interface, implementations convert from a BucketEntry to a DataWithTrackingAndMore, or just data.
 * Implementations are singletons, @Named with a qualifier.
 * Implementations must implement one of the default methods.
 *  */
public interface IBucketEntryMapper {
    default boolean alwaysNeedModes() { return false; }
    void writeEntities(GenericBucketExportRequest rp, List<Long> refs, Map<Long, Integer> entries, boolean withTrackingAndMore, IOutputSession os);
}
