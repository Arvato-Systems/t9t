package com.arvatosystems.t9t.in.be.camel.k8s;

import org.apache.camel.CamelContext;
import org.apache.camel.component.kubernetes.cluster.KubernetesClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.io.be.camel.service.impl.CamelService;

import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class CamelKubernetesService extends CamelService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CamelKubernetesService.class);

    @Override
    public void initializeClusterService(final CamelContext camelContext) {
        try {
            KubernetesClusterService kubernetesClusterService = new KubernetesClusterService();
            camelContext.addService(kubernetesClusterService);
        } catch (final Exception e) {
            LOGGER.error("There was a problem initializing cluster service due to ", e);
        }
    }
}
