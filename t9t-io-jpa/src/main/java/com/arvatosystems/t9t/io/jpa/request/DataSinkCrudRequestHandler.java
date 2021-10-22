/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.io.jpa.request;

import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.in.services.IInputFormatConverter;
import com.arvatosystems.t9t.io.CamelPostProcStrategy;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkCategoryType;
import com.arvatosystems.t9t.io.DataSinkDTO;
import com.arvatosystems.t9t.io.DataSinkRef;
import com.arvatosystems.t9t.io.InputProcessingType;
import com.arvatosystems.t9t.io.IOTools;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.event.DataSinkChangedEvent;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.request.DataSinkCrudRequest;
import com.arvatosystems.t9t.io.services.IDataSinkDefaultConfigurationProvider;
import com.arvatosystems.t9t.out.services.ICommunicationFormatGenerator;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.media.MediaCategory;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaTypeDescriptor;
import de.jpaw.dp.Jdp;

public class DataSinkCrudRequestHandler extends AbstractCrudSurrogateKey42RequestHandler<DataSinkRef, DataSinkDTO, FullTrackingWithVersion, DataSinkCrudRequest, DataSinkEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSinkCrudRequestHandler.class);

    private static final String[] FORBIDDEN_FILE_PATH_ELEMENTS = { ":", "\\", "../" };

//  @Inject
    private final IDataSinkEntityResolver sinksResolver = Jdp.getRequired(IDataSinkEntityResolver.class);

