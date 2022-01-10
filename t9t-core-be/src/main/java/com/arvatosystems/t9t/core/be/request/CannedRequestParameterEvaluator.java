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
package com.arvatosystems.t9t.core.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.core.CannedRequestDTO;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.MapComposer;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.core.MessageParserException;
import de.jpaw.dp.Singleton;

/**
 * The CannedRequestParameterEvaluator normalizes the parameters in the CannedRequestDTO.
 * There are 3 optional fields:
 * <ul>
 * <li>jobRequestObjectName - a PQON (String)
 * <li>jobParameters - optional task parameters, in JSON notation
 * <li>request - a full request object
 * </ul>
 * At least one of the parameters must be supplied. It is possible to provide only exactly one of them:
 * <ul>
 * <li>The jobRequestObjectName can be provided if the request has no parameters or all parameters have their default values.
 * <li>jobParameters can contain a field named "@PQON", which then takes the job request objectName. If that field is not contained,
 * the jobParameters only support the jobRequestObjectName field and are needed in addition to it.
 * <li>request will be sent by programmatic callers (setup jobs). For programmatic jobs, this is the preferred alternative, because the arguments are checked
 * for validity at compile time. If only request is provided, the other two fields are populated from it.
 * </ul>
 */
@Singleton
public class CannedRequestParameterEvaluator {
    private static final String POSTFIX_REQUEST = "Request";

    public void processDTO(final CannedRequestDTO dto) {
        if (dto.getJobRequestObjectName() == null) {
            // case 1: a request object has been supplied, but nothing else
            if (dto.getRequest() == null && dto.getJobParameters() == null) {
                throw new MessageParserException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD,
                        "jobRequestObjectName, jobParameters and request cannot be all null", -1, "CannedRequestDTO");
            }
            if (dto.getJobParameters() == null) {
                // request cannot be null then by check before
                dto.setJobParameters(MapComposer.marshal(dto.getRequest()));
                dto.setJobRequestObjectName(dto.getRequest().ret$PQON());
            } else {
                // dto.jobParameters !== null
                // ASCII parameters have priority over binary object (because they can be edited)
                // we hope "@PQON" is part of the parameters...
                final Object jobRequestObjectName = dto.getJobParameters().get("@PQON");
                if (jobRequestObjectName == null) {
                    throw new MessageParserException(MessageParserException.EMPTY_BUT_REQUIRED_FIELD,
                            "jobParameters must contain @PQON if jobRequestObjectName is empty", -1, "CannedRequestDTO");
                }
                dto.setJobRequestObjectName(jobRequestObjectName.toString());

                final BonaPortable rq = BonaPortableFactory.createObject(dto.getJobRequestObjectName());
                // create binary request from request parameters
                if (rq instanceof RequestParameters) {
                    if (dto.getJobParameters() != null && !dto.getJobParameters().isEmpty()) {
                        MapParser.populateFrom(rq, dto.getJobParameters());
                    }
                    dto.setRequest((RequestParameters) rq);
                } else {
                    throw new T9tException(T9tException.NOT_REQUEST_PARAMETERS, rq.ret$PQON());
                }
            }
        } else {
            // security: enforce that the request name ends with "Request", in order to avoid that arbitrary classes are called!
            if (!dto.getJobRequestObjectName().endsWith(POSTFIX_REQUEST)) {
                dto.setJobRequestObjectName(dto.getJobRequestObjectName() + POSTFIX_REQUEST); // nice side effect: no need to type "Request!"
            }

            // if dto.request has been populated and no parameters supplied, and the pqon match, then do not clear the parameters
            if (dto.getRequest() != null && dto.getJobRequestObjectName().equals(dto.getRequest().ret$PQON()) && dto.getJobParameters() == null) {
                final BonaPortable rq = BonaPortableFactory.createObject(dto.getJobRequestObjectName());
                dto.setRequest((RequestParameters) rq);
                // all fine, leave it as it is
            } else {
                final BonaPortable rq = BonaPortableFactory.createObject(dto.getJobRequestObjectName());

                if (rq instanceof RequestParameters) {
                    if (dto.getJobParameters() != null && !dto.getJobParameters().isEmpty()) {
                        MapParser.populateFrom(rq, dto.getJobParameters());
                    }
                    dto.setRequest((RequestParameters) rq);
                } else {
                    throw new T9tException(T9tException.NOT_REQUEST_PARAMETERS, rq.ret$PQON());
                }
            }
        }
    }
}
