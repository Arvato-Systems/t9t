/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import com.arvatosystems.t9t.auth.SessionDTO;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.auth.AuthenticationRequest;
import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.services.IRefGenerator;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;
import com.arvatosystems.t9t.server.services.IAuthenticate;
import com.arvatosystems.t9t.server.services.IRequestLogger;
import com.arvatosystems.t9t.server.services.IRequestProcessor;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class Authentication implements IAuthenticate {
    private static final Logger LOGGER = LoggerFactory.getLogger(Authentication.class);

    public static final int RTTI_MESSAGE_LOG = 2;
    private static final Permissionset EXEC_PERMISSIONS = Permissionset.ofTokens(OperationType.EXECUTE);

    private final IRefGenerator refGenerator = Jdp.getRequired(IRefGenerator.class);
    private final IRequestProcessor requestProcessor = Jdp.getRequired(IRequestProcessor.class);
    private final IRequestLogger messageLogger = Jdp.getRequired(IRequestLogger.class);

    /**
     * entry point to login requests which do not come via the regular /rpc path and therefore are not routed through the RequestProcessor class.
     * Since we need to do database I/O, a temporary request context is required. For security reasons, that one must be set up with minimal permissions.
     */
    @Override
    public AuthenticationResponse login(final AuthenticationRequest rq) {
        rq.validate(); // among other things, checks that ap is not null
        // structure seems to be OK here

        final InternalHeaderParameters ihdr = this.createInternalHeaderParametersForLogin(rq);
        // skip authorization since the IHDR is an artificial one anyway.
        final AuthenticationResponse resp = this.requestProcessor.<AuthenticationResponse>executeSynchronousAndCheckResult(rq, ihdr,
                AuthenticationResponse.class, true);

        final Instant endOfProcessing = Instant.now();

        // for logging, use the new Jwt, in case it exists
        final JwtInfo jwtInfo = resp.getReturnCode() == 0 ? resp.getJwtInfo() : ihdr.getJwtInfo(); // FIXME: use resp.jwtinfo in case of success
        final long processingDuration = endOfProcessing.toEpochMilli() - ihdr.getExecutionStartedAt().toEpochMilli();
        resp.setTenantId(ihdr.getJwtInfo().getTenantId());
        resp.setProcessRef(ihdr.getProcessRef());

        UserLogLevelType logLevel = null;

        if (ApplicationException.isOk(resp.getReturnCode())) {
            logLevel = jwtInfo.getLogLevel() == null ? UserLogLevelType.MESSAGE_ENTRY : jwtInfo.getLogLevel();
        } else {
            logLevel = jwtInfo.getLogLevelErrors() == null ? UserLogLevelType.MESSAGE_ENTRY : jwtInfo.getLogLevelErrors(); // original was taken from logLevel
                                                                                                                           // and it looks like a bug
        }

        final boolean logged = logLevel.ordinal() >= UserLogLevelType.MESSAGE_ENTRY.ordinal();
        final String returnCodeAsString = ApplicationException.isOk(resp.getReturnCode()) ? "OK" : ApplicationException.codeToString(resp.getReturnCode());
        LOGGER.info("Finished request {}@{}:{} with return code {} ({}) in {} ms {}, S/R = {}/{} ({})", jwtInfo.getUserId(), jwtInfo.getTenantId(),
                rq.ret$PQON(), resp.getReturnCode(), resp.getErrorDetails() == null ? "" : resp.getErrorDetails(), processingDuration,
                logged ? "(LOGGED)" : "(unlogged)", jwtInfo.getSessionRef(), ihdr.getProcessRef(), returnCodeAsString);

        if (logged) {
            // prepare the message for transition
            ExecutionSummary summary = new ExecutionSummary();
            summary.setProcessingTimeInMillisecs(processingDuration);
            summary.setReturnCode(resp.getReturnCode());
            summary.setErrorDetails(resp.getErrorDetails());
            messageLogger.logRequest(ihdr, summary, null, null); // always null content, do not log keys or passwords!
        }
        return resp;
    }

    protected InternalHeaderParameters createInternalHeaderParametersForLogin(final AuthenticationRequest rq) {
        final Instant now = Instant.now();
        // create an internal JWT structure for the login process
        final JwtInfo jwtInfo = new JwtInfo();
        jwtInfo.setIssuedAt(now);
        jwtInfo.setUserId(T9tConstants.ANONYMOUS_USER_ID);
        jwtInfo.setUserRef(T9tConstants.ANONYMOUS_USER_REF);
        jwtInfo.setTenantId(T9tConstants.GLOBAL_TENANT_ID);
        jwtInfo.setSessionRef(Long.valueOf(this.refGenerator.generateRef(SessionDTO.class$rtti())));
        jwtInfo.setSessionId(UUID.randomUUID());
        jwtInfo.setLogLevel(UserLogLevelType.MESSAGE_ENTRY);
        jwtInfo.setLogLevelErrors(UserLogLevelType.MESSAGE_ENTRY);
        jwtInfo.setResource(AuthenticationRequest.BClass.INSTANCE.getPqon());
        jwtInfo.setResourceIsWildcard(Boolean.TRUE);
        jwtInfo.setPermissionsMin(EXEC_PERMISSIONS);
        jwtInfo.setPermissionsMax(EXEC_PERMISSIONS);
        final InternalHeaderParameters internalHeaderParameters = new InternalHeaderParameters();
        internalHeaderParameters.setExecutionStartedAt(now);
        internalHeaderParameters.setEncodedJwt("N/A");
        internalHeaderParameters.setJwtInfo(jwtInfo);
        internalHeaderParameters.setProcessRef(Long.valueOf(this.refGenerator.generateRef(Authentication.RTTI_MESSAGE_LOG)));
        internalHeaderParameters.setLanguageCode(internalHeaderParameters.getJwtInfo().getLocale());
        internalHeaderParameters.setRequestParameterPqon(rq.ret$PQON());
        internalHeaderParameters.freeze();
        return internalHeaderParameters;
    }
}
