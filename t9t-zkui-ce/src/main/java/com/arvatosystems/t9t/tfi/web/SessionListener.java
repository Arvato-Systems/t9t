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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.SessionCleanup;
import org.zkoss.zk.ui.util.SessionInit;

/**
 * Counting the Session/user.
 * @author INCI02
 */
public class SessionListener implements SessionInit, SessionCleanup {
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionListener.class);

    public SessionListener() {
        LOGGER.debug("SessionListener CONSTRUCTOR: [{}]", this.hashCode());
    }

    @Override
    public void cleanup(Session sess) throws Exception {
        LOGGER.debug("SessionListener cleanup: [{}]", this.hashCode());
        if (ApplicationSession.isSessionValid()) {
            ApplicationSession.get().setJwt(null);
        } else {
            LOGGER.debug("Shiro session is not longer valid. Skip cleanup");
        }
    }

    @Override
    public void init(Session sess, Object request) throws Exception {
        LOGGER.info("** SessionListener init()...");
    }
}