//  @Inject
    private final IDataSinkDTOMapper sinksMapper = Jdp.getRequired(IDataSinkDTOMapper.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public ServiceResponse execute(RequestContext ctx, DataSinkCrudRequest crudRequest) throws Exception {
        DataSinkDTO data = crudRequest.getData();
        if (data != null) {
            // normalize by tenantId
            // not required ATM, since no object ref specified ... if (data.getTenantRef()...)
        }

        // Validation for operation CREATE, MERGE & UPDATE
        if (crudRequest.getCrud() == OperationType.CREATE
            || crudRequest.getCrud() == OperationType.MERGE
            || crudRequest.getCrud() == OperationType.UPDATE) {

            // If inputProccessingType is not null & not LOCAL, inputProcessingTarget must not be empty
            if (data.getInputProcessingType() != null && !InputProcessingType.LOCAL.equals(data.getInputProcessingType())
                    && (data.getInputProcessingTarget() == null || data.getInputProcessingTarget().isEmpty())) {
                throw new T9tException(T9tException.ILE_REQUIRED_PARAMETER_IS_NULL, "Input processing target must not be empty.");
            }
        }

        DataSinkDTO dataSinkDTO = null;
        if (crudRequest.getCrud() == OperationType.DELETE) {
            // In case of delete, we need to save the key to allow providing a DataSinkKey (with dataSinkId) in the event later on
            dataSinkDTO = sinksMapper.mapToDto(sinksResolver.findActive(crudRequest.getKey(), false));
        }

        final CrudSurrogateKeyResponse<DataSinkDTO, FullTrackingWithVersion> response = execute(ctx, sinksMapper, sinksResolver, crudRequest);

        if (response.getReturnCode() == 0
            && (crudRequest.getCrud() == OperationType.ACTIVATE
                || crudRequest.getCrud() == OperationType.INACTIVATE
                || crudRequest.getCrud() == OperationType.CREATE
                || crudRequest.getCrud() == OperationType.DELETE
                || crudRequest.getCrud() == OperationType.MERGE
                || crudRequest.getCrud() == OperationType.PATCH
                || crudRequest.getCrud() == OperationType.UPDATE)) {

            if (dataSinkDTO == null) {
                dataSinkDTO = response.getData();
            }

            executor.publishEvent(new DataSinkChangedEvent(dataSinkDTO, crudRequest.getCrud()));
        }

        return response;
    }

    // do a plausibility check to prevent the creation of incorrect data
    // TODO this method is too complex. Refactor in a simplere on with less ifs..
    protected void checkConfiguration(DataSinkDTO intended) {
        final MediaTypeDescriptor desc = MediaTypeInfo.getFormatByType(intended.getCommFormatType());
        final MediaType baseType = intended.getCommFormatType().getBaseEnum() instanceof MediaType ? (MediaType)intended.getCommFormatType().getBaseEnum() : null;

        // if category is "REPORT", then only CSV, XLS and PDF are allowed (or a delegation to the report config)
        Enum<?> anyEnum = intended.getCategory().getBaseEnum();
        if (anyEnum instanceof DataSinkCategoryType) {
            DataSinkCategoryType baseEnum = (DataSinkCategoryType)anyEnum;   // Why can't I just switch on any Enum? Java is sooo great! :-(
            switch (baseEnum) {
            case REPORT:
                switch (intended.getCommTargetChannelType()) {
                case FILE:
                case KAFKA:
                case NULL:
                    break; // all OK
                default:
                    throw new T9tException(T9tException.INVALID_CONFIGURATION, "Invalid channel type for reports");
                }
                if (baseType == null) {
                    throw new T9tException(T9tException.INVALID_CONFIGURATION, "Invalid format type for reports");
                }
                switch (baseType) {
                case CSV:
                case PDF:
                case XLS:
                case UNDEFINED: // in this case, the format is provided by the caller
                    // all good
                    break;
                default:
                    throw new T9tException(T9tException.DATASINK_UNSUPPORTED_FORMAT, "report output does not support "
                            + intended.getCommFormatType().toString());
                }
                break;
            case USER_DATA:
            case DATA_EXPORT:
                if ((desc != null && desc.getFormatCategory() == MediaCategory.RECORDS) ||
                    MediaType.UNDEFINED.equals(baseType) ||
                    MediaType.USER_DEFINED.equals(baseType)) {
                    break;
                }
                throw new T9tException(T9tException.DATASINK_UNSUPPORTED_FORMAT, "data export output does not support "
                        + intended.getCommFormatType().toString());
            case CUSTOMER_COMMUNICATION:
                if (desc == null || (desc.getFormatCategory() != MediaCategory.TEXT && desc.getFormatCategory() != MediaCategory.DOCUMENT)) {
                    throw new T9tException(T9tException.DATASINK_UNSUPPORTED_FORMAT, "formatted customer communication output does not support "
                            + intended.getCommFormatType().toString());
                }
                break;
            case MASTER_DATA:
                // can be anything
            }
        }

        if ((intended.getCategory() != null) && (intended.getCategory().getBaseEnum() == DataSinkCategoryType.CUSTOMER_COMMUNICATION)) {
            if (intended.getCommTargetChannelType() != CommunicationTargetChannelType.FILE) {
                throw new T9tException(T9tException.INVALID_CONFIGURATION, "Customer communication data sinks must have channel type FILE");
            }
        }

        if (intended.getCommFormatType().getBaseEnum() == MediaType.USER_DEFINED) {
            // validate that if USER_DEFINED communication format type is used, the communication format name have to be NOT NULL
            if (intended.getCommFormatName() == null) {
                throw new T9tException(T9tException.INVALID_CONFIGURATION,
                    "Communication Format is USER_DEFINED but custom communication format name is not defined (NULL)");
            }
        } else  {
            // validate that if USER_DEFINED communication format type is NOT used, the communication format name have to be NULL
            if (intended.getCommFormatName() != null) {
                throw new T9tException(T9tException.INVALID_CONFIGURATION,
                    "Communication Format is not USER_DEFINED but custom communication format name is defined (NOT NULL)");
            }
        }

        // check that we got valid qualifiers

        final boolean isInput = Boolean.TRUE.equals(intended.getIsInput());
        if (intended.getCommFormatName() != null) {
            if (isInput) {
                Jdp.getRequired(IInputFormatConverter.class, intended.getCommFormatName());
            } else {
                Jdp.getRequired(ICommunicationFormatGenerator.class, intended.getCommFormatName());
            }
        }
        if (intended.getPreTransformerName() != null) {
            final IDataSinkDefaultConfigurationProvider configPresetProvider = isInput ?
              Jdp.getRequired(IInputDataTransformer.class,     intended.getPreTransformerName()) :
              Jdp.getRequired(IPreOutputDataTransformer.class, intended.getPreTransformerName());
            // if we use XML, but no root element has been specified, populate it by defaults
            if (intended.getCommFormatType().getBaseEnum() == MediaType.XML && intended.getXmlRootElementName() == null) {
                LOGGER.info("Storing XML configuratiom without root element: auto-filling fields");
                IOTools.mergePreset(intended, configPresetProvider.getDefaultConfiguration(isInput));
            }
        }
    }

    @Override
    protected final void validateUpdate(DataSinkEntity current, DataSinkDTO intended) {
        // test to avoid changing data of a different tenant, or changing the tenant at all
        checkConfiguration(intended);
        validateEncoding(intended);

        boolean isInput = Boolean.TRUE.equals(intended.getIsInput());
        if (intended.getCommTargetChannelType().equals(CommunicationTargetChannelType.FILE) && (!isInput)) {
            validateFilePathPattern(intended.getFileOrQueueNamePattern());
            validateCamelRoutingParams(intended);
        }
    }

    private static void validateCamelRoutingParams(DataSinkDTO intended) {
        if (intended.getCamelRoute() == null) {
            return;
        }

        if (CamelPostProcStrategy.MOVE.equals(intended.getSuccessRoutingStrategy())) {
            if (intended.getSuccessDestPattern() == null) {
                throw new T9tException(T9tIOException.UNDEFINED_CAMEL_SUCCESS_DEST_PATH_ERROR);
            } else {
                validateFilePathPattern(intended.getSuccessDestPattern());
            }
        }

        if (CamelPostProcStrategy.MOVE.equals(intended.getFailedRoutingStrategy())) {
            if (intended.getFailureDestPattern() == null) {
                throw new T9tException(T9tIOException.UNDEFINED_CAMEL_FAILURE_DEST_PATH_ERROR);
            } else {
                validateFilePathPattern(intended.getFailureDestPattern());
            }
        }
    }

    @Override
    protected final void validateCreate(DataSinkDTO intended) {
        checkConfiguration(intended);
        validateEncoding(intended);

        boolean isInput = Boolean.TRUE.equals(intended.getIsInput());
        if (intended.getCommTargetChannelType().equals(CommunicationTargetChannelType.FILE) && (!isInput)) {
            validateFilePathPattern(intended.getFileOrQueueNamePattern());
            validateCamelRoutingParams(intended);
        }
    }

    private static void validateFilePathPattern(String pattern) {
        for (String vorbiddenElement : FORBIDDEN_FILE_PATH_ELEMENTS) {
            if (pattern.contains(vorbiddenElement)) {
                throw new T9tException(T9tIOException.FORBIDDEN_FILE_PATH_ELEMENTS);
            }
        }


        if (pattern.startsWith("/")) {
            throw new T9tException(T9tIOException.FORBIDDEN_FILE_PATH_ELEMENTS);
        }
    }

    private void validateEncoding(DataSinkDTO intended) {
        if (intended.getOutputEncoding() != null) {
            try {
                Charset.forName(intended.getOutputEncoding());
            } catch (IllegalCharsetNameException | UnsupportedCharsetException ue) {
                throw new T9tException(T9tException.DATASINK_UNSUPPORTED_ENCODING, intended.getOutputEncoding());
            }
        }
    }
}
