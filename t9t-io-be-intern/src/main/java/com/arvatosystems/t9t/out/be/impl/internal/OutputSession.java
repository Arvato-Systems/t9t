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
package com.arvatosystems.t9t.out.be.impl.internal;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.FieldMappers;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.IInputQueuePartitioner;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.io.CamelExecutionScheduleType;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.DataSinkRef;
import com.arvatosystems.t9t.io.OutboundMessageDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedRequest;
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedResponse;
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest;
import com.arvatosystems.t9t.out.be.impl.OutputDataTransformerIdentity;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorDumb;
import com.arvatosystems.t9t.out.be.impl.output.PatternExpansionUtil;
import com.arvatosystems.t9t.out.services.FoldableParams;
import com.arvatosystems.t9t.out.services.IAsyncTransmitter;
import com.arvatosystems.t9t.out.services.ICommunicationFormatGenerator;
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess;
import com.arvatosystems.t9t.out.services.IOutputResource;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;
import com.arvatosystems.t9t.translation.services.ITranslationProvider;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest;
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.media.EnumOutputType;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ExceptionUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

@Dependent
public class OutputSession implements IOutputSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutputSession.class);

    protected final RequestContext        ctx              = Jdp.getRequired(RequestContext.class);
    protected final IOutPersistenceAccess dpl              = Jdp.getRequired(IOutPersistenceAccess.class);
    protected final IExecutor             messaging        = Jdp.getRequired(IExecutor.class);
    protected final IAsyncTransmitter     asyncTransmitter = Jdp.getRequired(IAsyncTransmitter.class);
    protected final IFileUtil             fileUtil         = Jdp.getRequired(IFileUtil.class);

    protected enum State {
        OPENED, CLOSED, LAZY;
    }

    protected State                     currentState        = State.CLOSED;
    protected Charset                   encoding            = null;
    protected DataSinkDTO               sinkCfg             = null;
    protected OutputSessionParameters   params              = null;
    protected IOutputResource           outputResource      = null;
    protected ICommunicationFormatGenerator dataGenerator   = null;
    protected IPreOutputDataTransformer transformer         = null;
    @Nullable
    protected MediaTypeDescriptor       usedFormat          = null; // available after a successful open(), but only if known
    protected Long                      thisSinkRef         = null;
    protected SinkDTO                   thisSink            = null;
    protected FoldableParams            foldableParams      = null;
    protected int                       sourceRecordCounter = 0;
    protected int                       mappedRecordCounter = 0;
    protected long                      exportStarted       = System.nanoTime();
    protected IInputQueuePartitioner    processingSplitter  = null;  // only set if copyToAsyncChannel != null

    /**
     * {@inheritDoc}
     */
    @Override
    public Long open(final OutputSessionParameters osParams) {
        validateState(State.CLOSED);

        LOGGER.debug("OutputSession.open({})", osParams.toString());

        // use a default asOf date, if non supplied
        if (osParams.getAsOf() == null) {
            osParams.setAsOf(ctx.executionStart);
        }
        //params.freeze   // we store these for some time and need consistency
        this.params = osParams;

        // read the data sink configuration for this record
        sinkCfg = dpl.getDataSinkDTO(osParams.getDataSinkId());

        if (sinkCfg.getCopyToAsyncChannel() != null) {
            processingSplitter = Jdp.getRequired(IInputQueuePartitioner.class, sinkCfg.getInputProcessingSplitter());
        }

        // get communication target type and format type
        final CommunicationTargetChannelType communicationTargetChannelType = sinkCfg.getCommTargetChannelType();
        MediaXType communicationFormatType = sinkCfg.getCommFormatType();

        // plausi check for communicationFormatType
        if (osParams.getCommunicationFormatType() != null && MediaType.UNDEFINED != osParams.getCommunicationFormatType().getBaseEnum()) {
            // format defined in params. Must either match or have undefined in cfg
            LOGGER.debug("param comm format type is {}, data sink comm format type is {}", osParams.getCommunicationFormatType().name(),
                    communicationFormatType.name());
            if (MediaType.UNDEFINED == communicationFormatType.getBaseEnum()) {
                communicationFormatType = osParams.getCommunicationFormatType();
            } else if (!communicationFormatType.equals(osParams.getCommunicationFormatType())) {
                LOGGER.error("param comm format type mismatch: {} vs. {}", communicationFormatType, osParams.getCommunicationFormatType());
                throw new T9tException(T9tIOException.FORMAT_MISMATCH, communicationFormatType.name() + "<>" + osParams.getCommunicationFormatType().name());
                // if both conditions not true, cfg table and caller both specified the same format, which is fine
            }
        } else {
            // format has not been defined in param. Must have it in cfg!
            if (MediaType.UNDEFINED == communicationFormatType.getBaseEnum()) {
                LOGGER.error("param comm format type is undefined!");
                throw new T9tException(T9tIOException.FORMAT_UNSPECIFIED);
            }
        }

        if (communicationFormatType != null) {
            if (MediaType.USER_DEFINED == communicationFormatType.getBaseEnum()) {
                // validate that if USER_DEFINED communication format type is used, the communication format name has to be NOT NULL
                if (sinkCfg.getCommFormatName() == null) {
                    LOGGER.error("Communication Format is USER_DEFINED but custom communication format name is not defined (NULL)");
                    throw new T9tException(T9tException.INVALID_CONFIGURATION,
                            "Communication Format is USER_DEFINED and custom communication format name is also not defined (NULL)");
                }
            } else {
                // validate that if USER_DEFINED communication format type is NOT used, the communication format name must be NULL
                if (sinkCfg.getCommFormatName() != null) {
                    LOGGER.error("Communication Format is not USER_DEFINED but custom communication format name is defined (NOT NULL)");
                    throw new T9tException(T9tException.INVALID_CONFIGURATION,
                            "Communication Format is not USER_DEFINED but custom communication format name is also defined (NOT NULL)");
                }
            }
            usedFormat = MediaTypeInfo.getFormatByType(communicationFormatType);  // lookup will still return null for USER_DEFFINED
        }

        if (sinkCfg.getOutputEncoding() != null) {
            encoding = Charset.forName(sinkCfg.getOutputEncoding());
        } else {
            encoding = StandardCharsets.UTF_8;
        }

        // get expanded file or queue name from pattern
        thisSink = new SinkDTO();
        thisSinkRef = dpl.getNewSinkKey();
        final String gridId = osParams.getGridId() == null ? sinkCfg.getGridId() : osParams.getGridId();
        final Map<String, String> additionalParams = new HashMap<>(5);
        additionalParams.put("gridId", gridId == null ? "NOGRID" : gridId);
        additionalParams.put("ref", thisSinkRef.toString());

        final String expandedName = PatternExpansionUtil.expandFileOrQueueName(ctx, sinkCfg.getFileOrQueueNamePattern(),
                osParams, usedFormat, additionalParams);

        LOGGER.debug("Opening output session for tenant {} / user {} using sink configuration (name={}, channel={}, format={}) to gridId {} / target {}",
                ctx.tenantId, ctx.userId, osParams.getDataSinkId(), communicationTargetChannelType, communicationFormatType,
                gridId == null ? "(null)" : gridId, expandedName);

        if (Boolean.TRUE.equals(sinkCfg.getCheckDuplicateFilename())) {
            final CheckSinkFilenameUsedResponse checkResponse = messaging.executeSynchronousAndCheckResult(new CheckSinkFilenameUsedRequest(expandedName),
                    CheckSinkFilenameUsedResponse.class);

            if (checkResponse.getIsUsed()) {
                throw new T9tException(T9tException.IOF_DUPLICATE, "Duplicate export file " + expandedName + " for data sink " + sinkCfg.getDataSinkId());
            }
        }

        transformer = getPreOutputDataTransformer();  // retrieve the pre-output transformer (never null, but can be the identity mapping)

        if (gridId != null) {
            // foldable parameters provided by grid configuration
            foldableParams = getFoldableParams(gridId, osParams, communicationFormatType);
        } else {
            // maybe we get foldable parameters from the pre-output transformer (only if not a grid export)
            foldableParams = transformer.getFoldableParams(sinkCfg);
        }

        outputResource = Jdp.getRequired(IOutputResource.class, sinkCfg.getCommTargetChannelType().name());


        // if everything is okay, log data sink into database
        thisSink.setObjectRef(thisSinkRef);
        thisSink.setPlannedRunDate(ctx.internalHeaderParameters.getPlannedRunDate());
        thisSink.setFileOrQueueName(expandedName);
        thisSink.setOriginatorRef(osParams.getOriginatorRef());
        thisSink.setConfigurationRef(osParams.getConfigurationRef());
        thisSink.setDataSinkRef(new DataSinkRef(sinkCfg.getObjectRef()));
        thisSink.setRequiredPermission(osParams.getRequiredPermission());
        thisSink.setGenericRefs1(osParams.getGenericRefs1());
        thisSink.setGenericRefs2(osParams.getGenericRefs2());
        thisSink.setIsInput(Boolean.FALSE);
        thisSink.setCategory(sinkCfg.getCategory());
        thisSink.setGenericId1(osParams.getGenericId1());
        thisSink.setGenericId2(osParams.getGenericId2());
        thisSink.setCommTargetChannelType(communicationTargetChannelType);
        thisSink.setCommFormatType(communicationFormatType);
        // sinkResolver.save(thisSink);                          // no ref. integrity: save only later!

        sourceRecordCounter = 0;
        mappedRecordCounter = 0;

        if (Boolean.TRUE.equals(sinkCfg.getLazyOpen())) {
            currentState = State.LAZY;
        } else {
            openStream();
        }
        return thisSinkRef;
    }

    @Override
    public OutputStream getOutputStream() {
        // see if LAZY
        if (State.LAZY.equals(currentState))
            openStream();

        validateState(State.OPENED);
        return outputResource.getOutputStream();
    }

    @Override
    public MediaXType getCommunicationFormatType() {
        return usedFormat.getMediaType();
    }

    @Override
    public void store(BonaPortable record) {
        store(null, record);
    }

    @Override
    public void store(Long recordRef, BonaPortable record) {
        store(recordRef, "", "", record);
    }

    @Override
    public void store(Long recordRef, String partitionKey, String recordKey, BonaPortable record) {
        // see if LAZY
        if (State.LAZY.equals(currentState))
            openStream();

        // validate user called open() before
        validateState(State.OPENED);

        ctx.incrementProgress();
        sourceRecordCounter += 1;
        try {
            storeListOfMappedRecords(recordRef, partitionKey, recordKey, transformer.transformData(record, sinkCfg, params));
            if ((sourceRecordCounter % 250) == 0) { // log a status every 250 records...}
                LOGGER.debug("Stored source record {} ({} mapped records)", sourceRecordCounter, mappedRecordCounter);
            }
        } catch (IOException e) {
            LOGGER.error("Error while storing recordRef {}, partitionKey {}, recordKey {}: {}", recordRef, partitionKey, recordKey, e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        if (sinkCfg.getCamelRoute() == null) {
            thisSink.setCamelTransferStatus(ExportStatusEnum.RESPONSE_OK);  // means DONE
        } else {
            // if a camelRoute exists, we don't know the export status yet
            thisSink.setCamelTransferStatus(ExportStatusEnum.UNDEFINED);
        }

        LOGGER.debug("Setting Sink {} to camelTransferStatus: {} on close()", thisSink.getObjectRef(), thisSink.getCamelTransferStatus());

        // Store camel exceptions, throw after sink setup
        Exception outputResourceCloseError = null;

        // if LAZY open, skip actual opening, then also closing is not required
        if (currentState != State.LAZY) {
            if (currentState != State.OPENED) {
                // ignore redundant closes - this can happen if open() itself throws an exception
                LOGGER.warn("Ignoring close() on closed output session");
                return;
            }

            // done writing!
            sourceRecordCounter += 1;  // marks EOF-records
            storeListOfMappedRecords(null, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, transformer.footerData(sinkCfg, params));
            sourceRecordCounter -= 1;  // marks EOF-records

            // technical footers
            dataGenerator.close();

            // close the destination
            try {
                outputResource.close();
                if (CommunicationTargetChannelType.FILE == sinkCfg.getCommTargetChannelType() && Boolean.TRUE.equals(sinkCfg.getComputeFileSize())) {
                    final String absolutePath = fileUtil.getAbsolutePathForTenant(ctx.tenantId, thisSink.getFileOrQueueName());
                    final Path path = Paths.get(absolutePath);
                    thisSink.setFileSize(Files.size(path));
                }
                thisSink.setCamelTransferStatus(ExportStatusEnum.RESPONSE_OK);
            } catch (Exception e) {
                thisSink.setCamelTransferStatus(ExportStatusEnum.RESPONSE_ERROR);
                // throw this error after writing sink
                outputResourceCloseError = e;
            }

            // check if there is a Camel transfer to be performed
            if (sinkCfg.getCamelRoute() != null
                    && (CamelExecutionScheduleType.SCHEDULED == sinkCfg.getCamelExecution()
                     || CamelExecutionScheduleType.ASYNCHRONOUSLY == sinkCfg.getCamelExecution())
                    && !(sourceRecordCounter == 0 && Boolean.FALSE.equals(sinkCfg.getSkipZeroRecordSinkRefs())
                    // TBE-451: make sure the camelTransferStatus is set to RESPONSE_OK because camel will throw exception on empty file
                    && sinkCfg.getLazyOpen())) {
                thisSink.setCamelTransferStatus(ExportStatusEnum.READY_TO_EXPORT);
                LOGGER.debug("Setting Sink {} to camelTransferStatus: {} due to camel transfer to be performed",
                        thisSink.getObjectRef(), thisSink.getCamelTransferStatus());
                if (CamelExecutionScheduleType.ASYNCHRONOUSLY.equals(sinkCfg.getCamelExecution())) {
                    messaging.executeAsynchronous(ctx, new ProcessCamelRouteRequest(thisSink.getObjectRef()));
                }
            }
        }

        if (sourceRecordCounter != 0 || !Boolean.TRUE.equals(sinkCfg.getSkipZeroRecordSinkRefs())) {
            // update sink number of records
            thisSink.setNumberOfSourceRecords(sourceRecordCounter);  // do not record the footer records
            thisSink.setNumberOfMappedRecords(mappedRecordCounter);
            thisSink.setProcessingTime((int) ((System.nanoTime() - exportStarted) / 1000_000L));  // compute number of milliseconds
            dpl.storeNewSink(thisSink);
        } // else NO OP: do not store the sink Ref

        // cleanup
        thisSinkRef = null;
        usedFormat = null;
        currentState = State.CLOSED;

        if (outputResourceCloseError != null) {
            // if outputResource throws an error: throw it now after sink setup
            throw outputResourceCloseError;
        }
    }

    @Override
    public Integer getMaxNumberOfRecords() {
        return sinkCfg == null ? null : sinkCfg.getMaxNumberOfRecords();
    }

    @Override
    public Integer getChunkSize() {
        return sinkCfg == null ? null : sinkCfg.getChunkSize();
    }

    @Override
    public String getFileOrQueueName() {
        if (State.LAZY.equals(currentState)) {
            // LOGGER.warn("LAZY opening with getFilename forces eager opening! DS = {}",
            // sinkCfg.dataSinkId)
            // openStream(); // possibly alters file name
            LOGGER.warn("getFilename on LAZY opened (and not yet opened) dataSink {}, result possibly incorrect (for example if writing compressed)",
                    sinkCfg.getDataSinkId());
        }
        return thisSink.getFileOrQueueName();
    }

    @Override
    public boolean getUnwrapTracking(Boolean ospSetting) {
        if (Boolean.TRUE.equals(sinkCfg.getUnwrapTracking()))  // first, examine a configured setting
            return true;
        return Boolean.TRUE.equals(ospSetting);
    }

    @Override
    public Object getZ(String key) {
        return sinkCfg.getZ() == null ? null : sinkCfg.getZ().get(key);
    }


    protected void validateState(final State expectedState) {
        if (currentState != expectedState) {
            LOGGER.error("Program logic error for open()/write()/close(). Current state is {}, expected state is {}", currentState, expectedState);
            throw new T9tException(T9tException.SESSION_OPEN_CLOSE_SEQUENCE_ERROR,
                    String.format("Current: %s, Expected: %s", currentState, expectedState));
        }
    }

    @Nonnull
    protected IPreOutputDataTransformer getPreOutputDataTransformer() {

        if (sinkCfg.getPreTransformerName() == null || sinkCfg.getPreTransformerName().isEmpty()) {
            return OutputDataTransformerIdentity.INSTANCE;
        }

        final IPreOutputDataTransformer dataTransformer = Jdp.getOptional(IPreOutputDataTransformer.class, sinkCfg.getPreTransformerName());
        if (dataTransformer == null) {
            throw new T9tException(T9tIOException.OUTPUT_PRE_TRANSFORMER_NOT_FOUND, sinkCfg.getPreTransformerName());
        }

        return dataTransformer;
    }

    protected FoldableParams getFoldableParams(final String gridId, final OutputSessionParameters osParams, final MediaXType communicationFormatType) {
        final LeanGridConfigRequest req = new LeanGridConfigRequest();
        req.setGridId(gridId);
        req.setSelection(osParams.getSelection());
        // translateInvisibleHeaders = false

        final LeanGridConfigResponse gridResp = messaging.executeSynchronousAndCheckResult(req, LeanGridConfigResponse.class);
        final UILeanGridPreferences gridConfig = gridResp.getLeanGridConfig();

        final int numVisible = gridResp.getHeaders().size();
        if (numVisible > 255 && communicationFormatType != null && MediaType.XLS.equals(communicationFormatType.getBaseEnum())) {
            LOGGER.error("Too many columns ({}) have been selected for an Excel report (XLS format).", numVisible);
            throw new T9tException(T9tIOException.TOO_MANY_COLUMNS_FOR_EXCEL_EXPORT);
        }

        // optionally prepend a field prefix
        final List<String> selectedFields = gridConfig.getFields().stream().map(it -> FieldMappers.addPrefix(it)).collect(Collectors.toList());

        final EnumOutputType relevantEnumType = osParams.getEnumOutputType(); // TODO: use defaults from sinkCfg
        final SpecificTranslationProvider enumTranslator;

        if (EnumOutputType.DESCRIPTION == relevantEnumType) {
            final ITranslationProvider translationProvider = Jdp.getRequired(ITranslationProvider.class);
            enumTranslator = new SpecificTranslationProvider(translationProvider, ctx.tenantId, ctx.internalHeaderParameters.getLanguageCode());
        } else {
            enumTranslator = null;
        }

        final boolean applyVariantFilter = (osParams.getVariantFilter() != null) && (osParams.getVariantFilter().booleanValue());
        return new FoldableParams(selectedFields, gridResp.getHeaders(), relevantEnumType, enumTranslator, applyVariantFilter);
    }

    protected void openStream() {
        outputResource.open(sinkCfg, params, thisSinkRef, thisSink.getFileOrQueueName(), usedFormat, encoding);
        dataGenerator = createDataGenerator(thisSink.getCommFormatType());

        currentState = State.OPENED;

        try {
            dataGenerator.open(sinkCfg, params, thisSink.getCommFormatType(), foldableParams, outputResource, encoding, ctx.tenantId);

            // output has been opened, check for altered file name
            final String alteredName = outputResource.getEffectiveFilename();
            if (alteredName != null) {
                thisSink.setFileOrQueueName(alteredName);
            }

            storeListOfMappedRecords(null, IOutputSession.NO_PARTITION_KEY, IOutputSession.NO_RECORD_KEY, transformer.headerData(sinkCfg, params));
        } catch (Exception e) {
            LOGGER.error("Error while open stream sink id {}: {}", sinkCfg.getDataSinkId(), e.getMessage());
        }
    }

    protected ICommunicationFormatGenerator createDataGenerator(final MediaXType formatType) {
        if (MediaType.USER_DEFINED.equals(formatType.getBaseEnum())) {
            // format is free text, implementation must exist
            return Jdp.getRequired(ICommunicationFormatGenerator.class, sinkCfg.getCommFormatName());
        } else {
            // to avoid creating copies of a stub for PNG, JPG etc, first attempt is optional
            final ICommunicationFormatGenerator commFormatDataGenerator = Jdp.getOptional(ICommunicationFormatGenerator.class, formatType.name());
            return commFormatDataGenerator == null ? new FormatGeneratorDumb() : commFormatDataGenerator;
        }
    }

    protected void storeListOfMappedRecords(final Long recordRef, final String partitionKey, final String recordKey,
            final List<BonaPortable> records) throws IOException {
        if (records != null) {
            for (BonaPortable r : records) {
                final long recRef = recordRef == null ? 0L :  recordRef.longValue();
                mappedRecordCounter += 1;
                // persist the data in the DB interface table
                if (Boolean.TRUE.equals(sinkCfg.getLogMessages())) {
                    writeOutboundMessage(recordRef, r);
                }
                // store as async message
                if (sinkCfg.getCopyToAsyncChannel() != null) {
                    final int partition = processingSplitter.getPreliminaryPartitionKey(partitionKey);
                    asyncTransmitter.transmitMessage(sinkCfg.getCopyToAsyncChannel(), r, recordRef, "SINK", sinkCfg.getDataSinkId(), partition);
                }
                // and write it to file
                dataGenerator.generateData(sourceRecordCounter, mappedRecordCounter, recRef, partitionKey, recordKey, r);
            }
        }
    }

    protected void writeOutboundMessage(Long recordRef, BonaPortable record) {
        // persist the data in the DB interface table
        final OutboundMessageDTO om = new OutboundMessageDTO();
        om.setObjectRef(dpl.getNewOutboundMessageKey());
        om.setSinkRef(thisSinkRef);
        om.setSourceRecordNo(sourceRecordCounter);
        om.setMappedRecordNo(mappedRecordCounter);
        om.setRecordRef(recordRef);
        om.setRequestParameters(record);
        dpl.storeOutboundMessage(om);
    }

    @Override
    public void storeCustomElement(String name, Object value) {
        if (value != null) {
            try {
                dataGenerator.storeCustomElement(name, value.getClass(), value);
            } catch (Exception e) {
                LOGGER.error("Error while storing custom element {}: {}", name, ExceptionUtil.causeChain(e));
            }
        }
    }
}
