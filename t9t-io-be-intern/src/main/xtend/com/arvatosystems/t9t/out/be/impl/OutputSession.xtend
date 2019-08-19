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
package com.arvatosystems.t9t.out.be.impl

import com.arvatosystems.t9t.base.FieldMappers
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.output.ExportStatusEnum
import com.arvatosystems.t9t.base.output.OutputSessionParameters
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.IOutputSession
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.io.CamelExecutionScheduleType
import com.arvatosystems.t9t.io.CommunicationTargetChannelType
import com.arvatosystems.t9t.io.DataSinkDTO
import com.arvatosystems.t9t.io.DataSinkRef
import com.arvatosystems.t9t.io.OutboundMessageDTO
import com.arvatosystems.t9t.io.SinkDTO
import com.arvatosystems.t9t.io.T9tIOException
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedRequest
import com.arvatosystems.t9t.io.request.CheckSinkFilenameUsedResponse
import com.arvatosystems.t9t.io.request.ProcessCamelRouteRequest
import com.arvatosystems.t9t.out.be.ICommunicationFormatGenerator
import com.arvatosystems.t9t.out.be.IPreOutputDataTransformer
import com.arvatosystems.t9t.out.be.impl.formatgenerator.FormatGeneratorDumb
import com.arvatosystems.t9t.out.be.impl.output.FoldableParams
import com.arvatosystems.t9t.out.be.impl.output.PatternExpansionUtil
import com.arvatosystems.t9t.out.be.impl.output.SpecificTranslationProvider
import com.arvatosystems.t9t.out.services.IAsyncTransmitter
import com.arvatosystems.t9t.out.services.IOutPersistenceAccess
import com.arvatosystems.t9t.out.services.IOutputResource
import com.arvatosystems.t9t.translation.services.ITranslationProvider
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigRequest
import com.arvatosystems.t9t.uiprefsv3.request.LeanGridConfigResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.api.media.MediaTypeInfo
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.pojos.api.media.EnumOutputType
import de.jpaw.bonaparte.pojos.api.media.MediaType
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor
import de.jpaw.bonaparte.pojos.api.media.MediaXType
import de.jpaw.dp.Dependent
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.List

@AddLogger
@Dependent
class OutputSession implements IOutputSession {
    @Inject protected RequestContext        ctx
    @Inject protected IOutPersistenceAccess dpl
    @Inject protected IExecutor             messaging
    @Inject protected IAsyncTransmitter     asyncTransmitter

    protected enum State {
        OPENED, CLOSED, LAZY
    }

    protected State                     currentState    = State.CLOSED;
    protected Charset                   encoding        = null
    protected DataSinkDTO               sinkCfg         = null;
    protected OutputSessionParameters   params          = null
    protected IOutputResource           outputResource  = null
    protected ICommunicationFormatGenerator dataGenerator = null
    protected IPreOutputDataTransformer transformer     = null
    protected MediaTypeDescriptor       usedFormat      = null; // available after a successful open()
    protected Long                      thisSinkRef     = null;
    protected SinkDTO                   thisSink        = null;
    protected FoldableParams            foldableParams  = null;
    protected int                       sourceRecordCounter = 0;
    protected int                       mappedRecordCounter = 0;
    protected long                      exportStarted;

    def protected void validateState(State expectedState) {
        if (currentState != expectedState) {
            LOGGER.error("Program logic error for open()/write()/close(). Current state is {}, expected state is {}", currentState, expectedState);
            throw new T9tException(T9tException.SESSION_OPEN_CLOSE_SEQUENCE_ERROR,
                    String.format("Current: %s, Expected: %s", currentState, expectedState));
        }
    }

    def protected IPreOutputDataTransformer getPreOutputDataTransformer() {
        if (sinkCfg.preTransformerName.nullOrEmpty)
            return OutputDataTransformerIdentity.INSTANCE
        val transformer = Jdp.getOptional(IPreOutputDataTransformer, sinkCfg.preTransformerName);
        if (transformer === null) {
            throw new T9tException(T9tIOException.OUTPUT_PRE_TRANSFORMER_NOT_FOUND, sinkCfg.preTransformerName);
        }
        return transformer
    }

