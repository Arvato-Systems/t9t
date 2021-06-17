package com.arvatosystems.t9t.base.services;

import java.util.List;
import java.util.function.BiFunction;

import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;

/**
 * Interface to be used to search an entity by ref, when the type of the data is not known.
 */
public interface IAnyKeySearchRegistry {

    /** Register a new LeanSearchRequestHandler, to resolve a ref of specific RTTI. */
    void registerLeanSearchRequest(BiFunction<RequestContext, Long, List<Description>> resolver, int rtti, String classname);

    /** Apply a search. */
    ResolveAnyRefResponse performLookup(RequestContext ctx, Long ref);
}
