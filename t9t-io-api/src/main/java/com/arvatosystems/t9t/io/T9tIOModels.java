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
package com.arvatosystems.t9t.io;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.io.request.AsyncChannelCrudRequest;
import com.arvatosystems.t9t.io.request.AsyncChannelSearchRequest;
import com.arvatosystems.t9t.io.request.AsyncMessageSearchRequest;
import com.arvatosystems.t9t.io.request.AsyncMessageStatisticsSearchRequest;
import com.arvatosystems.t9t.io.request.AsyncQueueCrudRequest;
import com.arvatosystems.t9t.io.request.AsyncQueueSearchRequest;
import com.arvatosystems.t9t.io.request.CsvConfigurationCrudRequest;
import com.arvatosystems.t9t.io.request.CsvConfigurationSearchRequest;
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest;
import com.arvatosystems.t9t.io.request.DataSinkSearchRequest;
import com.arvatosystems.t9t.io.request.SinkCrudRequest;
import com.arvatosystems.t9t.io.request.SinkSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;

public final class T9tIOModels implements IViewModelContainer {

    private static final CrudViewModel<DataSinkDTO, FullTrackingWithVersion> DATA_SINK_VIEW_MODEL
      = new CrudViewModel<>(
        DataSinkDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        DataSinkSearchRequest.BClass.INSTANCE,
        DataSinkCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<CsvConfigurationDTO, FullTrackingWithVersion> CSV_CONFIGURATION_VIEW_MODEL
      = new CrudViewModel<>(
        CsvConfigurationDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        CsvConfigurationSearchRequest.BClass.INSTANCE,
        CsvConfigurationCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<SinkDTO, FullTrackingWithVersion> SINK_VIEW_MODEL
      = new CrudViewModel<>(
        SinkDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        SinkSearchRequest.BClass.INSTANCE,
        SinkCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AsyncQueueDTO, FullTrackingWithVersion> ASYNC_QUEUE_VIEW_MODEL
      = new CrudViewModel<>(
        AsyncQueueDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AsyncQueueSearchRequest.BClass.INSTANCE,
        AsyncQueueCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AsyncChannelDTO, FullTrackingWithVersion> ASYNC_CHANNEL_VIEW_MODEL
      = new CrudViewModel<>(
        AsyncChannelDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        AsyncChannelSearchRequest.BClass.INSTANCE,
        AsyncChannelCrudRequest.BClass.INSTANCE);
    private static final CrudViewModel<AsyncMessageDTO, WriteTracking> ASYNC_MESSAGE_VIEW_MODEL
      = new CrudViewModel<>(
        AsyncMessageDTO.BClass.INSTANCE,
        WriteTracking.BClass.INSTANCE,
        AsyncMessageSearchRequest.BClass.INSTANCE,
        null);
    private static final CrudViewModel<AsyncMessageStatisticsDTO, NoTracking> ASYNC_MESSAGE_STATISTICS_VIEW_MODEL
      = new CrudViewModel<>(
        AsyncMessageStatisticsDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        AsyncMessageStatisticsSearchRequest.BClass.INSTANCE,
        null);

    @Override
    public void register() {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("sinkSearch",        SINK_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("dataSinkConfig",    DATA_SINK_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("csvConfiguration",  CSV_CONFIGURATION_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("asyncQueueCfg",     ASYNC_QUEUE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("asyncChannelCfg",   ASYNC_CHANNEL_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("asyncMessage",      ASYNC_MESSAGE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("asyncMessageStatistics", ASYNC_MESSAGE_STATISTICS_VIEW_MODEL);
    }
}
