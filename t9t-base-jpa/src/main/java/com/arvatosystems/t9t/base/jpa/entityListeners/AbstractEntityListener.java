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
package com.arvatosystems.t9t.base.jpa.entityListeners;

import java.util.concurrent.atomic.AtomicReference;

import org.joda.time.Instant;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.entities.FullTracking;
import com.arvatosystems.t9t.base.jpa.IEntityListener;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration;

import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

/** Abstract base class for entity listeners, performs injection of the context.
 *
 * @author Michael Bischoff
 *
 */
public abstract class AbstractEntityListener<T extends TrackingBase> implements IEntityListener<T> {
    private static final Long ZERO = Long.valueOf(0L);

    static private final AtomicReference<LazyReferences> refs = new AtomicReference<LazyReferences>();

    // a simpler logic would work in EclipseLink, but Hibernate creates all EntityListeners already when the EntityManagerFactory is set up,
    // and then the providers may not yet be known

    static private class LazyReferences {
        private final T9tServerConfiguration configuration = Jdp.getRequired(T9tServerConfiguration.class);
        private final Provider<RequestContext> contextProvider = Jdp.getProvider(RequestContext.class);
        private final String cutUser = cutUser(configuration.getDatabaseConfiguration().getUsername());
    }

    private static LazyReferences getRef() {
        LazyReferences myRef = refs.get();
        if (myRef != null)
            return myRef;
        // init it
        myRef = new LazyReferences();
        refs.compareAndSet(null, myRef);
        return refs.get();
    }

    public static String cutUser(String userId) {
        return userId.length() > 8 ? userId.substring(0,8) : userId;
    }

    protected final String getCutUserId() {
        return getRef().cutUser;
    }

    protected final RequestContext getRequestContext() {
        return getRef().contextProvider.get();
    }

    protected final void updateTracking(FullTracking rw, boolean clear) {
        final LazyReferences ref = getRef();
        Instant now = Instant.now();
        RequestContext ctx = ref.contextProvider.get();
        rw.setMTechUserId(ref.cutUser);
        rw.setMTimestamp(now);
        if (ctx != null) {
            rw.setMAppUserId(ctx.userId);
            rw.setMProcessRef(ctx.requestRef);
            // DTO CACHE GONE
//            if (clear) {
//                PersistenceProviderJPA jpactx = ref.jpaContextProvider.get();
//                if (jpactx != null)
//                    jpactx.dtoCache.clear();
//            }
        } else {
            rw.setMAppUserId(T9tConstants.ANONYMOUS_USER_ID);
            rw.setMProcessRef(ZERO);
        }
    }

    protected final void createTracking(FullTracking rw) {
        updateTracking(rw, false);
        rw.setCTechUserId(rw.getMTechUserId());
        rw.setCTimestamp(rw.getMTimestamp());
        rw.setCAppUserId(rw.getMAppUserId());
        rw.setCProcessRef(rw.getMProcessRef());
    }
}
