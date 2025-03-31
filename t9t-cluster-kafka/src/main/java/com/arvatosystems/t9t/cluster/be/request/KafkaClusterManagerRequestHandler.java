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
package com.arvatosystems.t9t.cluster.be.request;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.IClusterEnvironment;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cluster.request.KafkaClusterManagerRequest;

import de.jpaw.dp.Jdp;

public class KafkaClusterManagerRequestHandler extends AbstractReadOnlyRequestHandler<KafkaClusterManagerRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaClusterManagerRequestHandler.class);

    private final IClusterEnvironment clusterEnvironment = Jdp.getRequired(IClusterEnvironment.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final KafkaClusterManagerRequest request) throws Exception {

        LOGGER.info("Received '{}' command", request.getCommand());
        switch (request.getCommand()) {
        case PAUSE:
            this.clusterEnvironment.pausePartitions();
            break;
        case RESUME:
            this.clusterEnvironment.resumePartitions();
            break;
        default:
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, request.getCommand());
        }

        return ok();
    }

}
