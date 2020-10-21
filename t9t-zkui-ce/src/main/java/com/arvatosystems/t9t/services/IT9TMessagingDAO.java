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
package com.arvatosystems.t9t.services;

import java.io.IOException;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.FileDownloadRequest;
import com.arvatosystems.t9t.io.request.FileDownloadResponse;
import com.arvatosystems.t9t.rep.ReportParamsRef;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.util.ByteArray;



public interface IT9TMessagingDAO {

    /**
     * Invocation allows to output a simple header/footer document with no data items included.
     * In some cases it will be an empty file, but still useful to see generated filenames or test connectivity.
     * Intended to be used from the configuration UI, but also for remote tests.
     * @param dataSink
     * @param numDataRecords
     * @throws ReturnCodeException
     */
    public Long dataSinkTestRequest(DataSinkDTO dataSink, int numDataRecords) throws ReturnCodeException;

    /**
     * Prepare a ZK Media object that is needed for download content. This media object can be used with {@link Filedownload#save(Media)}.<br>
     * Used Request: {@link FileDownloadRequest}<br>
     * The meta data about the file (name, content-type and format) will collect from the {@link FileDownloadResponse}<br>
     * If more chunks of data are available, it will be handled internally to retrieve these.
     * @param sinkRef The reference for the download
     * @param chunkSizeInBytes Set the chunk size of the internal re-read mechanism
     * @return the filled Media object.
     * @throws ReturnCodeException
     */
    public Media downloadFileRequest(Long sinkRef) throws ReturnCodeException;

    /** entry used by the export button of grid28. */
    public void downloadFileAndSave(Long sinkRef) throws ReturnCodeException;
    public void downloadFileAndSave(RequestParameters rp);

    /**
     * Get from back-end the full Sink object by giving only the reference
     * @param sinkObjectRef Sink objectRef
     * @return Full Sink object
     * @throws ReturnCodeException
     */
    public SinkDTO retrieveSink(Long sinkObjectRef) throws ReturnCodeException;


    /**
     * Rerun a single Request
     * @param referencedRequestRef the cProcessRef of the request to be rerun.
     * @throws ReturnCodeException
     */
    public void rerunRequest(Long referencedRequestRef) throws ReturnCodeException;


    /**
     * The file upload request allows to create a sink entry for given upload data. It returns a sink reference in case of success.
     */
    public SinkCreatedResponse fileUploadRequest(OutputSessionParameters parameters, ByteArray data) throws ReturnCodeException;

    /**
     * Just run the RunReportRequest. The request will be filled with ReportParamsRef.<br>
     * The response will be the sink-objectRef {@link SinkCreatedResponse#getSinkRef()}
     * @param paramsRef
     * @return sinkObjectRef
     * @throws ReturnCodeException
     */
    public Long runReportRequest(ReportParamsRef paramsRef) throws ReturnCodeException;

    public MediaData getUploadedData(UploadEvent ev) throws IOException;

    public Long executeCannedRequest(CannedRequestRef ref) throws ReturnCodeException;
}
