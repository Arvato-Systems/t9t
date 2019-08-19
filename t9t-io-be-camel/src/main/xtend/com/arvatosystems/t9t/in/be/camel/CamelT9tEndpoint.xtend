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
package com.arvatosystems.t9t.in.be.camel

import de.jpaw.annotations.AddLogger
import org.apache.camel.Consumer
import org.apache.camel.Processor
import org.apache.camel.Producer
import org.apache.camel.impl.DefaultEndpoint
/**
 * CamelT9tEndpoint for the 't9t:*' camel component
 * To be noted: If any additional parameters should be added to the camel route (as in t9t:ROUTE?apiKey=* )
 * they have to be added here as a class variable with getters & setters.
 *
 */
@AddLogger
class CamelT9tEndpoint extends DefaultEndpoint {

    /*
     * Defines the APIKey
     */
    private String apiKey =""

    new() {
    }

    new(String uri, CamelT9tComponent component, String url) {
        super(uri, component);
        LOGGER.info("Created a t9t camel endpoint for URI {}, remaining = {}", uri, url)
    }

    @Deprecated
    new(String endpointUri) {
        super(endpointUri);
    }

    override Producer createProducer() throws Exception {
        var camelProducer = new CamelT9tProducer(this)
        return camelProducer
    }

    override Consumer createConsumer(Processor processor) throws Exception {
        var consumer = new CamelT9tConsumer(this, processor)
        configureConsumer(consumer)
        return consumer;
    }

    override boolean isSingleton() {
        return true;
    }

    def String getApiKey() {
        return this.apiKey
    }
    def void setApiKey(String apiKey) {
        this.apiKey = apiKey
    }
}
