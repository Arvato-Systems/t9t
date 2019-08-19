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
package com.arvatosystems.t9t.misc.extensions

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.crud.CrudModuleCfgResponse
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.batch.SliceTrackingDTO
import com.arvatosystems.t9t.batch.request.SliceTrackingCrudRequest
import com.arvatosystems.t9t.core.CannedRequestDTO
import com.arvatosystems.t9t.core.request.CannedRequestCrudRequest
import com.arvatosystems.t9t.email.EmailModuleCfgDTO
import com.arvatosystems.t9t.email.request.EmailModuleCfgCrudRequest
import com.arvatosystems.t9t.event.SubscriberConfigDTO
import com.arvatosystems.t9t.event.request.SubscriberConfigCrudRequest
import com.arvatosystems.t9t.genconf.ConfigDTO
import com.arvatosystems.t9t.genconf.request.ConfigCrudRequest
import com.arvatosystems.t9t.io.CsvConfigurationDTO
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.request.CsvConfigurationCrudRequest
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest
import com.arvatosystems.t9t.rep.ReportConfigDTO
import com.arvatosystems.t9t.rep.ReportParamsDTO
import com.arvatosystems.t9t.rep.request.ReportConfigCrudRequest
import com.arvatosystems.t9t.rep.request.ReportParamsCrudRequest
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO
import com.arvatosystems.t9t.ssm.request.SchedulerSetupCrudRequest
import de.jpaw.bonaparte.pojos.api.OperationType

class MiscExtensionsCreate {

    // extension methods for the types with surrogate keys
    def static CrudSurrogateKeyResponse<DataSinkDTO, FullTrackingWithVersion> create(DataSinkDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new DataSinkCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CsvConfigurationDTO, FullTrackingWithVersion> create(CsvConfigurationDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CsvConfigurationCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> create(SchedulerSetupDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SchedulerSetupDTO, FullTrackingWithVersion> createReducedResponse(SchedulerSetupDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SchedulerSetupCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
            suppressResponseParameters = true
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CannedRequestDTO, FullTrackingWithVersion> create(CannedRequestDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CannedRequestCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<CannedRequestDTO, FullTrackingWithVersion> createReducedResponse(CannedRequestDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new CannedRequestCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
            suppressResponseParameters = true
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<SliceTrackingDTO, FullTrackingWithVersion> create(SliceTrackingDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SliceTrackingCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ConfigDTO, FullTrackingWithVersion> create(ConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ConfigCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ReportConfigDTO, FullTrackingWithVersion> create(ReportConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ReportConfigCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudSurrogateKeyResponse<ReportParamsDTO, FullTrackingWithVersion> create(ReportParamsDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new ReportParamsCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
    def static CrudModuleCfgResponse<EmailModuleCfgDTO> create(EmailModuleCfgDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new EmailModuleCfgCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudModuleCfgResponse)
    }
    def static CrudSurrogateKeyResponse<SubscriberConfigDTO, FullTrackingWithVersion> create(SubscriberConfigDTO dto, ITestConnection dlg) {
        dto.validate
        return dlg.typeIO(new SubscriberConfigCrudRequest => [
            crud            = OperationType.CREATE
            data            = dto
        ], CrudSurrogateKeyResponse)
    }
}
