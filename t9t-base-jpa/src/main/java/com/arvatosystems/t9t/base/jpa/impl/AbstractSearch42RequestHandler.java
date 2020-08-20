package com.arvatosystems.t9t.base.jpa.impl;

import java.io.Serializable;

import com.arvatosystems.t9t.base.jpa.IEntityMapper42;
import com.arvatosystems.t9t.base.jpa.IResolverAnyKey42;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SearchRequest;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.jpa.BonaPersistableKey;
import de.jpaw.bonaparte.jpa.BonaPersistableTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;

/** An implementation of a search request handler which also provides count capabilities. */
public abstract class AbstractSearch42RequestHandler<
    KEY extends Serializable,
    DATA extends BonaPortable,
    TRACKING extends TrackingBase,
    RQ extends SearchRequest<DATA, TRACKING>,
    ENTITY extends BonaPersistableKey<KEY> & BonaPersistableTracking<TRACKING>
  > extends AbstractSearchRequestHandler<RQ> {

    protected ReadAllResponse<DATA, TRACKING> execute(
        final RQ request,
        final IResolverAnyKey42<KEY, TRACKING, ENTITY> resolver,
        final IEntityMapper42<KEY, DATA, TRACKING, ENTITY> mapper
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
                int actualResults = result.getDataList().size();
                if (request.getOffset() > 0 && actualResults == 0) {
                    // offset could have been exceeded number of results, must search!
                    ; // fall through, have to query again
                }
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
