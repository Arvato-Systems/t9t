package com.arvatosystems.t9t.base.be.search;

import com.arvatosystems.t9t.base.search.GenericTextSearchRequest;
import com.arvatosystems.t9t.base.search.GenericTextSearchResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class GenericTextSearchRequestHandler extends AbstractSearchRequestHandler<GenericTextSearchRequest> {
    private final ITextSearch engine = Jdp.getRequired(ITextSearch.class);

    @Override
    public GenericTextSearchResponse execute(final RequestContext ctx, final GenericTextSearchRequest rq) {
        GenericTextSearchResponse resp = new GenericTextSearchResponse();
        resp.setResults(engine.search(ctx, rq, rq.getDocumentName(), rq.getResultFieldName()));
        return resp;
    }
}
