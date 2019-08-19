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
package com.arvatosystems.t9t.misc.io

import com.arvatosystems.t9t.base.ITestConnection
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.entities.FullTracking
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.io.SinkDTO
import com.arvatosystems.t9t.io.request.ImportFromString
import com.arvatosystems.t9t.io.request.ImportInputSessionRequest
import com.arvatosystems.t9t.io.request.SinkSearchRequest
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaDataUtil
import de.jpaw.bonaparte.pojos.api.SortColumn
import de.jpaw.bonaparte.pojos.api.UnicodeFilter
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW
import java.util.List
import java.util.UUID
import com.arvatosystems.t9t.io.T9tIOException

@AddLogger
class T9tImportTools {
    /**
     * Imports a resource as text file. The path must be relative (no leading /).
     * Checks for no errors and returns the number of imported records.
     */
    def static Integer importTextResource(ITestConnection connection, UUID myApiKey, String myDataSinkId, String path) {
        importTextResourceNoCheck(connection, myApiKey, myDataSinkId, path)
        return checkLastImport(connection, myDataSinkId, path)
    }

    /** Imports a resource as text file. The path must be relative (no leading /). */
    def static void importTextResourceNoCheck(ITestConnection connection, UUID myApiKey, String myDataSinkId, String path) {
        val inputData = MediaDataUtil.getTextResource(path)
        if (inputData === null)
            throw new T9tException(T9tException.FILE_NOT_FOUND_FOR_DOWNLOAD, path)
        connection.okIO(new ImportInputSessionRequest => [
            dataSinkId = myDataSinkId
            apiKey     = myApiKey
            sourceName = path
            data = new ImportFromString(inputData)
        ])
    }

    /**
     * Imports a resource as text file. The path must be relative (no leading /).
     * Checks for no errors and returns the number of imported records.
     */
    def static Integer importFromString(ITestConnection connection, UUID myApiKey, String myDataSinkId, String inputData, String source) {
        importFromStringNoCheck(connection, myApiKey, myDataSinkId, inputData, source)
        return checkLastImport(connection, myDataSinkId, source)
    }

    /** Imports a resource as text file. The path must be relative (no leading /). */
    def static void importFromStringNoCheck(ITestConnection connection, UUID myApiKey, String myDataSinkId, String inputData, String source) {
        connection.okIO(new ImportInputSessionRequest => [
            dataSinkId = myDataSinkId
            apiKey     = myApiKey
            sourceName = source
            data = new ImportFromString(inputData)
        ])
    }

    /** Retrieves the summary record of the most recent import and check for errors. Throws an exception if errors found, otherwise returns the number of records read. */
    def static Integer checkLastImport(ITestConnection connection, String dataSinkId, String filename) {
        val searchRq = new SinkSearchRequest => [
            searchFilter  = new UnicodeFilter("dataSink.dataSinkId") => [ equalsValue = dataSinkId ]
            sortColumns   = #[ new SortColumn("objectRef", true) ]
        ]
        val resp = connection.typeIO(searchRq, ReadAllResponse).dataList as List<DataWithTrackingW<SinkDTO, FullTracking>>;
        if (resp.isEmpty)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "No SinkDTO found for the given data sink " + dataSinkId)
        val sink = resp.get(0).data
        if (filename !== null && filename != sink.fileOrQueueName)
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "Most recent SinkDTO of " + dataSinkId
                 + " does not have expected filename " + filename + ", but " + sink.fileOrQueueName)
        LOGGER.debug("Found sink entry for {} with {} source records, {} mapped records, {} in error",
            dataSinkId, sink.numberOfSourceRecords, sink.numberOfMappedRecords, sink.numberOfErrorRecords
        )
        if (Integer.valueOf(0) != sink.numberOfErrorRecords)
            throw new T9tException(T9tIOException.IMPORT_FINISHED_WITH_ERRORS, "Data sink ID: " + dataSinkId + ": " + sink.numberOfErrorRecords + " error records")
        return sink.numberOfSourceRecords
    }
}
