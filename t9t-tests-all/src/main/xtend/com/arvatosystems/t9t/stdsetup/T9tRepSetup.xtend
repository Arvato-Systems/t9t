/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.stdsetup

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkCategoryType
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest
import com.arvatosystems.t9t.io.request.DataSinkSearchRequest
import com.arvatosystems.t9t.io.request.FileUploadRequest
import com.arvatosystems.t9t.rep.ReportClassificationType
import com.arvatosystems.t9t.rep.ReportConfigDTO
import com.arvatosystems.t9t.rep.ReportIntervalCategoryType
import com.arvatosystems.t9t.rep.ReportIntervalType
import com.arvatosystems.t9t.rep.ReportParamsDTO
import com.arvatosystems.t9t.rep.request.ReportConfigSearchRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.NotFilter
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS
import de.jpaw.util.ByteArray
import java.util.HashMap
import java.util.List
import org.eclipse.xtend.lib.annotations.Data
import java.time.LocalDateTime

import static extension com.arvatosystems.t9t.misc.extensions.MiscExtensions.*
import org.apache.commons.io.IOUtils
import de.jpaw.bonaparte.pojos.api.UnicodeFilter

/** Class which offers methods to create configurations for all reports.
 *
 */
@AddLogger
@Data
class T9tRepSetup {
    ITestConnection dlg
    boolean setupForGeneralTenant

    public static final String OUTPUT_DATASINK_ID = "RepStdT9t"

    /** main external entry */
    public def void setupAllReports() {
        setupReportDataSinks
        setupReportConfigs
        createReportParamsForAllReportConfigs
    }

    public def void setupReportDataSinks() {
        // first, create a data sink for the reports.
        new DataSinkDTO => [
            dataSinkId                  = OUTPUT_DATASINK_ID
            commFormatType              = MediaType.UNDEFINED
            isActive                    = true
            isInput                     = false
            commTargetChannelType       = CommunicationTargetChannelType.FILE
            fileOrQueueNamePattern      = "Reports/${now|yyyyMMddHHmmss}_${reportConfigId}_${tenantId}_${userId}.${fileExt}"
            category                    = DataSinkCategoryType.REPORT
            createOrUpdateDatasink(dlg)
        ]

        new DataSinkDTO => [
            dataSinkId              = 'reportSrc'
            description             = "Storage of reports src"
            isActive                = true
            isInput                 = false
            commTargetChannelType   = CommunicationTargetChannelType.FILE
            commFormatType          = MediaType.RAW
            fileOrQueueNamePattern  = 'reports/src/${localFilename}'
            category                = DataSinkCategoryType.MASTER_DATA
            createOrUpdateDatasink(dlg)
        ]
    }

    def void setupReportConfigs() {

        new ReportConfigDTO => [
            reportConfigId              = "ErrorLogDetails"
            name                        = "Detailed error log report"
            isActive                    = true
            jasperReportTemplateName    = "ErrorLogDetails.jrxml"
            description                 = "Detailed error log report, by date"
            classificationType          = ReportClassificationType.MONITORING
            intervalRequired            = true
            merge(dlg)
        ]
        new ReportConfigDTO => [
            reportConfigId              = "ErrorLogSummary"
            name                        = "Return code counts"
            isActive                    = true
            jasperReportTemplateName    = "ErrorLogSummary.jrxml"
            description                 = "Summary error log report, by date"
            classificationType          = ReportClassificationType.MONITORING
            intervalRequired            = true
            merge(dlg)
        ]
        new ReportConfigDTO => [
            reportConfigId              = "TransactionTime"
            name                        = "Processing time report"
            isActive                    = true
            jasperReportTemplateName    = "TransactionTime.jrxml"
            description                 = "Processing time report, by date"
            classificationType          = ReportClassificationType.MONITORING
            intervalRequired            = true
            merge(dlg)
        ]
        new ReportConfigDTO => [
            reportConfigId              = "FailedTransFlex"
            name                        = "Failed Transactions (flex)"
            isActive                    = true
            jasperReportTemplateName    = "FailedTransactionsFlex.jrxml"
            description                 = "Failed Transactions (with flexbile parameters)"
            classificationType          = ReportClassificationType.OPERATION
            intervalRequired            = true
            merge(dlg)
        ]
    }


