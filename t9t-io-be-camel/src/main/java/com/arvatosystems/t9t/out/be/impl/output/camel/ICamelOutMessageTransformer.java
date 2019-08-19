/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

/**
 * Interface for defining an endpoint specific transformer for camel messages.
 */
public interface ICamelOutMessageTransformer {

    /**
     * Method for processing the transformation.
     *
     * @param exchange
     *            incoming exchange
     * @param endpoint
     *            endpoint the message should be send to
     * @return the modified exchange
     */
    public Exchange transformMessage(Exchange exchange, Endpoint endpoint);

    /**
     * Type of endpoint this transformer is used for
     *
     * @return endpoint type
     */
    public Class<? extends Endpoint> forType();

}
