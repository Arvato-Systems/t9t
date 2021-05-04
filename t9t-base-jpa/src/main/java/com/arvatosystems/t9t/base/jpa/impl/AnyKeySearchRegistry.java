package com.arvatosystems.t9t.base.jpa.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.jpa.IAnyKeySearchRegistry;
import com.arvatosystems.t9t.base.search.Description;
import com.arvatosystems.t9t.base.search.ResolveAnyRefResponse;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Singleton;

@Singleton
public class AnyKeySearchRegistry implements IAnyKeySearchRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnyKeySearchRegistry.class);

    private static final Map<Integer, String> classnameByRtti = new ConcurrentHashMap<>(50);
    private static final Map<Integer, BiFunction<RequestContext, Long, List<Description>>> resolverByRtti = new ConcurrentHashMap<>(50);

    @Override
    public void registerLeanSearchRequest(BiFunction<RequestContext, Long, List<Description>> resolver, int rtti, String classname) {
        if (rtti <= 0) {
            LOGGER.error("Cannot register resolver for rtti <= 0: {}", classname);
            return;
        }
        final String theClassname = classname.endsWith("Entity") ? classname.substring(0, classname.length() - 6) : classname;
        final Integer rttiObject = Integer.valueOf(rtti);  // ensure a single instance is used for both maps, do not autobox twice
        final String previousClassname = classnameByRtti.put(rttiObject, theClassname);
        if (previousClassname != null) {
            LOGGER.error("RTTI used twice: {} for {} and {}", rttiObject, theClassname, previousClassname);
        }
        resolverByRtti.put(rttiObject, resolver);
        LOGGER.debug("Registered resolver for {} by RTTI {}", theClassname, rttiObject);
    }

    @Override
    public ResolveAnyRefResponse performLookup(RequestContext ctx, Long ref) {
        final Integer rtti = (int)(ref % 10000L);
        final BiFunction<RequestContext, Long, List<Description>> resolver = resolverByRtti.get(rtti);
        final ResolveAnyRefResponse resp = new ResolveAnyRefResponse();
        resp.setEntityClass(classnameByRtti.get(rtti)); 
        if (resolver != null) {
            // it is possible to find a description
            List<Description> descs = resolver.apply(ctx, ref);
            if (!descs.isEmpty()) {
                for (Description desc : descs) {
                    if (desc.getObjectRef().equals(ref)) {
                        resp.setDescription(desc);
                        break;
                    }
                }
            }
        }
        return resp;
    }

}
