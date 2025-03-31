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
package com.arvatosystems.t9t.base.jpa.impl;

import java.io.Serializable;

import com.arvatosystems.t9t.base.jpa.IEntityMapper;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** An implementation of a search request handler which also provides count capabilities. */
public abstract class AbstractSearchWithTotalsRequestHandler<
  KEY extends Serializable,
  DATA extends BonaPortable,
  TRACKING extends TrackingBase,
  RQ extends SearchRequest<DATA, TRACKING>,
  ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
> extends AbstractSearchRequestHandler<RQ> {

    protected ReadAllResponse<DATA, TRACKING> execute(
        final RequestContext ctx,
        final RQ request,
        final IResolverAnyKey<KEY, TRACKING, ENTITY> resolver,
        final IEntityMapper<KEY, DATA, TRACKING, ENTITY> mapper
    ) throws Exception {
        mapper.processSearchPrefixForDB(request);
        final ReadAllResponse<DATA, TRACKING> result = mapper.createReadAllResponse(resolver.search(request, null), request.getSearchOutputTarget());
        if (Boolean.TRUE.equals(request.getCountTotals())) {
            if (request.getSearchOutputTarget() != null) {
                if (result.getNumResults() != null) {
                    // data export, and result size already set
                    return result;  // we are done
                }
            } else {
                final int actualResults = result.getDataList().size();
//                if (request.getOffset() > 0 && actualResults == 0) {
//                    // offset could have been exceeded number of results, must search!
//                     // fall through, have to query again
//                }
                if (request.getLimit() == 0 || actualResults < request.getLimit()) {
                    // all results are known due to query with untruncated result set
                    result.setNumResults(Long.valueOf(actualResults + request.getOffset()));
                    return result;  // we are done
                }
                // fall through - must do another query
            }
            result.setNumResults(resolver.count(request.getSearchFilter(), request.getApplyDistinct()));
        }
        return result;
    }
}
