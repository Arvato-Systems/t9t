/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.impl.output.camel;

import java.util.List;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Producer;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Provider;

/**
 * Class for processing all camel exports. The incoming message is dynamically routed to the endpoint specified for the name found in the header property
 * 'camelRoute'. The routes have to be configured in a properties file named 'camelEndpoints.properties'. According to the endpoint type the message is
 * eventually transformed via an implementation of {@linkplain ICamelOutMessageTransformer}, to match the expected format.
 */
@Dependent
@Named("outputCamelProcessor")
public class CamelOutputProcessor extends AbstractCamelProcessor {

    private final Provider<CamelContext> camelContext = Jdp.getProvider(CamelContext.class);
    private final List<ICamelOutMessageTransformer> transformers = Jdp.getAll(ICamelOutMessageTransformer.class);

    public void process(Exchange exchange) throws Exception {
        final String camelRoute = exchange.getIn().getHeader("camelRoute", String.class);

        Endpoint endpoint = null;

        final Boolean camelRouteIsValidCamelRoute = exchange.getIn().getHeader("camelRouteIsValidCamelRoute", Boolean.class);

        if (camelRouteIsValidCamelRoute != null && camelRouteIsValidCamelRoute) {
            // directly take endpoint from provided camel route string
            endpoint = camelContext.get().getEndpoint(camelRoute);
        } else {
            // camelRoute provided is only an ID to the actual route. Get the corresponding endpoint URI from a separate properties file
            final String camelEndpoint = getEndporintURI(camelRoute);
            // Get the endpoint for the route
             endpoint = camelContext.get().getEndpoint(camelEndpoint);
        }

        // Transform the message
        final ICamelOutMessageTransformer transformer = getTransformer(endpoint.getClass());

        if (transformer != null) {
            exchange = transformer.transformMessage(exchange, endpoint);
        }

        // Process the message via the endpoints producer

        final Producer producer = endpoint.createProducer();

        producer.start();

        producer.process(exchange);

        producer.stop();

    }

    private ICamelOutMessageTransformer getTransformer(final Class<? extends Endpoint> endpointClass) {
        if (transformers != null) {
            for (final ICamelOutMessageTransformer transformer : transformers) {
                if (transformer.forType().equals(endpointClass)) {
                    return transformer;
                }
            }
        }
        return null;
    }
}
