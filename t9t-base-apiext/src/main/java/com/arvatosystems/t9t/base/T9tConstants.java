/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base;


/**
 * Global constants to used all over the application.
 */
public interface T9tConstants {

    /**
     * The tenant ID representing the global / default / admin tenant.
     */
    public static final String GLOBAL_TENANT_ID = "@";

    /**
     * The tenant ID representing no tenant (i.e. data to use in the tenant field when the request does not depend on a tenant).
     */
    public static final String NO_TENANT_ID = "-";

    /**
     * The user ID that shall be used for anonymous users (i.e. data to use in the user ID field when no user is logged in.)
     */
    public static final String ANONYMOUS_USER_ID = "?";

    /**
     * The user ID that shall be used for the admin to set up the system
     */
    public static final String ADMIN_USER_ID = "admin";
    public static final String ADMIN_ROLE_ID = "admin";

    /**
     * The user ID that shall be used for internal startup processes.
     */
    public static final String STARTUP_USER_ID = "$init";

    /**
     * The user ID that shall be used for internal technical processes.
     */
    public static final String TECHNICAL_USER_ID = "#";

    /**
     * An X500 authentication value if an unencrypted (and therefore always unauthenticated) connection is used.
     */
    public static final String X500_AUTH_UNENCRYPTED = "-";

    /**
     * An X500 authentication value if the peer connected using an encrypted channel, but the peer certificate did not exist or could not be verified.
     */
    public static final String X500_AUTH_UNVERIFIED = "?";

    /**
     * The maximum length of a message in the system.
     */
    public static final int MAXIMUM_MESSAGE_LENGTH = 16 * 1024 * 1024;

    /**
     * The name of the field that holds the object_ref value
     */
    public static final String OBJECT_REF_FIELD_NAME = "objectRef";

    /**
     * The name of the field that holds the tenant_id value
     */
    public static final String TENANT_ID_FIELD_NAME = "tenantId";

    /**
     * The name of the SOLR index field that holds the tenant_ref value of the outermost document
     */
    public static final String TENANT_REF_FIELD_NAME = "tenantRef";

    /**
     * The maximum return code which is considered as an "OK" response code.
     */
    public static final String UI_META_NO_ASSIGNED_VALUE = "-";

    /**
     * The maximum return code which is considered as an "OK" response code.
     */
    public static final int MAX_OK_RETURN_CODE = 99999999;

    /**
     * The maximum return code which is considered as a decline, but technically OK (i.e. does not perform a rollback).
     */
    public static int MAX_DECLINE_RETURN_CODE = 199999999;

    // some fortytwo compatibility data
    public static final Long GLOBAL_TENANT_REF42        = 1001L;
    public static final Long ADMIN_USER_REF42           = 1002L;
    public static final Long ADMIN_ROLE_REF42           = 1004L;
    public static final Long STARTUP_USER_REF42         = 1003L;
    public static final Long TECHNICAL_USER_REF42       = 1009L;
    public static final Long ANONYMOUS_USER_REF42       = 1011L;
    public static final String TENANT_REF_FIELD_NAME42  = "tenantRef";

    public static final int DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES = 3650;
    public static final long ONE_DAY_IN_MS = 1000L * 24L * 3600L;
    public static final int DEFAULT_RANDOM_PASS_LENGTH = 12;

    public static final String WILDCARD = "*";

    // bucket mode indicators - values or logically ORed together, to indicate a combination of these
    public static final Integer BUCKET_UPDATED = Integer.valueOf(1);
    public static final Integer BUCKET_CREATED = Integer.valueOf(2);
    public static final Integer BUCKET_DELETED = Integer.valueOf(4);

    public static final long DEFAULT_JVM_LOCK_TIMEOUT   = 5000L;  //JVM Lock in milliseconds

    // System property which triggers flyway SQL migration
    public static final String START_MIGRATION_PROPERTY = "t9t.run.flyway.migration";

    // MDC keys
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

    public static final String PLUGIN_API_ID_REQUEST_HANDLER = "requestHandler";  // ID for plugins implementing IRequestHandlerPlugin
    public static final String PLUGIN_API_ID_WORKFLOW_STEP   = "workflowStep";    // ID for plugins implementing IWorkflowStepPlugin
}