    def protected getFoldableParams(String gridId, OutputSessionParameters params, MediaXType communicationFormatType) {
        val req = new LeanGridConfigRequest => [
            it.gridId                 = gridId
            selection                 = params.selection
            // translateInvisibleHeaders = false
        ]
        val gridResp = messaging.executeSynchronousAndCheckResult(req, LeanGridConfigResponse);
        val gridConfig = gridResp.leanGridConfig

        val numVisible = gridResp.headers.size();
        if (numVisible > 255 && communicationFormatType?.baseEnum === MediaType.XLS) {
            LOGGER.error("Too many columns ({}) have been selected for an Excel report (XLS format).", numVisible);
            throw new T9tException(T9tIOException.TOO_MANY_COLUMNS_FOR_EXCEL_EXPORT);
        }

        // optionally prepend a field prefix
        val selectedFields = gridConfig.fields.map[FieldMappers.addPrefix(it)].toList

        val relevantEnumType = params.getEnumOutputType(); // TODO: use defaults from sinkCfg

        val enumTranslator = if (relevantEnumType == EnumOutputType.DESCRIPTION) {
            val translationProvider = Jdp.getRequired(ITranslationProvider);
            new SpecificTranslationProvider(translationProvider, ctx.tenantId, ctx.internalHeaderParameters.getLanguageCode());
        }
        val applyVariantFilter = (params.variantFilter !== null) && (params.variantFilter.booleanValue());
        return new FoldableParams(selectedFields, gridResp.headers, relevantEnumType, enumTranslator, applyVariantFilter);
    }

    /**
     * {@inheritDoc}
     */
    override Long open(OutputSessionParameters params) {
        validateState(State.CLOSED);

        LOGGER.debug("OutputSession.open({})", params.toString());
        exportStarted = System.currentTimeMillis;

        // use a default asOf date, if non supplied
        if (params.getAsOf() === null) {
            params.setAsOf(ctx.executionStart);
        }
        //params.freeze   // we store these for some time and need consistency
        this.params = params

        // read the data sink configuration for this record
        sinkCfg = dpl.getDataSinkDTO(params.getDataSinkId());

        // get communication target type and format type
        var CommunicationTargetChannelType communicationTargetChannelType = sinkCfg.commTargetChannelType;
        var MediaXType communicationFormatType = sinkCfg.commFormatType;

        // plausi check for communicationFormatType
        if (params.communicationFormatType !== null && params.communicationFormatType?.baseEnum != MediaType.UNDEFINED) {
            // format defined in params. Must either match or have undefined in cfg
            LOGGER.debug("param comm format type is {}, data sink comm format type is {}", params.communicationFormatType.name, communicationFormatType.name);
            if (communicationFormatType.baseEnum == MediaType.UNDEFINED) {
                communicationFormatType = params.communicationFormatType
            } else if (communicationFormatType != params.communicationFormatType) {
                LOGGER.error("param comm format type mismatch: {} vs. {}", communicationFormatType, params.communicationFormatType);
                throw new T9tException(T9tIOException.FORMAT_MISMATCH, communicationFormatType.name() + "<>" + params.getCommunicationFormatType().name());
                // if both conditions not true, cfg table and caller both specified the same format, which is fine
            }
        } else {
            // format has not been defined in param. Must have it in cfg!
            if (communicationFormatType.baseEnum == MediaType.UNDEFINED) {
                LOGGER.error("param comm format type is undefined!");
                throw new T9tException(T9tIOException.FORMAT_UNSPECIFIED);
            }
        }

        // validate that if USER_DEFINED communication format type is used, the communication format name have to be NOT NULL
        if (MediaType.USER_DEFINED == communicationFormatType?.baseEnum && sinkCfg.commFormatName === null) {
            LOGGER.error("Communication Format is USER_DEFINED but custom communication format name is not defined (NULL)");
            throw new T9tException(T9tException.INVALID_CONFIGURATION, "Communication Format is USER_DEFINED but custom communication format name is not defined (NULL)");
        } else if (MediaType.USER_DEFINED != communicationFormatType?.baseEnum && sinkCfg.commFormatName !== null) {
            // validate that if USER_DEFINED communication format type is NOT used, the communication format name have to be NULL
            LOGGER.error("Communication Format is not USER_DEFINED but custom communication format name is defined (NOT NULL)");
            throw new T9tException(T9tException.INVALID_CONFIGURATION, "Communication Format is not USER_DEFINED but custom communication format name is defined (NOT NULL)");
        }

        usedFormat = MediaTypeInfo.getFormatByType(communicationFormatType);
        encoding = if (sinkCfg.outputEncoding !== null) Charset.forName(sinkCfg.outputEncoding) else StandardCharsets.UTF_8


        // get expanded file or queue name from pattern
        thisSink = ctx.customization.newDtoInstance(SinkDTO.class$rtti(), SinkDTO);
        thisSinkRef = dpl.getNewSinkKey();
        val gridId = params.gridId ?: sinkCfg.gridId;

        val String expandedName = PatternExpansionUtil.expandFileOrQueueName(ctx, sinkCfg.getFileOrQueueNamePattern(), params, usedFormat,
            #{
                "gridId"    -> (gridId ?: "NOGRID"),
                "ref"       -> thisSinkRef.toString
            }
        );

