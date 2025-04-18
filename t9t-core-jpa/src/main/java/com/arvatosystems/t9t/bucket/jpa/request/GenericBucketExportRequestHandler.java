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
package com.arvatosystems.t9t.bucket.jpa.request;

import java.util.List;
import java.util.Map;

import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.bucket.request.GenericBucketExportRequest;
import com.arvatosystems.t9t.bucket.services.IBucketEntryMapper;

import de.jpaw.dp.Jdp;

public class GenericBucketExportRequestHandler extends AbstractBucketExportRequestHandler<GenericBucketExportRequest> {

    @Override
    protected void exportChunk(final IOutputSession os, final List<Long> refs, final GenericBucketExportRequest request, final String qualifier,
      final int bucketNoToSelect) {
        final IBucketEntryMapper mapper = Jdp.getRequired(IBucketEntryMapper.class, request.getQualifier());
        final boolean withMore = request.getWithTrackingAndMore();

        // expands refs to Entries
        final Map<Long, Integer> entries = withMore || mapper.alwaysNeedModes() ? super.getModes(qualifier, bucketNoToSelect, refs) : null;
        mapper.writeEntities(request, refs, entries, withMore, os);
    }
}
