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
package com.arvatosystems.t9t.zkui.filters;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.entities.WriteTrackingMs;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Predicate;

@Singleton
@Named("asyncMessage")
public class AsyncMessageResultTextFilter implements IResultTextFilter<DataWithTracking<AsyncMessageDTO, WriteTrackingMs>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMessageResultTextFilter.class);

    @Override
    public Predicate<DataWithTracking<AsyncMessageDTO, WriteTrackingMs>> getFilter(@Nonnull final String filterText) {
        return dwt -> {
            if (T9tUtil.isBlank(filterText)) {
                return false;
            }
            final AsyncMessageDTO dto = dwt.getData();
            final String json = JsonComposer.toJsonString(dto.getPayload());
            return json.contains(filterText);
        };
    }
}
