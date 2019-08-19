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
package com.arvatosystems.t9t.auth.be.impl;

import com.arvatosystems.t9t.auth.SessionDTO
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.auth.AuthenticationRequest
import com.arvatosystems.t9t.base.auth.AuthenticationResponse
import com.arvatosystems.t9t.base.services.IRefGenerator
import com.arvatosystems.t9t.server.ExecutionSummary
import com.arvatosystems.t9t.server.InternalHeaderParameters
import com.arvatosystems.t9t.server.services.IAuthenticate
import com.arvatosystems.t9t.server.services.IRequestLogger
import com.arvatosystems.t9t.server.services.IRequestProcessor
import de.jpaw.annotations.AddLogger
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import de.jpaw.bonaparte.pojos.api.auth.Permissionset
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.util.ApplicationException
import java.util.UUID
import org.joda.time.Instant

@AddLogger
@Singleton
class Authentication implements IAuthenticate {
    public static final int RTTI_MESSAGE_LOG = 2;       // defined separately because we cannot reference that class due to module isolation
    private static final Permissionset EXEC_PERMISSIONS = Permissionset.ofTokens(OperationType.EXECUTE)

    @Inject IRefGenerator           refGenerator
    @Inject IRequestProcessor       requestProcessor
    @Inject IRequestLogger          messageLogger

    def protected createInternalHeaderParametersForLogin(AuthenticationRequest rq) {
        val now = new Instant
        // create an internal JWT structure for the login process
        val jwt = new JwtInfo => [
            issuedAt            = now
            userId              = T9tConstants.ANONYMOUS_USER_ID
            userRef             = T9tConstants.ANONYMOUS_USER_REF42
            tenantId            = T9tConstants.GLOBAL_TENANT_ID
            tenantRef           = T9tConstants.GLOBAL_TENANT_REF42
            sessionRef          = refGenerator.generateRef(SessionDTO.class$rtti)
            sessionId           = UUID.randomUUID
            logLevel            = UserLogLevelType.MESSAGE_ENTRY
            logLevelErrors      = UserLogLevelType.MESSAGE_ENTRY
            resource            = AuthenticationRequest.BClass.INSTANCE.pqon
            resourceIsWildcard  = Boolean.TRUE
            permissionsMin      = EXEC_PERMISSIONS
            permissionsMax      = EXEC_PERMISSIONS
        ]
        return new InternalHeaderParameters => [
            executionStartedAt  = now
            encodedJwt          = "N/A"
            jwtInfo             = jwt
            processRef          = refGenerator.generateRef(RTTI_MESSAGE_LOG)
            languageCode        = jwtInfo.locale
            requestParameterPqon= rq.ret$PQON
            freeze
        ]
    }


    /** entry point to login requests which do not come via the regular /rpc path and therefore are not routed through the RequestProcessor class.
     * Since we need to do database I/O, a temporary request context is required. For security reasons, that one must be set up with minimal permissions.
     */
    override login(AuthenticationRequest rq) {

        rq.validate  // among other things, checks that ap is not null
        // structure seems to be OK here

        val ihdr =  rq.createInternalHeaderParametersForLogin

        // skip authorization since the IHDR is an artificial one anyway.
        val resp = requestProcessor.executeSynchronousAndCheckResult(rq, ihdr, AuthenticationResponse, true)

        val endOfProcessing         = new Instant

        // for logging, use the new Jwt, in case it exists
        val jwtInfo                 = if (resp.returnCode == 0) resp.jwtInfo else ihdr.jwtInfo  // FIXME: use resp.jwtinfo in case of success
        val processingDuration      = endOfProcessing.millis - ihdr.executionStartedAt.millis
        resp.tenantId               = ihdr.jwtInfo.tenantId
        resp.processRef             = ihdr.processRef

        val logLevel = if (ApplicationException.isOk(resp.returnCode))
            jwtInfo.logLevel ?: UserLogLevelType.MESSAGE_ENTRY
        else
            jwtInfo.logLevelErrors ?: jwtInfo.logLevel ?: UserLogLevelType.MESSAGE_ENTRY

        val logged = logLevel.ordinal >= UserLogLevelType.MESSAGE_ENTRY.ordinal
        val returnCodeAsString = if (ApplicationException.isOk(resp.returnCode)) "OK" else ApplicationException.codeToString(resp.returnCode)

        LOGGER.info("Finished request {}@{}:{} with return code {} ({}) in {} ms {}, S/R = {}/{} ({})",
            jwtInfo.userId, jwtInfo.tenantId, rq.ret$PQON, resp.returnCode,
            resp.errorDetails ?: "",
            processingDuration, if (logged) "(LOGGED)" else "(unlogged)",
            jwtInfo.sessionRef, ihdr.processRef, returnCodeAsString
        )

        if (logged) {
            // prepare the message for transition
            val summary = new ExecutionSummary => [
                processingTimeInMillisecs   = processingDuration
                returnCode                  = resp.returnCode
                errorDetails                = resp.errorDetails
            ]
            messageLogger.logRequest(ihdr, summary, null, null)  // always null content, do not log keys or passwords!
        }

        return resp;
    }
}
