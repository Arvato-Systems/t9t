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
