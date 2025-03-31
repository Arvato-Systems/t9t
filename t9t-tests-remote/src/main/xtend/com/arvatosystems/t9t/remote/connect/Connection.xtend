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
package com.arvatosystems.t9t.remote.connect

import com.arvatosystems.t9t.authc.api.LogoutRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.core.BonaPortable
import de.jpaw.bonaparte.sock.HttpPostClient
import de.jpaw.bonaparte.util.impl.RecordMarshallerBonaparte
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity
import de.jpaw.bonaparte.util.impl.RecordMarshallerJson
import java.util.UUID
import com.arvatosystems.t9t.base.T9tConstants

/** Remote connection via http. */
@AddLogger
class Connection extends AbstractConnection {
    public static final ConnectionTypes HOW_TO_CONNECT = ConnectionTypes.COMPACT_BONAPARTE;

    // logical configuration

    final String baseUrl;
    final HttpPostClient dlg;

    def private static final String defaultUrl() {
        val myProtocol = if ("443" == initialPort) "https" else "http"
        return '''«myProtocol»://«initialHost»:«initialPort»'''
    }

    // Java: default parameters PLEEEEEAASSE!

    // convenience method for manual override
    new() {
        this(defaultUrl, true, false, HOW_TO_CONNECT)
    }

    new(UUID initialUUID) {
        this(defaultUrl, true, false, HOW_TO_CONNECT, initialUUID, null, null, null)
    }

    new(String url, UUID initialUUID) {
        this(url, true, false, HOW_TO_CONNECT, initialUUID, null, null, null)
    }

    new(String userId, String password) {
        this(defaultUrl, true, false, HOW_TO_CONNECT, null, userId, password, null)
    }

    new(String url, String userId, String password, UUID apiKey) {
        this(url, true, false, HOW_TO_CONNECT, apiKey, userId, password, null)
    }

    new(String userId, String password, String newPassword) {
        this(defaultUrl, true, false, HOW_TO_CONNECT, null, userId, password, newPassword)
    }

    // false logs less then the no args constructor, true more
    new(boolean differentLog) {
        this(defaultUrl, differentLog, differentLog, HOW_TO_CONNECT)
    }

    // false logs less then the no args constructor, true more
    new(boolean differentLog, ConnectionTypes connectionType) {
        this(defaultUrl, differentLog, differentLog, connectionType)
    }

    new(String url, boolean logSizes, boolean logMessage, ConnectionTypes connectionType) {
        this(url, logSizes, logMessage, connectionType, null, null, null, null)
    }

    new(String url, boolean logSizes, boolean logMessage, ConnectionTypes connectionType, UUID initialUUID,
            String userId, String password, String newPassword) {

        LOGGER.info("Connecting to {}", url)
        baseUrl = url;

        switch (connectionType) {
        case BONAPARTE:
            dlg = new HttpPostClient(url, false, logSizes, logMessage, false, new RecordMarshallerBonaparte())
        case COMPACT_BONAPARTE:
            dlg = new HttpPostClient(url, false, logSizes, false, logMessage, new RecordMarshallerCompactBonaparteIdentity())
        case JSON:
            dlg = new HttpPostClient(url, false, logSizes, logMessage, false, new RecordMarshallerJson())
        default: {
            dlg = null
            throw new Exception('''format «connectionType?.name() ?: "NULL"» not supported by this client''')
            }
        }

        // authenticate and obtain the token
        dlg.baseUrl = baseUrl + "/login"

        var AuthenticationResponse authResult
        if (initialUUID === null && userId === null) {
            authResult = auth(INITIAL_USER_ID, getInitialPassword())
        } else if (initialUUID === null && userId !== null) {
            authResult = if (newPassword === null) auth(userId, password) else changePassword(userId, password, newPassword)
        } else {
            authResult = auth(initialUUID)
        }

        dlg.authentication = T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.encodedJwt  // the encoded token
        dlg.baseUrl = baseUrl + "/rpc"
    }

    override void switchUser(UUID newApiKey) {
        dlg.baseUrl = baseUrl + "/login"
        val authResult = if (newApiKey === null) auth(INITIAL_USER_ID, initialPassword) else auth(newApiKey)
        dlg.authentication = T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.encodedJwt  // the encoded token
        dlg.baseUrl = baseUrl + "/rpc"
    }

    override void switchUser(String userId, String password) {
        dlg.baseUrl = baseUrl + "/login"
        val authResult = auth(userId, password)
        dlg.authentication = T9tConstants.HTTP_AUTH_PREFIX_JWT + authResult.encodedJwt
        dlg.baseUrl = baseUrl + "/rpc"
    }


    // just a proxy
    override BonaPortable doIO(BonaPortable rp) {
        return dlg.doIO(rp)
    }

    override void logout() {
        LOGGER.info("Logging out")
        dlg.baseUrl = baseUrl + "/logout"
        dlg.doIO(new LogoutRequest)
    }

    // methods not required for normal operation, but for esting of specific scenarios
    override setAuthentication(String header) {
        dlg.authentication = header
    }

    def setBaseUrl(String postPath) {
        dlg.baseUrl = baseUrl + postPath
    }
}
