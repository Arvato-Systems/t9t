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
/**
 *
 */
package com.arvatosystems.t9t.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.ContentTypes;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.request.ExecuteCannedRequest;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.DataSinkTestRequest;
import com.arvatosystems.t9t.io.request.FileDownloadRequest;
import com.arvatosystems.t9t.io.request.FileDownloadResponse;
import com.arvatosystems.t9t.io.request.FileUploadRequest;
import com.arvatosystems.t9t.rep.ReportParamsRef;
import com.arvatosystems.t9t.rep.request.RunReportRequest;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;


/**
 * @author NGTZ001
 *
 */
@Singleton
public class T9TMessagingDAO implements IT9TMessagingDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9TMessagingDAO.class);
    private final T9TRemoteUtils t9tRemoteUtils = Jdp.getRequired(T9TRemoteUtils.class);

    /*
     * (non-Javadoc)
     * @see com.arvatosystems.t9t.tfi.services.IMessagingDAO#dataSinkTestRequest(com.arvatosystems.t9t.tfi.base.messaging.DataSink, int)
     */
    @Override
    public Long dataSinkTestRequest(DataSinkDTO dataSink, int numDataRecords) throws ReturnCodeException {
        try {

            DataSinkTestRequest dataSinkTestRequest = new DataSinkTestRequest();
            dataSinkTestRequest.setNumDataRecords(numDataRecords);
            dataSinkTestRequest.setDataSinkId(dataSink.getDataSinkId());

            ServiceResponse serviceResponse = t9tRemoteUtils.executeAndHandle(dataSinkTestRequest, ServiceResponse.class);

            return findSinkRefInReponse(serviceResponse);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("messaging-crud.bon#DataSinkTestRequest", e);
            return null; // just for the compiler
        }

    }

    @Override
    public SinkCreatedResponse fileUploadRequest(OutputSessionParameters parameters, ByteArray data) throws ReturnCodeException {
        try {

            FileUploadRequest dataSinkTestRequest = new FileUploadRequest();
            dataSinkTestRequest.setParameters(parameters);
            dataSinkTestRequest.setData(data);

            SinkCreatedResponse serviceResponse = t9tRemoteUtils.executeAndHandle(dataSinkTestRequest, SinkCreatedResponse.class);

            return serviceResponse;
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("messaging-crud.bon#fileUploadRequest", e);
            return null; // just for the compiler
        }
    }
    /*
     * (non-Javadoc)
     * @see com.arvatosystems.t9t.tfi.services.IMessagingDAO#downloadFileRequest(com.arvatosystems.t9t.tfi.base.messaging.SinkRef,
     * java.lang.Integer)
     */
    @Override
    public Media downloadFileRequest(Long sinkRef) throws ReturnCodeException {

        try {
            // the offset starts always with 0
            long offset = 0;
            // set given chunkSizeInBytes as limit or default 1 MB
            int limit = 1024*1024;
            // is needed for the loop breakup
            boolean hasMore = true;

            String name = null;
            String format = null;
            String ctype = null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            while (hasMore) {
                FileDownloadResponse fileDownloadResponse = execFileDownloadRequest(sinkRef, offset, limit);

                // Collect meta data about the file
                if (name == null) {
                    SinkDTO sink = fileDownloadResponse.getSink();
                    name = new File(sink.getFileOrQueueName()).getName();
                    int j = name.lastIndexOf('.');
                    if (j >= 0) {
                        format = name.substring(j + 1);
                        ctype = ContentTypes.getContentType(format);
                    }
                    LOGGER.debug("Download: file-name:{}, format:{}, content-type:{}", name, format, ctype);
                }

                // Get the data
                byte[] data = fileDownloadResponse.getData().getBytes();
                hasMore = fileDownloadResponse.getHasMore();
                LOGGER.debug("Download: Chunk/content-length:{}. Has-more-chunks:{} (offset:{}/limit:{})", data.length, hasMore, offset, limit);

                // Collect data
                outputStream.write(data);

                // calculate new offset and limit --> if hasMore == false the calculation has no effect
                offset = offset + limit;
            }

            // Provide data for Media (ZK related stuff)
            return new AMedia(name, format, ctype, new ByteArrayInputStream(outputStream.toByteArray()));

        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("messaging.bon#FileDownloadRequest", e);
            return null; // just for the compiler
        }

    }

    /** Run an arbitrary request which returns a FileDownloadResponse. */
    @Override
    public void downloadFileAndSave(RequestParameters rp) {
        FileDownloadResponse fileDownloadResponse = t9tRemoteUtils.executeExpectOk(rp, FileDownloadResponse.class);
        SinkDTO sink = fileDownloadResponse.getSink();
        String name = new File(sink.getFileOrQueueName()).getName();
        String ctype = "text/plain";
        String format = "txt";
        int j = name.lastIndexOf('.');
        if (j >= 0) {
            format = name.substring(j + 1);
            ctype = ContentTypes.getContentType(format);
        }
        LOGGER.debug("Download: file-name:{}, format:{}, content-type:{}", name, format, ctype);
        // Provide data for Media (ZK related stuff)
        Media media = new AMedia(name, format, ctype, new ByteArrayInputStream(fileDownloadResponse.getData().getBytes()));
        Filedownload.save(media);
    }

    /*
     * (non-Javadoc)
     * @see com.arvatosystems.t9t.tfi.services.IMessagingDAO#retrieveSink(java.lang.Long)
     */
    @Override
    public SinkDTO retrieveSink(Long sinkObjectRef) throws ReturnCodeException {
        FileDownloadResponse fileDownloadResponse = execFileDownloadRequest(sinkObjectRef, 0, 0);
        return fileDownloadResponse.getSink();
    }


    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    // private helpers
    // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    private FileDownloadResponse execFileDownloadRequest(Long sinkObjectRef, long offset, int limit) throws ReturnCodeException {
        FileDownloadRequest fileDownloadRequest = new FileDownloadRequest();
        fileDownloadRequest.setSinkRef(sinkObjectRef);
        fileDownloadRequest.setOffset(offset);
        fileDownloadRequest.setLimit(limit);
        FileDownloadResponse fileDownloadResponse = t9tRemoteUtils.executeAndHandle(fileDownloadRequest, FileDownloadResponse.class);
        return fileDownloadResponse;
    }

    protected Long findSinkRefInReponse(ServiceResponse serviceResponse) {
        Long sinkRef = null;
        if (serviceResponse instanceof SinkCreatedResponse) {
            sinkRef = ((SinkCreatedResponse) serviceResponse).getSinkRef();
            LOGGER.debug("Response is instanceof {} - returning sinkObjectRef={}", SinkCreatedResponse.class.getSimpleName(), sinkRef);
            return sinkRef;
        } else if (serviceResponse instanceof ReadAllResponse) {
            sinkRef = ((ReadAllResponse<?, ?>) serviceResponse).getSinkRef();
            LOGGER.debug("Response is instanceof {} - returning sinkObjectRef={}", ReadAllResponse.class.getSimpleName(), sinkRef);
            return sinkRef;
        }
        LOGGER.debug("Response is NOT instanceof SinkCreatedResponse or ReadAllResponse. It is {} - returning NULL", serviceResponse.getClass().getSimpleName());
        return null;
    }

    @Override
    public void rerunRequest(Long referencedRequestRef) throws ReturnCodeException {
        // TODO Auto-generated method stub

    }


    @Override
    public Long runReportRequest(ReportParamsRef paramsRef) throws ReturnCodeException {
        try {
            RunReportRequest runReportRequest = new RunReportRequest();
            runReportRequest.setReportParamsRef(paramsRef);
            SinkCreatedResponse sinkCreatedResponse = t9tRemoteUtils.executeAndHandle(runReportRequest, SinkCreatedResponse.class);
            return sinkCreatedResponse.getSinkRef();
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("rep.bon#RunReportRequest - (base.)messaging.bon#SinkCreatedResponse", e);
            return null;
        }
    }

    @Override
    public void downloadFileAndSave(Long sinkRef) throws ReturnCodeException {
        Media media = downloadFileRequest(sinkRef);
        if (media != null) {
            // SEND "remote" file to browser
            Filedownload.save(media);
        }
    }

    @Override
    public MediaData getUploadedData(UploadEvent ev) throws IOException {
        final Media m = ev.getMedia();
        final String filename = m.getName();
        LOGGER.debug("uploadMediaData: got the media, CT = {}, format = {}, name = {}", m.getContentType(), m.getFormat(), filename);

        MediaTypeDescriptor mtd = MediaTypeInfo.getFormatByMimeType(m.getContentType());
        final MediaData md = new MediaData();
        if (m.isBinary()) {
            md.setMediaType(mtd == null ? MediaXType.of(MediaType.RAW) : mtd.getMediaType());
            if (!m.inMemory())
                md.setRawData(ByteArray.fromInputStream(m.getStreamData(), -1));  // mp3s cause exceptions with getByteData
            else
                md.setRawData(new ByteArray(m.getByteData()));
        } else {
            md.setMediaType(mtd == null ? MediaXType.of(MediaType.TEXT) : mtd.getMediaType());
            md.setText(m.getStringData());
        }
        if (filename != null) {
            Map<String, Object> meta = new HashMap<String, Object>(4);
            meta.put("fileName", filename);
            md.setZ(meta);
        }
        return md;
    }

    @Override
    public Long executeCannedRequest(CannedRequestRef cannedRequestRef) throws ReturnCodeException {
        LOGGER.debug("executeCannedRequest with ref {}", cannedRequestRef);

        try {
            ExecuteCannedRequest executeCannedRequest = new ExecuteCannedRequest();;
            executeCannedRequest.setRequestRef(cannedRequestRef);
            t9tRemoteUtils.executeAndHandle(executeCannedRequest, ServiceResponse.class);
        } catch (Exception e) {
            t9tRemoteUtils.returnCodeExceptionHandler("executeCannedRequest", e);
            return null; // just for the compiler
        }
        return null;
    }
}
