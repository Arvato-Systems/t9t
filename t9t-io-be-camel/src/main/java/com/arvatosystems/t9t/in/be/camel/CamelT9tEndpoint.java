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
package com.arvatosystems.t9t.in.be.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.support.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * CamelT9tEndpoint for the 't9t:*' camel component
 * To be noted: If any additional parameters should be added to the camel route (as in t9t:ROUTE?apiKey=* )
 * they have to be added here as a class variable with getters & setters.
 *
 */
public class CamelT9tEndpoint extends DefaultEndpoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(CamelT9tEndpoint.class);

    // Defines the APIKey
    private String apiKey = "";

    // CamelT9tEndpoint() { // unused empty constructor
    // }

    public CamelT9tEndpoint(final String uri, final CamelT9tComponent component, final String url) {
        super(uri, component);
        LOGGER.info("Created a t9t camel endpoint for URI {}, remaining = {}", uri, url);
    }

    @Override
    public Producer createProducer() throws Exception {
        var camelProducer = new CamelT9tProducer(this);
        return camelProducer;
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        final CamelT9tConsumer consumer = new CamelT9tConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }
}
