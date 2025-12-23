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
package com.arvatosystems.t9t.base.services;

import java.util.Objects;

import org.slf4j.MDC;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

/**
 * Global constants to used all over the application, but values which should not be used by API callers.
 */
public final class T9tInternalConstants {
    private T9tInternalConstants() { }

    // some compatibility data
    public static final Long ADMIN_USER_REF           = 1002L;
    public static final Long ADMIN_ROLE_REF           = 1004L;
    public static final Long STARTUP_USER_REF         = 1003L;
    public static final Long TECHNICAL_USER_REF       = 1009L;
    public static final Long ANONYMOUS_USER_REF       = 1011L;
    public static final Long INITIAL_SUBSCRIBER_CONFIG_REF = 1090L;

    // data for PK creation (generateRef)
    public static final int RTTI_MESSAGE_LOG = 2;

    public static final String TABLENAME_MESSAGE_LOG = "p28_int_message";
    public static final String TABLENAME_SESSION = "p28_dat_session";

    // MDC keys
    public static final String MDC_MESSAGE_ID = "messageId";
    public static final String MDC_SESSION_REF = "sessionRef";
    public static final String MDC_PROCESS_REF = "processRef";
    public static final String MDC_REQUEST_PQON = "requestPqon";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_TENANT_ID = "tenantId";

    public static final String MDC_SSM_JOB_ID = "ssmJobId";
    public static final String MDC_IO_DATA_SINK_ID = "ioDataSinkId";

    public static final String MDC_BPMN_PROCESS = "bpmnProcess";
    public static final String MDC_BPMN_PROCESS_INSTANCE = "bpmnProcessInstance";
    public static final String MDC_BPMN_STEP = "bpmnStep";
    public static final String EMPTY_JWT = "N/A";

    /** Initializes basic fields of a new MDC. */
    public static void initMDC(final JwtInfo jwtInfo) {
        // Clear all old MDC data, since a completely new request is now processed
        MDC.put(T9tInternalConstants.MDC_USER_ID, jwtInfo.getUserId());
        MDC.put(T9tInternalConstants.MDC_TENANT_ID, jwtInfo.getTenantId());
        MDC.put(T9tInternalConstants.MDC_SESSION_REF, Objects.toString(jwtInfo.getSessionRef(), null));
    }
}
