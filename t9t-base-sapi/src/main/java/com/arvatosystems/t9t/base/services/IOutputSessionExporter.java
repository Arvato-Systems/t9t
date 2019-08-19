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
package com.arvatosystems.t9t.base.services;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.arvatosystems.t9t.base.search.AbstractExportRequest;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;

public interface IOutputSessionExporter {
    public <D> SinkCreatedResponse runExport(
            final AbstractExportRequest request,
            final String defaultDataSinkId,
            final Map<String, Object> params,  // optional
            final BiFunction<Long, Integer, List<D>> chunkReader,
            final BiFunction<List<D>, IOutputSession, Long> chunkWriter,  // alternative to writer
            final BiFunction<D, IOutputSession, Long> writer);
}
