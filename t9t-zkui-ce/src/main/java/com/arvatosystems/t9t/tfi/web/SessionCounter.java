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
package com.arvatosystems.t9t.tfi.web;

import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Singleton;

/**
 * Counting the Session/user.
 * @author INCI02
 *
 */
@Singleton
public class SessionCounter implements HttpSessionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionListener.class);
    private static final AtomicInteger sessionCounter = new AtomicInteger(0);

    public SessionCounter() {
        LOGGER.debug("SessionCounter CONSTRUCTOR (web.xml)");
    }

    /**
     * session Created.
     * @param event HttpSessionEvent
     */
    @Override
    public void sessionCreated(HttpSessionEvent event) {
        sessionCounter.incrementAndGet();
    }

    /**
     * @param event HttpSessionEvent
     */
    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        sessionCounter.decrementAndGet();
    }

    public static int getActiveSessionNumber() {
        return sessionCounter.get();
    }

}
