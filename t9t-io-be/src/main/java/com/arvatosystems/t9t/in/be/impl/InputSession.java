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
package com.arvatosystems.t9t.in.be.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IInputQueuePartitioner;
import com.arvatosystems.t9t.base.IStatefulServiceSession;
import com.arvatosystems.t9t.base.StringTrimmer;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.auth.ApiKeyAuthentication;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.request.ErrorRequest;
import com.arvatosystems.t9t.base.types.SessionParameters;
import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.in.services.IInputFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.DataSinkKey;
import com.arvatosystems.t9t.io.DataSinkRef;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedRequest;
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedResponse;
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest;
import com.arvatosystems.t9t.io.request.ImportStatusResponse;
import com.arvatosystems.t9t.io.request.StoreSinkRequest;
import com.arvatosystems.t9t.io.request.WriteRecordsToDataSinkRequest;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableClass;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.DataConverter;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.meta.AlphanumericElementaryDataItem;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

//this class operates outside of a RequestContext!
@Dependent
public class InputSession implements IInputSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputSession.class);
    private static final DataConverter<String, AlphanumericElementaryDataItem> STRING_TRIMMER = new StringTrimmer();
    private static final int MAX_RESPONSES = 1000; // maximum number of responses which are buffered
    private static final String WORKER_THREAD_NAME_PREFIX = "t9t-InputSessionWorker-";

    private final IStatefulServiceSession session = Jdp.getRequired(IStatefulServiceSession.class); // holds the backend connection
    protected final AtomicInteger numSource = new AtomicInteger(); // unmapped records
    protected final AtomicInteger numProcessed = new AtomicInteger(); // mapped records
    protected final AtomicInteger numError = new AtomicInteger(); // errors
    protected final Instant start = Instant.now();
    protected final SinkDTO sinkDTO = new SinkDTO();
    protected DataSinkDTO dataSinkCfg;
    protected BonaPortableClass<?> baseBClass;
    protected IInputDataTransformer<BonaPortable> inputTransformer;
    protected IInputFormatConverter inputFormatConverter;
    protected final Map<String, Object> headerData = new HashMap<String, Object>();
    protected String sourceReference = null;
    protected boolean isDuplicateImport = false;
    protected boolean isProcessingError = false;
    protected List<BonaPortable> responseBuffer = null;
    protected IInputQueuePartitioner inputProcessingSplitter;
    protected boolean isPooledProcessing = false;
    protected Integer inputProcessingParallel;
    protected List<ExecutorService> workerThreadExecutors;
    protected List<Future<ServiceResponse>> pooledProcessingFutures = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public DataSinkDTO open(final String dataSourceId, final UUID apiKey, final String sourceURI, final Map<String, Object> params) {
        LOGGER.info("Opening input session for dataSource ID {}, source URI {}", dataSourceId, sourceURI);

        // open a session
        sourceReference = "DS=" + dataSourceId + ", filename=" + (sourceURI == null ? "-" : sourceURI);
        final SessionParameters sessionParameters = new SessionParameters();
        sessionParameters.setDataUri(sourceURI);
        session.open(sessionParameters, new ApiKeyAuthentication(apiKey));

        final DataSinkCrudRequest dataSinkReadRq = new DataSinkCrudRequest();
        dataSinkReadRq.setCrud(OperationType.READ);
        dataSinkReadRq.setNaturalKey(new DataSinkKey(dataSourceId));
        final ServiceResponse resp = session.execute(dataSinkReadRq);
        if (ApplicationException.isOk(resp.getReturnCode()) && resp instanceof CrudSurrogateKeyResponse) {
            dataSinkCfg = ((CrudSurrogateKeyResponse<DataSinkDTO, FullTrackingWithVersion>) resp).getData();
        } else {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST, "DataSink " + dataSourceId);
        }

        if (Boolean.TRUE.equals(dataSinkCfg.getCheckDuplicateFilename())) {
            final CheckSinkFilenameUsedRequest checkSinkFilenameUsedRequest = new CheckSinkFilenameUsedRequest();
            checkSinkFilenameUsedRequest.setFileOrQueueName(sourceURI);
            final CheckSinkFilenameUsedResponse checkResponse = ((CheckSinkFilenameUsedResponse) session.execute(checkSinkFilenameUsedRequest));

            if (checkResponse.getIsUsed()) {
                isDuplicateImport = true;
                final ErrorRequest req = new ErrorRequest();
                req.setReturnCode(T9tException.IOF_DUPLICATE);
                req.setErrorDetails("Duplicate import file " + sourceURI + " for data sink " + dataSinkCfg.getDataSinkId());
                session.execute(req);
            }
        }

        if (dataSinkCfg.getBaseClassPqon() != null) {
            baseBClass = BonaPortableFactory.getBClassForPqon(dataSinkCfg.getBaseClassPqon());
        }

        final String formatName;
        if (dataSinkCfg.getCommFormatType().getBaseEnum() != MediaType.USER_DEFINED) {
            formatName = dataSinkCfg.getCommFormatType().name();
        } else {
            formatName = dataSinkCfg.getCommFormatName();
        }
        if (formatName != null) {
            inputFormatConverter = Jdp.getRequired(IInputFormatConverter.class, formatName);
        } else {
            throw new T9tException(T9tException.INVALID_CONFIGURATION,
                    "Input format Converter has not been defined for camelRoute " + dataSinkCfg.getCamelRoute());
        }
        if (inputFormatConverter != null) {
            inputFormatConverter.open(this, params, baseBClass);
        }
        if (dataSinkCfg.getPreTransformerName() != null) {
            inputTransformer = Jdp.getRequired(IInputDataTransformer.class, dataSinkCfg.getPreTransformerName());
        } else {
            inputTransformer = new IdentityTransformer();
        }
        inputTransformer.open(this, params, baseBClass);

        if (dataSinkCfg.getResponseDataSinkRef() != null) {
            responseBuffer = new ArrayList<>(MAX_RESPONSES);
        }

        if (dataSinkCfg.getCamelRoute() == null) {
            sinkDTO.setCamelTransferStatus(ExportStatusEnum.RESPONSE_OK); // means DONE
        } else {
            // if a camelRoute exists, we don't know the export status yet
            sinkDTO.setCamelTransferStatus(ExportStatusEnum.UNDEFINED);
        }

        inputProcessingParallel = dataSinkCfg.getInputProcessingParallel();
        isPooledProcessing = inputProcessingParallel != null && inputProcessingParallel >= 2;
        if (isPooledProcessing) {
            if (dataSinkCfg.getInputProcessingSplitter() == null) {
                throw new T9tException(T9tException.INVALID_CONFIGURATION,
                        "Input processing splitter has not been defined for data sink {}" + dataSinkCfg.getDataSinkId());
            }
            inputProcessingSplitter = Jdp.getRequired(IInputQueuePartitioner.class, dataSinkCfg.getInputProcessingSplitter());
            initWorkerThreadExecutors();
        }

        sinkDTO.setPlannedRunDate(start);
        sinkDTO.setCommTargetChannelType(dataSinkCfg.getCommTargetChannelType());
        sinkDTO.setCommFormatType(dataSinkCfg.getCommFormatType());
        sinkDTO.setIsInput(Boolean.TRUE);
        sinkDTO.setCategory(dataSinkCfg.getCategory());
        sinkDTO.setFileOrQueueName(sourceURI);
        sinkDTO.setDataSinkRef(new DataSinkRef(dataSinkCfg.getObjectRef()));
        return dataSinkCfg;
    }

    @Override
    public void close() {
        // flush any buffered data in the transformer
        final RequestParameters rp = inputTransformer.getPending();
        if (rp != null) {
            process(rp);
        }

        // waiting for pooledProcessingFutures to be processed
        processAndPurgePooledProcessingFutures();
        // shutdown and purge the worker threads if any
        shutdownAndPurgeWorkerThreadExecutors();

        // terminate the transformer. Flush it first
        inputTransformer.close();
        inputFormatConverter.close();

        // write any remaining response records
        addOrFlushResponses(null);

        // calculate statistics and write the sink record
        final long end = System.currentTimeMillis();
        sinkDTO.setProcessingTime((int) (end - start.toEpochMilli()));
        sinkDTO.setNumberOfSourceRecords(Integer.valueOf(numSource.get()));
        sinkDTO.setNumberOfMappedRecords(Integer.valueOf(numProcessed.get()));
        sinkDTO.setNumberOfErrorRecords(Integer.valueOf(numError.get()));

        if (dataSinkCfg.getCamelRoute() != null && ExportStatusEnum.UNDEFINED == sinkDTO.getCamelTransferStatus()) {
            sinkDTO.setCamelTransferStatus(ExportStatusEnum.RESPONSE_OK);
        }

        session.execute(new StoreSinkRequest(sinkDTO));

        session.close();
        LOGGER.info("Imported dataSource {}, URI {}: {} records processed in {} ms ({} errors)", dataSinkCfg.getDataSinkId(), sinkDTO.getFileOrQueueName(),
                sinkDTO.getNumberOfSourceRecords(), sinkDTO.getProcessingTime(), sinkDTO.getNumberOfErrorRecords());

        if (isDuplicateImport) {
            throw new T9tException(T9tException.IOF_DUPLICATE, "Duplicate import file " + getSourceURI() + " for data sink " + dataSinkCfg.getDataSinkId());
        }
    }

    @Override
    public void process(final InputStream is) {
        try {
            inputFormatConverter.process(is);
        } catch (final Exception e) {
            sinkDTO.setCamelTransferStatus(ExportStatusEnum.PROCESSING_ERROR);
            throw e;
        }
    }

    @Override
    public void process(final byte[] data) {
        try {
            inputFormatConverter.process(data);
        } catch (final Exception e) {
            sinkDTO.setCamelTransferStatus(ExportStatusEnum.PROCESSING_ERROR);
            throw e;
        }
    }

    @Override
    public void process(final BonaPortable dto) {
        final int recordNo = numSource.incrementAndGet();

        // Fast fail in case of duplicates
        if (isDuplicateImport) {
            numError.incrementAndGet();
            return;
        }

        RequestParameters rp = null;

        // validate the first step's output
        try {
            dto.treeWalkString(STRING_TRIMMER, true);
            dto.validate(); // validate that the parser created a valid DTO

            // ok, DTO is good...
            try {
                rp = inputTransformer.transform(dto);
                if (rp != null) {
                    rp.validate();
                }
            } catch (final Exception e) {
                // in case of an exception during conversion, send an error request
                rp = conditionalLog("Transform", recordNo, e);
            }
        } catch (final Exception e) {
            // in case of an exception during conversion, send an error request
            rp = conditionalLog("Parse", recordNo, e);
        }

        // now feed it into the backend (unless it has been buffered)...
        if (rp != null) {
            process(rp);
        }
    }

    @Override
    public ServiceResponse process(final RequestParameters rp) {
        if (isDuplicateImport) {
            numError.incrementAndGet();
            final ServiceResponse resp = new ServiceResponse();
            resp.setReturnCode(T9tException.IOF_DUPLICATE);
            resp.setErrorDetails("Duplicate import file " + sinkDTO.getFileOrQueueName() + " for data sink " + dataSinkCfg.getDataSinkId());
            return resp;
        }

        numProcessed.incrementAndGet();
        if (isPooledProcessing) {
            final int threadNum = Math.abs(inputProcessingSplitter.determinePartitionKey(rp) % inputProcessingParallel);
            final ExecutorService executor = workerThreadExecutors.get(threadNum);
            pooledProcessingFutures.add(executor.submit(() -> {
                return session.execute(rp);
            }));
            return new ServiceResponse();
        } else {
            // default execution
            return processServiceResponse(session.execute(rp));
        }
    }


    @Override
    public ServiceResponse execute(final RequestParameters rp) {
        // simple delegate to the session - no counting, no pooling
        return session.execute(rp);
    }

    @Override
    public <T extends ServiceResponse> T executeAndCheckResult(final RequestParameters params, final Class<T> requiredType) {
        final ServiceResponse response = session.execute(params);
        if (!ApplicationException.isOk(response.getReturnCode())) {
            LOGGER.error("Error during request handler execution for {} (returnCode={}, errorMsg={}, errorDetails={})", params.ret$PQON(),
                    response.getReturnCode(), response.getErrorMessage(), response.getErrorDetails());
            throw new T9tException(response.getReturnCode(), response.getErrorDetails());
        }
        // the response must be a subclass of the expected one
        if (!requiredType.isAssignableFrom(response.getClass())) {
            LOGGER.error("Error during request handler execution for {}, expected response class {} but got {}", params.ret$PQON(),
                    requiredType.getSimpleName(), response.ret$PQON());
            throw new T9tException(T9tException.INCORRECT_RESPONSE_CLASS, requiredType.getSimpleName());
        }
        return requiredType.cast(response); // all OK
    }

    @Override
    public String getSourceURI() {
        return sinkDTO.getFileOrQueueName();
    }

    @Override
    public String getTenantId() {
        if (session.getTenantId() != null) {
            return session.getTenantId();
        }
        return null;
    }

    @Override
    public Object getHeaderData(final String name) {
        return headerData.get(name);
    }

    @Override
    public void setHeaderData(final String name, final Object value) {
        headerData.put(name, value);
    }

    @Override
    public DataSinkDTO getDataSinkDTO() {
        return dataSinkCfg;
    }

    // log according to configured severity and return an error request
    // this is called if an exception occurs during construction of either the DTO or the request
    protected ErrorRequest conditionalLog(final String where, final int recordNo, final Exception e) {
        final int errorCode = e instanceof ApplicationException ae ? ae.getErrorCode() : T9tException.GENERAL_EXCEPTION;
        final String details = where + ": " + sourceReference + ", record " + recordNo + ": " + e.getClass().getSimpleName() + ": " + e.getMessage();
        LOGGER.error(details, e);
        final ErrorRequest errorRq = new ErrorRequest();
        errorRq.setErrorDetails(details);
        errorRq.setReturnCode(errorCode);
        return errorRq;
    }

    /** Store additional responses, or flush the stored responses. */
    protected void addOrFlushResponses(final List<BonaPortable> newResponses) {
        if (responseBuffer == null) {
            // no buffer: no response data sink => nothing to do
            return;
        }
        if (newResponses != null) {
            // there is a response data sink, and we have at least one new record to store and this is not the flush operation
            if (newResponses.size() + responseBuffer.size() <= MAX_RESPONSES) {
                // just buffer it, we take care later
                responseBuffer.addAll(newResponses);
                return;
            }
            // fall through to write / flush because we have too many now
        }
        // we have stored records, and new ones, and want to write them now
        final WriteRecordsToDataSinkRequest writeResponsesRequest = new WriteRecordsToDataSinkRequest();
        writeResponsesRequest.setDataSinkId(((DataSinkKey) dataSinkCfg.getResponseDataSinkRef()).getDataSinkId());
        writeResponsesRequest.setRecords1(responseBuffer);
        writeResponsesRequest.setRecords2(newResponses);
        session.execute(writeResponsesRequest);
        responseBuffer.clear();
    }

    protected void initWorkerThreadExecutors() {
        workerThreadExecutors = new ArrayList<>(inputProcessingParallel);
        for (int i = 0; i < inputProcessingParallel; i++) {
            final String threadName = WORKER_THREAD_NAME_PREFIX + i;
            workerThreadExecutors.add(Executors.newSingleThreadExecutor(call -> new Thread(call, threadName)));
            LOGGER.debug("New thread has been created: {}", threadName);
        }
    }

    protected void shutdownAndPurgeWorkerThreadExecutors() {
        if (workerThreadExecutors != null) {
            for (int i = 0; i < workerThreadExecutors.size(); i++) {
                final String threadName = WORKER_THREAD_NAME_PREFIX + i;
                final ExecutorService executor = workerThreadExecutors.get(0);
                LOGGER.debug("Shutting down thread: {}", threadName);
                executor.shutdown();
            }
            workerThreadExecutors.clear();
        }
    }

    protected ServiceResponse processServiceResponse(final ServiceResponse response) {
        if (!ApplicationException.isOk(response.getReturnCode())) {
            numError.incrementAndGet();
        }
        if (response instanceof ImportStatusResponse isResp) {
            final List<BonaPortable> responses = isResp.getResponses();
            if (responses != null && !responses.isEmpty()) {
                addOrFlushResponses(responses);
            }
        }
        return response;
    }

    protected void processAndPurgePooledProcessingFutures() {
        for (final Future<ServiceResponse> future: pooledProcessingFutures) {
            try {
                processServiceResponse(future.get());
            } catch (ExecutionException e) {
                numError.incrementAndGet();
                LOGGER.error("One of the pooledProcessingFutures throws an exception!", e);
            } catch (InterruptedException e) {
                numError.incrementAndGet();
                LOGGER.error("Worker thread got interrupted!", e);
            }
        }
        pooledProcessingFutures.clear();
    }
}
