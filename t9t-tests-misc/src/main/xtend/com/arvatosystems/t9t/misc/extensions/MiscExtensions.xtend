/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.misc.extensions

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.api.RequestParameters
import com.arvatosystems.t9t.base.crud.CrudModuleCfgResponse
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.batch.SliceTrackingDTO
import com.arvatosystems.t9t.batch.SliceTrackingKey
import com.arvatosystems.t9t.batch.request.SliceTrackingCrudRequest
import com.arvatosystems.t9t.bucket.BucketCounterDTO
import com.arvatosystems.t9t.bucket.BucketCounterKey
import com.arvatosystems.t9t.bucket.request.BucketCounterCrudRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.CannedRequestKey
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest
import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.request.EmailModuleCfgCrudRequest
import com.arvatosystems.t9t.event.ListenerConfigDTO
import com.arvatosystems.t9t.event.ListenerConfigKey
import com.arvatosystems.t9t.event.SubscriberConfigDTO
import com.arvatosystems.t9t.event.SubscriberConfigKey
import com.arvatosystems.t9t.event.request.ListenerConfigCrudRequest
import com.arvatosystems.t9t.event.request.SubscriberConfigCrudRequest
import com.arvatosystems.t9t.genconf.ConfigDTO
import com.arvatosystems.t9t.genconf.ConfigKey
import com.arvatosystems.t9t.genconf.request.ConfigCrudRequest
import com.arvatosystems.t9t.io.AsyncChannelDTO
import com.arvatosystems.t9t.io.AsyncChannelKey
import com.arvatosystems.t9t.io.AsyncQueueDTO
import com.arvatosystems.t9t.io.AsyncQueueKey
import com.arvatosystems.t9t.io.CsvConfigurationDTO
import com.arvatosystems.t9t.io.CsvConfigurationKey
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.DataSinkKey
import com.arvatosystems.t9t.io.request.AsyncChannelCrudRequest
import com.arvatosystems.t9t.io.request.AsyncQueueCrudRequest
import com.arvatosystems.t9t.io.request.CsvConfigurationCrudRequest
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest
import com.arvatosystems.t9t.plugins.LoadedPluginDTO
import com.arvatosystems.t9t.plugins.LoadedPluginKey
import com.arvatosystems.t9t.plugins.request.LoadedPluginCrudRequest
import com.arvatosystems.t9t.rep.ReportConfigDTO
import com.arvatosystems.t9t.rep.ReportConfigKey
import com.arvatosystems.t9t.rep.ReportParamsDTO
import com.arvatosystems.t9t.rep.ReportParamsKey
import com.arvatosystems.t9t.rep.request.ReportConfigCrudRequest
import com.arvatosystems.t9t.rep.request.ReportParamsCrudRequest
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.SchedulerSetupKey
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigDTO
import com.arvatosystems.t9t.uiprefsv3.LeanGridConfigKey
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigCrudRequest
import de.jpaw.bonaparte.core.MapComposer
import de.jpaw.bonaparte.pojos.api.OperationType

class MiscExtensions {

    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<DataSinkDTO, FullTrackingWithVersion> merge(DataSinkDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DataSinkCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new DataSinkKey(dto.dataSinkId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CsvConfigurationDTO, FullTrackingWithVersion> merge(CsvConfigurationDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CsvConfigurationCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new CsvConfigurationKey(dto.csvConfigurationId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<AsyncQueueDTO, FullTrackingWithVersion> merge(AsyncQueueDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new AsyncQueueCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new AsyncQueueKey(dto.asyncQueueId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<AsyncChannelDTO, FullTrackingWithVersion> merge(AsyncChannelDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new AsyncChannelCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new AsyncChannelKey(dto.asyncChannelId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> merge(SchedulerSetupDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new SchedulerSetupKey(dto.schedulerId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> mergeReducedResponse(SchedulerSetupDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new SchedulerSetupKey(dto.schedulerId)
            suppressResponseParameters = true
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CannedRequestDTO, FullTrackingWithVersion> merge(CannedRequestDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CannedRequestCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new CannedRequestKey(dto.requestId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CannedRequestDTO, FullTrackingWithVersion> mergeReducedResponse(CannedRequestDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CannedRequestCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new CannedRequestKey(dto.requestId)
            suppressResponseParameters = true
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SliceTrackingDTO, FullTrackingWithVersion> merge(SliceTrackingDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SliceTrackingCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new SliceTrackingKey(dto.dataSinkId, dto.id)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<LeanGridConfigDTO, FullTrackingWithVersion> merge(LeanGridConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new LeanGridConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new LeanGridConfigKey(dto.gridId, dto.variant, dto.userRef)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ConfigDTO, FullTrackingWithVersion> merge(ConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ConfigKey(dto.configGroup, dto.configKey, dto.genericRef1, dto.genericRef2)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ReportConfigDTO, FullTrackingWithVersion> merge(ReportConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ReportConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ReportConfigKey(dto.reportConfigId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ReportParamsDTO, FullTrackingWithVersion> merge(ReportParamsDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ReportParamsCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ReportParamsKey(dto.reportParamsId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudModuleCfgResponse<EmailModuleCfgDTO> merge(EmailModuleCfgDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new EmailModuleCfgCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
        ], CrudModuleCfgResponse)
    }
    def static CrudSurrogateKeyResponse<SubscriberConfigDTO, FullTrackingWithVersion> merge(SubscriberConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SubscriberConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new SubscriberConfigKey(dto.eventID, dto.handlerClassName)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ListenerConfigDTO, FullTrackingWithVersion> merge(ListenerConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ListenerConfigCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new ListenerConfigKey(dto.classification)
        ], CrudSurrogateKeyResponse)
    }

    def static CrudSurrogateKeyResponse<BucketCounterDTO, FullTrackingWithVersion> merge(BucketCounterDTO dto, ITestConnection dlg) {
        dto.validate
        dlg.typeIO(new BucketCounterCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new BucketCounterKey(dto.qualifier)
        ], CrudSurrogateKeyResponse)
    }

    /** Create a canned request, and return its objectRef */
    def static Long createCannedRequestWithParameters(ITestConnection dlg, String id, String description, RequestParameters params) {
        params.validate
        val rq = new CannedRequestDTO => [
            requestId            = id
            it.name              = description
            jobRequestObjectName = params.ret$PQON
            request              = params
            jobParameters        = MapComposer.marshal(params, false, true)
        ]
        return (rq.mergeReducedResponse(dlg)).key
    }

    // all methods for plugins */
    def static CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> merge(LoadedPluginDTO dto, ITestConnection dlg) {
        dto.validate
        dlg.typeIO(new LoadedPluginCrudRequest => [
            crud            = OperationType.MERGE
            data            = dto
            naturalKey      = new LoadedPluginKey(dto.pluginId)
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> update(LoadedPluginDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new LoadedPluginCrudRequest => [
            crud            = OperationType.UPDATE
            data            = dto
            key             = dto.objectRef
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> delete(LoadedPluginDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new LoadedPluginCrudRequest => [
            crud            = OperationType.DELETE
            data            = dto
            key             = dto.objectRef
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> create(LoadedPluginDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new LoadedPluginCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
}
