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
package com.arvatosystems.t9t.msglog;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.msglog.request.MessageSearchRequest;
import com.arvatosystems.t9t.msglog.request.MessageStatisticsSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;

public final class T9tMsglogModels implements IViewModelContainer {

    private static final CrudViewModel<MessageDTO, NoTracking> MESSAGE_VIEW_MODEL
      = new CrudViewModel<>(
        MessageDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        MessageSearchRequest.BClass.INSTANCE,
        null);

    private static final CrudViewModel<MessageStatisticsDTO, NoTracking> MESSAGE_STATISTICS_VIEW_MODEL
      = new CrudViewModel<>(
        MessageStatisticsDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        MessageStatisticsSearchRequest.BClass.INSTANCE,
        null);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("requests", MESSAGE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("messageStatistics", MESSAGE_STATISTICS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("messageStatisticsAggregation", MESSAGE_STATISTICS_VIEW_MODEL);
    }
}