        LOGGER.debug("Opening output session for tenant {} / user {} using sink configuration (name={}, channel={}, format={}) to gridId {} / target {}",
                ctx.tenantId, ctx.userId, params.dataSinkId, communicationTargetChannelType, communicationFormatType,
                gridId ?: "(null)", expandedName);

        if (Boolean.TRUE.equals(sinkCfg.checkDuplicateFilename)) {
            val checkResponse = messaging.executeSynchronousAndCheckResult(new CheckSinkFilenameUsedRequest => [
                                                                                fileOrQueueName = expandedName
                                                                           ],
                                                                           CheckSinkFilenameUsedResponse)

            if (checkResponse.isUsed) {
                throw new T9tException(T9tException.IOF_DUPLICATE, "Duplicate export file " + expandedName + " for data sink " + sinkCfg.dataSinkId)
            }
        }

        if (gridId !== null)
            foldableParams = getFoldableParams(gridId, params, communicationFormatType)
        transformer = getPreOutputDataTransformer();
        outputResource = Jdp.getRequired(IOutputResource, sinkCfg.commTargetChannelType.name)


        // if everything is okay, log data sink into database
        thisSink => [
            objectRef               = thisSinkRef
            plannedRunDate          = ctx.internalHeaderParameters.plannedRunDate
            fileOrQueueName         = expandedName
            originatorRef           = params.originatorRef
            configurationRef        = params.configurationRef
            dataSinkRef             = new DataSinkRef(sinkCfg.objectRef)
            requiredPermission      = params.requiredPermission
            genericRefs1            = params.getGenericRefs1
            genericRefs2            = params.getGenericRefs2
            isInput                 = Boolean.FALSE
            category                = sinkCfg.category
        ]
        thisSink.commTargetChannelType  = communicationTargetChannelType
        thisSink.commFormatType         = communicationFormatType
        // sinkResolver.save(thisSink);                          // no ref. integrity: save only later!

        sourceRecordCounter = 0;
        mappedRecordCounter = 0;

