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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.jpa.impl.AbstractLeanSearchRequestHandler;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.request.LeanDataSinkSearchRequest;

import de.jpaw.dp.Jdp;

public class LeanDataSinkSearchRequestHandler extends AbstractLeanSearchRequestHandler<LeanDataSinkSearchRequest, DataSinkEntity> {
    public LeanDataSinkSearchRequestHandler() {
        super(Jdp.getRequired(IDataSinkEntityResolver.class), (DataSinkEntity it) -> {
            final String description = it.getDescription() == null ? "?" : it.getDescription();
            return new Description(null, it.getDataSinkId(), description, false, false);
        });
    }
}
