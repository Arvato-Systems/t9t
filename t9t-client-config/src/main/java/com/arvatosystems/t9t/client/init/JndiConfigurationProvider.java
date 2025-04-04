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
package com.arvatosystems.t9t.client.init;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JndiConfigurationProvider extends DefaultsConfigurationProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(JndiConfigurationProvider.class);

    private static String lookup(final Context context, final String name) throws NamingException {
        try {
            final Object val = context.lookup(name);
            if (val != null) {
                if (val instanceof String vals) {
                    LOGGER.debug("Obtained {} via JNDI as {}", name, vals);
                    return vals;
                }
                LOGGER.error("Obtained {} via JNDI, but of type {}, want a String - using default value", name, val.getClass().getCanonicalName());
                return null;
            }
        } catch (final NameNotFoundException e) {
            // not severe - warn and use the default value
            // fall trough
        }
        LOGGER.debug("Unable to find {} via JNDI - using default value", name);
        return null;
    }

    public JndiConfigurationProvider(final Context ctx)  throws NamingException {
        super(
            "JNDI",
            lookup(ctx, "t9t/port"),
            lookup(ctx, "t9t/host"),
            lookup(ctx, "t9t/rpcpath")
        );
    }
    public JndiConfigurationProvider() throws NamingException {
        this((Context)(new InitialContext()).lookup("java:comp/env"));
    }
}