        if (Boolean.TRUE == sinkCfg.lazyOpen) {
            currentState = State.LAZY;
        } else {
            openStream();
        }
        return thisSinkRef;
    }

    def protected void openStream() {
        outputResource.open(sinkCfg, params, thisSinkRef, thisSink.fileOrQueueName, usedFormat, encoding)
        dataGenerator = createDataGenerator(thisSink.commFormatType, params, foldableParams);

        currentState = State.OPENED;

        dataGenerator.open(sinkCfg, params, thisSink.commFormatType, foldableParams, outputResource, encoding, ctx.tenantId)

        // output has been opened, check for altered file name
        val alteredName = outputResource.effectiveFilename
        if (alteredName !== null) {
            thisSink.fileOrQueueName = alteredName
        }

        storeListOfMappedRecords(null, transformer.headerData(sinkCfg, params));
    }

    def protected void writeOutboundMessage(Long recordRef, BonaPortable record) {
        // persist the data in the DB interface table
        val it              = ctx.customization.newDtoInstance(OutboundMessageDTO.class$rtti(), OutboundMessageDTO);
        objectRef           = dpl.getNewOutboundMessageKey()
        sinkRef             = thisSinkRef
        sourceRecordNo      = Integer.valueOf(sourceRecordCounter)
        mappedRecordNo      = Integer.valueOf(mappedRecordCounter)
        it.recordRef        = recordRef
        requestParameters   = record
        dpl.storeOutboundMessage(it)
    }


    def protected void storeListOfMappedRecords(Long recordRef, List<BonaPortable> records) {
        if (records !== null) {
            for (BonaPortable r : records) {
                val long recRef = if (recordRef !== null) recordRef.longValue else 0L;
                mappedRecordCounter += 1;
                // persist the data in the DB interface table
                if (Boolean.TRUE == sinkCfg.logMessages)
                    writeOutboundMessage(recordRef, r);
                // store as async message
                if (sinkCfg.copyToAsyncChannel !== null)
                    asyncTransmitter.transmitMessage(sinkCfg.copyToAsyncChannel, r, recordRef, "SINK", sinkCfg.dataSinkId)
                // and write it to file
                dataGenerator.generateData(sourceRecordCounter, mappedRecordCounter, recRef, r)
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    override void store(Long recordRef, BonaPortable record) {
        // see if LAZY
        if (currentState == State.LAZY)
            openStream();

        // validate user called open() before
        validateState(State.OPENED);

        ctx.incrementProgress();
        sourceRecordCounter += 1;
        storeListOfMappedRecords(recordRef, transformer.transformData(record, sinkCfg, params));
        if ((sourceRecordCounter % 250) == 0) {   // log a status every 250 records...}
            LOGGER.debug("Stored source record {} ({} mapped records)", sourceRecordCounter, mappedRecordCounter);
        }
    }

    /**
     * {@inheritDoc}
     */
    override void store(BonaPortable record) {
        store(null, record);
    }

    /**
     * {@inheritDoc}
     */
    override void close() throws Exception {
        thisSink.camelTransferStatus = ExportStatusEnum.RESPONSE_OK  // means DONE

        // if LAZY open, skip actual opening, then also closing is not required
        if (currentState != State.LAZY) {
            if (currentState != State.OPENED) {
                // ignore redundant closes - this can happen if open() itself throws an exception
                LOGGER.warn("Ignoring close() on closed output session");
                return;
            }

            // done writing!
            sourceRecordCounter += 1;  // marks EOF-records
            storeListOfMappedRecords(null, transformer.footerData(sinkCfg, params));
            sourceRecordCounter -= 1;  // marks EOF-records

            // technical footers
            dataGenerator.close()

            // close the destination
            outputResource.close

            // check if there is a Camel transfer to be performed
            if (sinkCfg.camelRoute !== null && (sinkCfg.camelExecution == CamelExecutionScheduleType.SCHEDULED || sinkCfg.camelExecution == CamelExecutionScheduleType.ASYNCHRONOUSLY)) {
                thisSink.camelTransferStatus = ExportStatusEnum.READY_TO_EXPORT;
                if (sinkCfg.camelExecution == CamelExecutionScheduleType.ASYNCHRONOUSLY) {
                    messaging.executeAsynchronous(ctx, new ProcessCamelRouteRequest(thisSink.objectRef))
                }
            }
        }

        if (sourceRecordCounter == 0 && Boolean.TRUE == sinkCfg.skipZeroRecordSinkRefs) {
            // NO OP: do not store the sink Ref
        } else {
            // update sink number of records
            thisSink.setNumberOfSourceRecords(Integer.valueOf(sourceRecordCounter));  // do not record the footer records
            thisSink.setNumberOfMappedRecords(Integer.valueOf(mappedRecordCounter));
            thisSink.processingTime = (System.currentTimeMillis - exportStarted) as int  // compute number of seconds
            dpl.storeNewSink(thisSink);
        }

        // cleanup
        thisSinkRef = null;
        usedFormat = null;
        currentState = State.CLOSED;
    }


    override OutputStream getOutputStream() {
        // see if LAZY
        if (currentState == State.LAZY)
            openStream();

        validateState(State.OPENED);
        return outputResource.getOutputStream();
    }

    override MediaXType getCommunicationFormatType() {
        return usedFormat.mediaType;
    }



    def protected ICommunicationFormatGenerator createDataGenerator(MediaXType formatType, OutputSessionParameters params, FoldableParams foldableParams) {
        if (formatType.baseEnum == MediaType.USER_DEFINED) {
            // format is free text, implementation must exist
            return Jdp.getRequired(ICommunicationFormatGenerator, sinkCfg.commFormatName)
        } else {
            // to avoid creating copies of a stub for PNG, JPG etc, first attempt is optional
            val dataGenerator = Jdp.getOptional(ICommunicationFormatGenerator, formatType.name)
            return dataGenerator ?: new FormatGeneratorDumb();
        }
    }

    override getMaxNumberOfRecords() {
        return if (sinkCfg === null) null else sinkCfg.maxNumberOfRecords
    }

    override getChunkSize() {
        return if (sinkCfg === null) null else sinkCfg.chunkSize
    }

    override getFileOrQueueName() {
        // see if LAZY
        if (currentState == State.LAZY) {
//            LOGGER.warn("LAZY opening with getFilename forces eager opening! DS = {}", sinkCfg.dataSinkId)
//            openStream();  // possibly alters file name
            LOGGER.warn("getFilename on LAZY opened (and not yet opened) dataSink {}, result possibly incorrect (for example if writing compressed)", sinkCfg.dataSinkId)
        }
        return thisSink.fileOrQueueName
    }

    override getUnwrapTracking(Boolean ospSetting) {
        if (Boolean.TRUE == sinkCfg.unwrapTracking)  // first, examine a configured setting
            return true;
        return Boolean.TRUE == ospSetting
    }

    override getZ(String key) {
        return sinkCfg.z?.get(key)
    }
}
