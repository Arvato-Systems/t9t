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
package com.arvatosystems.t9t.base.be.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/** The abstract superclass of all search and export requests is responsible to perform the optional data export portion of the request,
 * as well as applying extra user related restrictions. */
public abstract class AbstractSearchBERequestHandler<DTO extends BonaPortable, TRACKING extends TrackingBase, REQUEST extends SearchRequest<DTO, TRACKING>> extends AbstractSearchRequestHandler<REQUEST> {
    private final List<DataWithTrackingW<DTO, TRACKING>> EMPTY_RESULT_LIST = new ArrayList<DataWithTrackingW<DTO, TRACKING>>(0);
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSearchBERequestHandler.class);

    protected ReadAllResponse<DTO,TRACKING> execute(List<DataWithTrackingW<DTO,TRACKING>> result, OutputSessionParameters op) {
        ReadAllResponse<DTO, TRACKING> rs = new ReadAllResponse<DTO, TRACKING>();
        LOGGER.debug("{} result size has size {}", this.getClass().getSimpleName(), result.size());

        if (op == null) {
            // fill the result
            rs.setDataList(result);
        } else {
            // push output into an outputSession (export it)
            op.setSmartMappingForDataWithTracking(Boolean.TRUE);
            try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
                Long sinkRef = outputSession.open(op);
                for (DataWithTrackingW<DTO,TRACKING> e : result)
                    outputSession.store(e);
                // successful close: store ref
                rs.setSinkRef(sinkRef);
                rs.setDataList(EMPTY_RESULT_LIST);
            } catch (Exception e) {
                if (e instanceof ApplicationException)
                    throw (ApplicationException)e;
                throw new ApplicationException(T9tException.GENERAL_EXCEPTION, ExceptionUtil.causeChain(e));
            }
        }
        rs.setReturnCode(0);
        return rs;
    }
}
