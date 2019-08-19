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
package com.arvatosystems.t9t.core;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.BucketTracking;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.entities.WriteTracking;
import com.arvatosystems.t9t.base.request.ComponentInfoDTO;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.batch.SliceTrackingDTO;
import com.arvatosystems.t9t.batch.StatisticsDTO;
import com.arvatosystems.t9t.batch.request.SliceTrackingCrudRequest;
import com.arvatosystems.t9t.batch.request.SliceTrackingSearchRequest;
import com.arvatosystems.t9t.batch.request.StatisticsCrudRequest;
import com.arvatosystems.t9t.batch.request.StatisticsSearchRequest;
import com.arvatosystems.t9t.bucket.BucketCounterDTO;
import com.arvatosystems.t9t.bucket.BucketEntryDTO;
import com.arvatosystems.t9t.bucket.request.BucketCounterCrudRequest;
import com.arvatosystems.t9t.bucket.request.BucketCounterSearchRequest;
import com.arvatosystems.t9t.bucket.request.BucketEntryCrudRequest;
import com.arvatosystems.t9t.bucket.request.BucketEntrySearchRequest;
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest;
import com.arvatosystems.t9t.core.request.CannedRequestSearchRequest;
import com.arvatosystems.t9t.core.request.ComponentInfoSearchRequest;
import com.arvatosystems.t9t.core.request.ProcessStatusSearchRequest;
import com.arvatosystems.t9t.event.ListenerConfigDTO;
import com.arvatosystems.t9t.event.SubscriberConfigDTO;
import com.arvatosystems.t9t.event.request.ListenerConfigCrudRequest;
import com.arvatosystems.t9t.event.request.ListenerConfigSearchRequest;
import com.arvatosystems.t9t.event.request.SubscriberConfigCrudRequest;
import com.arvatosystems.t9t.event.request.SubscriberConfigSearchRequest;

import de.jpaw.bonaparte.pojos.api.NoTracking;

public class T9tCoreModels implements IViewModelContainer {
    public static final CrudViewModel<StatisticsDTO, WriteTracking> STATISTICS_VIEW_MODEL = new CrudViewModel<StatisticsDTO, WriteTracking>(
        StatisticsDTO.BClass.INSTANCE,
        WriteTracking.BClass.INSTANCE,
        StatisticsSearchRequest.BClass.INSTANCE,
        StatisticsCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<CannedRequestDTO, FullTrackingWithVersion> CANNED_REQUEST_VIEW_MODEL = new CrudViewModel<CannedRequestDTO, FullTrackingWithVersion>(
        CannedRequestDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        CannedRequestSearchRequest.BClass.INSTANCE,
        CannedRequestCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<SliceTrackingDTO, FullTrackingWithVersion> SLICE_TRACKING_VIEW_MODEL = new CrudViewModel<SliceTrackingDTO, FullTrackingWithVersion>(
        SliceTrackingDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        SliceTrackingSearchRequest.BClass.INSTANCE,
        SliceTrackingCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<SubscriberConfigDTO, FullTrackingWithVersion> SUBSCRIBER_CONFIG_VIEW_MODEL = new CrudViewModel<SubscriberConfigDTO, FullTrackingWithVersion>(
        SubscriberConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        SubscriberConfigSearchRequest.BClass.INSTANCE,
        SubscriberConfigCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<ListenerConfigDTO, FullTrackingWithVersion> LISTENER_CONFIG_VIEW_MODEL = new CrudViewModel<ListenerConfigDTO, FullTrackingWithVersion>(
        ListenerConfigDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        ListenerConfigSearchRequest.BClass.INSTANCE,
        ListenerConfigCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<BucketCounterDTO, FullTrackingWithVersion> BUCKET_COUNTER_VIEW_MODEL = new CrudViewModel<BucketCounterDTO, FullTrackingWithVersion>(
        BucketCounterDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        BucketCounterSearchRequest.BClass.INSTANCE,
        BucketCounterCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<BucketEntryDTO, BucketTracking> BUCKET_ENTRY_VIEW_MODEL = new CrudViewModel<BucketEntryDTO, BucketTracking>(
        BucketEntryDTO.BClass.INSTANCE,
        BucketTracking.BClass.INSTANCE,
        BucketEntrySearchRequest.BClass.INSTANCE,
        BucketEntryCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<ProcessStatusDTO, NoTracking> PROCESS_STATUS_VIEW_MODEL = new CrudViewModel<ProcessStatusDTO, NoTracking>(
        ProcessStatusDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        ProcessStatusSearchRequest.BClass.INSTANCE,
        null);
    public static final CrudViewModel<ComponentInfoDTO, NoTracking> COMPONENT_INFO_VIEW_MODEL = new CrudViewModel<ComponentInfoDTO, NoTracking>(
        ComponentInfoDTO.BClass.INSTANCE,
        NoTracking.BClass.INSTANCE,
        ComponentInfoSearchRequest.BClass.INSTANCE,
        null);
    static {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("statistics",       STATISTICS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("componentInfo",    COMPONENT_INFO_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("processStatus",    PROCESS_STATUS_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("cannedRequest",    CANNED_REQUEST_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("sliceTracking",    SLICE_TRACKING_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("subscriberConfig", SUBSCRIBER_CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("listenerConfig",   LISTENER_CONFIG_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("bucketCounter",    BUCKET_COUNTER_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("bucketEntry",      BUCKET_ENTRY_VIEW_MODEL);
    }
}
