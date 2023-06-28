/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.jpa.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.SimpleCallOutExecutor;
import com.arvatosystems.t9t.base.services.IForeignRequest;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.UplinkConfiguration;
import com.arvatosystems.t9t.io.OutboundMessageDTO;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.StoreSinkRequest;
import com.arvatosystems.t9t.out.jpa.impl.OutPersistenceAccess;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import de.jpaw.util.ApplicationException;

@Specializes
@Singleton
public class ProxyOutPersistenceAccess extends OutPersistenceAccess {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyOutPersistenceAccess.class);
    private static final String UPLINK_KEY_MAIN = "SERVER-MAIN";

    private final IForeignRequest remoteCaller;
    private Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    /** Constructor to initialize the callout executor. */
    public ProxyOutPersistenceAccess() {
        final UplinkConfiguration mainServerConfig = ConfigProvider.getUplinkOrThrow(UPLINK_KEY_MAIN);
        remoteCaller = SimpleCallOutExecutor.createCachedExecutor(UPLINK_KEY_MAIN, mainServerConfig.getUrl());
    }

//    /** Returns a new key for a sink. This implementation uses JDBC access to do it. */
//    @Override
//    public Long getNewSinkKey() {
//        return 0L;   // TODO: get key via JDBC
//    }

    /** Persist a sink. This implementation stores it via remote request invocation. */
    @Override
    public void storeNewSink(SinkDTO sink) {
        final StoreSinkRequest ssrq = new StoreSinkRequest();
        ssrq.setDataSink(sink);
        final ServiceResponse remoteResponse = remoteCaller.execute(ctxProvider.get(), ssrq);
        if (!ApplicationException.isOk(remoteResponse.getReturnCode())) {
            throw new T9tException(remoteResponse.getReturnCode(), remoteResponse.getErrorDetails());
        }
    }

    @Singleton
    public void storeOutboundMessage(OutboundMessageDTO sink) {
        // ignored. Should throw an exception instead?
    }
}
