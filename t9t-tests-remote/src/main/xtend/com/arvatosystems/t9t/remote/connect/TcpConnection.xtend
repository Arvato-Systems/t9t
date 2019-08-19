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
package com.arvatosystems.t9t.remote.connect

import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.core.ByteArrayComposer
import de.jpaw.bonaparte.core.ByteArrayParser
import java.util.UUID

@AddLogger
class TcpConnection extends AbstractConnection {

    final SimpleTcpClient dlg

    // convenience method for manual override
    new() {
        this(initialHost, initialPortTcp, true, false)
    }

    // false logs less then the no args constructor, true more
    new(boolean differentLog) {
        this(initialHost, initialPortTcp, differentLog, differentLog)
    }

    new(String host, String port, boolean logSizes, boolean logMessage) {
        // connect
        dlg = new SimpleTcpClient(host, Integer.parseInt(port), false)
        // authenticate and obtain the token
        auth(INITIAL_USER_ID, initialPassword)
    }


    override void switchUser(UUID newApiKey) {
        auth(newApiKey)
    }

    override void switchUser(String userId, String password) {
        auth(userId, password)
    }

    override final BonaPortable doIO(BonaPortable rp) {
        val cbac = new ByteArrayComposer
        cbac.writeRecord(rp)
        val rs = dlg.doRawIO(cbac.buffer, 0, cbac.length)
        val bap = new ByteArrayParser(rs.currentBuffer, 0, rs.length)
        return bap.readRecord()
    }

    override void logout() {
        LOGGER.info("Logging out")
        dlg.close
    }

    // methods not required for normal operation, but for esting of specific scenarios
    override setAuthentication(String header) {
        // no op - this is a stateful connection
    }
}
