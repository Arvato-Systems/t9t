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
package com.arvatosystems.t9t.base.be.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.google.common.collect.ImmutableList;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;
import de.jpaw.util.ExceptionUtil;

/** The abstract superclass of all search and export requests is responsible to perform the optional data export portion of the request,
 * as well as applying extra user related restrictions. */
public abstract class AbstractSearchBERequestHandler<DTO extends BonaPortable, TRACKING extends TrackingBase, REQUEST extends SearchRequest<DTO, TRACKING>>
  extends AbstractSearchRequestHandler<REQUEST> {
    private final List<DataWithTrackingS<DTO, TRACKING>> emptyResultList = ImmutableList.<DataWithTrackingS<DTO, TRACKING>>of();
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSearchBERequestHandler.class);

    protected ReadAllResponse<DTO, TRACKING> execute(final List<DataWithTrackingS<DTO, TRACKING>> result, final OutputSessionParameters op) {
        final ReadAllResponse<DTO, TRACKING> rs = new ReadAllResponse<>();
        LOGGER.debug("{} result size has size {}", this.getClass().getSimpleName(), result.size());

        if (op == null) {
            // fill the result
            rs.setDataList(result);
        } else {
            // push output into an outputSession (export it)
            op.setSmartMappingForDataWithTracking(Boolean.TRUE);
            try (IOutputSession outputSession = Jdp.getRequired(IOutputSession.class)) {
                final Long sinkRef = outputSession.open(op);
                for (final DataWithTrackingS<DTO, TRACKING> e : result) {
                    outputSession.store(e);
                }
                // successful close: store ref
                rs.setSinkRef(sinkRef);
                rs.setDataList(emptyResultList);
            } catch (final Exception e) {
                if (e instanceof ApplicationException ae) {
                    throw ae;
                }
                throw new ApplicationException(T9tException.GENERAL_EXCEPTION, ExceptionUtil.causeChain(e));
            }
        }
        rs.setReturnCode(0);
        return rs;
    }
}