    public def void createReportParamsForAllReportConfigs() {
        createReportParamsForReportConfig("ErrorLogDetails")
        createReportParamsForReportConfig("ErrorLogSummary")
        createReportParamsForReportConfig("TransactionTime")
    }

    public def void createReportParamsForReportConfig(String reportConfigId) {
        val configSearchReq = new ReportConfigSearchRequest => [
            searchFilter = new AsciiFilter("reportConfigId", reportConfigId, null, null, null, null)
        ]
        val reportConfigs = ((dlg.doIO(configSearchReq) as ReadAllResponse<ReportConfigDTO, FullTrackingWithVersion>).dataList as List<DataWithTrackingS<ReportConfigDTO, FullTrackingWithVersion>>).map[data]

        reportConfigs.forEach[
            createReportParams(it)
        ]
    }

    public def void createReportParams(ReportConfigDTO repConf) {
        new ReportParamsDTO => [
            isActive = true
            reportParamsId = repConf.reportConfigId
            reportConfigRef = repConf
            outputFileType = MediaType.XLS
            dataSinkId = OUTPUT_DATASINK_ID
            intervalCategory = ReportIntervalCategoryType.BY_RANGE
            interval = ReportIntervalType.DAILY
            fromDate = LocalDateTime.now.minusYears(10)
            toDate = LocalDateTime.now.plusYears(10)
            merge(dlg)
        ]
    }

    public def FileUploadRequest createFileUploadRequest(String fileNamePrefix, String filename) {
        val fileInput = typeof(T9tRepSetup).getResourceAsStream(fileNamePrefix+filename)

        if (fileInput === null) throw new IllegalArgumentException("Report not found: " + fileNamePrefix+filename);

        val fileData = IOUtils.toByteArray(fileInput)
        fileInput.close


        new FileUploadRequest => [
            parameters = new OutputSessionParameters => [
                dataSinkId = "reportSrc"
                additionalParameters = new HashMap<String, Object>()
                additionalParameters.put("localFilename", filename)
                communicationFormatType = MediaXType.forName("RAW")
            ]
            data = new ByteArray(fileData)
            dlg.okIO(it)
        ]
    }

    /*
     * Helper to update datasink if several datasinks with the same datasinkId exist for different tenants
     */
    public def void createOrUpdateDatasink(DataSinkDTO data, ITestConnection dlg) {
        val dataSinkSearchReq = new DataSinkSearchRequest
        if (setupForGeneralTenant) {
            dataSinkSearchReq.searchFilter = new AndFilter => [
                filter1 = new AsciiFilter("dataSinkId", data.dataSinkId, null, null, null, null)
                filter2 = new UnicodeFilter("tenantId", T9tConstants.GLOBAL_TENANT_ID, null, null, null, null)
            ]
        } else {
            dataSinkSearchReq.searchFilter = new AndFilter => [
                filter1 = new AsciiFilter("dataSinkId", data.dataSinkId, null, null, null, null)
                filter2 = new UnicodeFilter("tenantId", T9tConstants.GLOBAL_TENANT_ID, null, null, null, null)
            ]
        }

        val dataSinkGeneralTenantReqRes = ((dlg.doIO(dataSinkSearchReq) as ReadAllResponse<DataSinkDTO, FullTrackingWithVersion>).dataList as List<DataWithTrackingS<DataSinkDTO, FullTrackingWithVersion>>)

        val crudReq = new DataSinkCrudRequest => [
            crud = OperationType.UPDATE
            it.data = data
        ]

        if (dataSinkGeneralTenantReqRes.size == 1) {
            crudReq.crud = OperationType.UPDATE
            crudReq.key = dataSinkGeneralTenantReqRes.get(0).data.objectRef
        } else if (dataSinkGeneralTenantReqRes.size == 0) {
             crudReq.crud = OperationType.CREATE
        }
        dlg.okIO(crudReq)
    }
}
