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
package com.arvatosystems.t9t.tfi.component.dropdown;

import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.bucket.BucketCounterDTO;
import com.arvatosystems.t9t.bucket.BucketCounterKey;
import com.arvatosystems.t9t.bucket.BucketCounterRef;
import com.arvatosystems.t9t.bucket.request.LeanBucketCounterSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("bucketId")
@Singleton
public class Dropdown28FactoryBucketCounter implements IDropdown28DbFactory<BucketCounterRef> {

    @Override
    public String getDropdownId() {
        return "bucketId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanBucketCounterSearchRequest();
    }

    @Override
    public BucketCounterRef createRef(Long ref) {
        return new BucketCounterRef(ref);
    }

    @Override
    public BucketCounterRef createKey(String id) {
        return new BucketCounterKey(id);
    }

    @Override
    public Dropdown28Db<BucketCounterRef> createInstance() {
        return new Dropdown28Db<BucketCounterRef>(this);
    }

    @Override
    public String getIdFromKey(BucketCounterRef key) {
        if (key instanceof BucketCounterKey)
            return ((BucketCounterKey)key).getQualifier();
        if (key instanceof BucketCounterDTO)
            return ((BucketCounterDTO)key).getQualifier();
        return null;
    }
}
