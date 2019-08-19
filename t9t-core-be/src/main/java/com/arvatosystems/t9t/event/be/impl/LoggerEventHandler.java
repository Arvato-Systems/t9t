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
package com.arvatosystems.t9t.event.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.services.IEventHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.bonaparte.util.ToStringHelper;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("logger")
public class LoggerEventHandler implements IEventHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerEventHandler.class);

    @Override
    public int execute(RequestContext ctx, EventParameters eventData) {
        LOGGER.info("Event data is {}", ToStringHelper.toStringML(eventData));
        return 0;
    }
}
