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
package com.arvatosystems.t9t.embedded.connect

import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.jdp.Init
import de.jpaw.annotations.AddLogger
import java.util.UUID

@AddLogger
class Connection extends AbstractConnection {

    private static final Object DUMMY_INITIALIZER   = {
        val cfgFile = System.getProperty("T9T_TESTS_EMBEDDED_CFGFILE")
        ConfigProvider.readConfiguration(cfgFile);        // update a possible new location of the config file before we run the startup process
        Init.initializeT9t
        return null
    }

    def protected static getInitialPassword() {
        return System.getProperty("t9t.password") ?: System.getenv("PASSWORD") ?: INITIAL_PASSWORD
    }

    new() {
        auth(INITIAL_USER_ID, getInitialPassword)
    }

    new(UUID apiKey) {
        auth(apiKey)
    }
}
