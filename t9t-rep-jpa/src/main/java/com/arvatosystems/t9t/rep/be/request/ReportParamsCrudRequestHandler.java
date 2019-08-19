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
package com.arvatosystems.t9t.rep.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKey42RequestHandler;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.ReportParamsRef;
import com.arvatosystems.t9t.rep.T9tRepException;
import com.arvatosystems.t9t.rep.jpa.entities.ReportParamsEntity;
import com.arvatosystems.t9t.rep.jpa.mapping.IReportParamsDTOMapper;
import com.arvatosystems.t9t.rep.jpa.persistence.IReportParamsEntityResolver;
import com.arvatosystems.t9t.rep.request.ReportParamsCrudRequest;

import de.jpaw.dp.Jdp;

public class ReportParamsCrudRequestHandler extends
        AbstractCrudSurrogateKey42RequestHandler<ReportParamsRef, ReportParamsDTO, FullTrackingWithVersion, ReportParamsCrudRequest, ReportParamsEntity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportParamsCrudRequestHandler.class);
    protected final IReportParamsEntityResolver resolver = Jdp.getRequired(IReportParamsEntityResolver.class);
    protected final IReportParamsDTOMapper mapper = Jdp.getRequired(IReportParamsDTOMapper.class);

    @Override
    public CrudSurrogateKeyResponse<ReportParamsDTO, FullTrackingWithVersion> execute(final ReportParamsCrudRequest request) throws Exception {
        return execute(mapper, resolver, request);
    }

    @Override
    protected void validateCreate(ReportParamsDTO intended) {
        validateInterval(intended);
        super.validateCreate(intended);
    }

    @Override
    protected void validateUpdate(ReportParamsEntity current, ReportParamsDTO intended) {
        validateInterval(intended);
        super.validateUpdate(current, intended);
    }

    void validateInterval(ReportParamsDTO reportParamsDTO) {

        switch (reportParamsDTO.getIntervalCategory()) {
        case BY_TIME: {
            if (reportParamsDTO.getInterval() == null) {
                LOGGER.error("Interval is required for intervalCategory BY TIME");
                throw new T9tException(T9tRepException.BAD_INTERVAL, "need interval and factor");
            }
            reportParamsDTO.setIntervalDays(null);
            reportParamsDTO.setIntervalSeconds(null);
            reportParamsDTO.setFromDate(null);
            reportParamsDTO.setToDate(null);
            break;
        }
        case BY_DURATION: {
            if (reportParamsDTO.getIntervalDays() == null || reportParamsDTO.getIntervalSeconds() == null) {
                LOGGER.error("Interval days and Interval Seconds is required for intervalCategory BY DURATION");
                throw new T9tException(T9tRepException.BAD_INTERVAL, "need days and seconds");
            }
            reportParamsDTO.setInterval(null);
            reportParamsDTO.setFactor(null);
            reportParamsDTO.setFromDate(null);
            reportParamsDTO.setToDate(null);
            break;
        }
        case BY_RANGE: {
            if (reportParamsDTO.getFromDate() == null || reportParamsDTO.getToDate() == null) {
                LOGGER.error("From date and To date is required for intervalCategory BY RANGE");
                throw new T9tException(T9tRepException.BAD_INTERVAL, "need from & to date");
            }
            reportParamsDTO.setIntervalDays(null);
            reportParamsDTO.setIntervalSeconds(null);
            reportParamsDTO.setInterval(null);
            reportParamsDTO.setFactor(null);
            break;
        }
        }
    }
}
