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
package com.arvatosystems.t9t.client.init;

import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractJndiConfigurationProvider extends AbstractConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiConfigurationProvider.class);

    private static final String lookup(Context context, String name) throws NamingException {
        try {
            Object val = context.lookup(name);
            if (val != null) {
                if (val instanceof String) {
                    String vals = (String)val;
                    LOGGER.debug("Obtained {} via JNDI as {}", name, vals);
                    return vals;
                }
                LOGGER.error("Obtained {} via JNDI, but of type {}, want a String - using default value", name, val.getClass().getCanonicalName());
                return null;
            }
        } catch (NameNotFoundException e) {
            // not severe - warn and use the default value
            // fall trough
        }
        LOGGER.debug("Unable to find {} via JNDI - using default value", name);
        return null;
    }

    protected AbstractJndiConfigurationProvider(Context ctx) throws NamingException {
        super(
          "JNDI",
          lookup(ctx, "t9t/port"),
          lookup(ctx, "t9t/host"),
          lookup(ctx, "t9t/rpcpath"),
          lookup(ctx, "t9t/authpath")
        );
    }
}
