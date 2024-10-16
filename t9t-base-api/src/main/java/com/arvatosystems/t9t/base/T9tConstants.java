/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
public final class T9tConstants {
    private T9tConstants() { }

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
     * The maximum return code which is considered as an "OK" response code.
     */
    public static final String UI_META_NO_ASSIGNED_VALUE = "-";

    // offsets / ranges for t9t related exception codes: There are 1 k numbers reserved per module
    public static final int EXCEPTION_OFFSET_BASE           = 20_000;   // The range for t9t-base exception codes starts here
    public static final int EXCEPTION_OFFSET_AUTH           = 21_000;   // The range for t9t-auth exception codes starts here
    public static final int EXCEPTION_OFFSET_SSM            = 22_000;   // The range for t9t-ssm exception codes starts here
    public static final int EXCEPTION_OFFSET_REP            = 23_000;   // The range for t9t-rep exception codes starts here
    public static final int EXCEPTION_OFFSET_DOC_EXT        = 24_000;   // The range for t9t-doc exception codes starts here - external API
    public static final int EXCEPTION_OFFSET_JWT            = 25_000;   // The range for t9t-jwt exception codes starts here
    public static final int EXCEPTION_OFFSET_BPMN           = 26_000;   // The range for t9t-bpmn exception codes starts here
    public static final int EXCEPTION_OFFSET_BPMN2          = 27_000;   // The range for t9t-bpmn2 exception codes starts here
    public static final int EXCEPTION_OFFSET_IO             = 28_000;   // The range for t9t-io exception codes starts here
    public static final int EXCEPTION_OFFSET_EMAIL          = 29_000;   // The range for t9t-email exception codes starts here
    public static final int EXCEPTION_OFFSET_CORE           = 30_000;   // The range for t9t-core exception codes starts here
    public static final int EXCEPTION_OFFSET_DOC            = 34_000;   // The range for t9t-doc exception codes starts here - internal (admin) API
    public static final int EXCEPTION_OFFSET_VOICE          = 41_000;   // The range for t9t-voice exception codes starts here

    public static final int EXCEPTION_OFFSET_VDB            = 50_000;   // The range for t9t-vdb exception codes starts here (vector DB exceptions)
    public static final int EXCEPTION_OFFSET_VDB_PGVECTOR   = 51_000;   // The range for t9t-vdb exception codes related to Postgres pgvector ext. starts here
    public static final int EXCEPTION_OFFSET_VDB_PINECONE   = 52_000;   // The range for t9t-vdb exception codes related to Pinecone starts here
    public static final int EXCEPTION_OFFSET_VDB_QDRANT     = 53_000;   // The range for t9t-vdb exception codes related to Qdrant starts here

    public static final int EXCEPTION_OFFSET_AI             = 60_000;   // The range for t9t-ai exception codes starts here (generic AI exceptions)
    public static final int EXCEPTION_OFFSET_LANGCHAIN      = 61_000;   // The range for langchain related exception codes starts here
    public static final int EXCEPTION_OFFSET_OPENAI         = 62_000;   // The range for t9t-openai exception codes starts here (OpenAI specific exceptions)
    public static final int EXCEPTION_OFFSET_OLLAMA         = 63_000;   // The range for t9t-ollama exception codes starts here (Ollama specific exceptions)

    /**
     * The maximum return code which is considered as an "OK" response code.
     */
    public static final int MAX_OK_RETURN_CODE = 99999999;

    /**
     * The maximum return code which is considered as a decline, but technically OK (i.e. does not perform a rollback).
     */
    public static final int MAX_DECLINE_RETURN_CODE = 199999999;

    public static final int DEFAULT_MAXIUM_NUMBER_OF_DAYS_IN_BETWEEN_USER_ACTIVITIES = 3650;
    public static final long ONE_DAY_IN_S  = 24L * 3600L;
    public static final long ONE_DAY_IN_MS = 1000L * 24L * 3600L;
    public static final int DEFAULT_RANDOM_PASS_LENGTH = 12;

    public static final String WILDCARD = "*";

    // bucket mode indicators - values or logically ORed together, to indicate a combination of these
    public static final Integer BUCKET_UPDATED = Integer.valueOf(1);
    public static final Integer BUCKET_CREATED = Integer.valueOf(2);
    public static final Integer BUCKET_DELETED = Integer.valueOf(4);

    public static final long DEFAULT_JVM_LOCK_TIMEOUT   = 5000L;  //JVM Lock in milliseconds

    public static final String MEDIA_DATA_Z_KEY_FILENAME = "filename";             // an optional z-field entry where a source file name is stored

    public static final String CFG_FILE_KEY_ENVIRONMENT_TEXT = "environment";      // the description of the environment (PRODUCTION / INTEGRATION / UAT / DEV)
    public static final String CFG_FILE_KEY_ENVIRONMENT_CSS  = "environment_css";  // the CSS class to use for the display (in case of non default)

    // System property which triggers flyway SQL migration
    public static final String START_MIGRATION_PROPERTY = "t9t.run.flyway.migration";

    // qualifier to use when getting alternate DB connection
    public static final String QUALIFIER_JDBC_SECONDARY = "JDBC2";

    // default name of the kafka topic for single-tenant request transmissions
    public static final String DEFAULT_KAFKA_TOPIC_SINGLE_TENANT_REQUESTS = "t9tRequestTopic";
    public static final String DEFAULT_KAFKA_REQUESTS_GROUP_ID            = "t9tRequestGroup";
    public static final int DEFAULT_KAFKA_PARTITION_COUNT_SMALL           = 12;
    public static final int DEFAULT_KAFKA_PARTITION_COUNT_LARGE           = 60;

    public static final String PLUGIN_API_ID_REQUEST_HANDLER = "requestHandler";    // ID for plugins implementing IRequestHandlerPlugin
    public static final String PLUGIN_API_ID_WORKFLOW_STEP   = "workflowStep";      // ID for plugins implementing IWorkflowStepPlugin

    public static final String DATA_SINK_ID_UI_EXPORT = "UIExport";
    public static final String DOCUMENT_ID_UI_EXPORT  = "UIExportEmail";

    public static final Integer SCHEDULER_RUN_ON_ALL_NODES = Integer.valueOf(411);  // l33t for All

    /**
     * Key for the zMap of a MediaData to put/get the assigned attachment name.
     */
    public static final String DOC_MEDIA_ATTACHMENT_NAME = "attachmentName";

    public static final String HTTP_HEADER_FORWARDED_FOR    = "X-Forwarded-For";    // The header used to obtain the client's IP behind a reverse proxy
    public static final String HTTP_HEADER_IDEMPOTENCY_KEY  = "Idempotency-Key";    // The header used to provide a unique request ID
    public static final String HTTP_HEADER_CONTENT_TYPE     = "Content-Type";       // Just for code which does not have a jakarta-rs API dependency
    public static final String HTTP_HEADER_ACCEPT           = "Accept";             // Just for code which does not have a jakarta-rs API dependency
    public static final String HTTP_HEADER_CHARSET          = "Charset";            // Just for code which does not have a jakarta-rs API dependency
    public static final String HTTP_HEADER_ACCEPT_CHARSET   = "Accept-Charset";     // Just for code which does not have a jakarta-rs API dependency
    public static final String HTTP_HEADER_CONTENT_ENCODING = "Content-Encoding";   // Header needed to specify compressed data
    public static final String HTTP_HEADER_AUTH             = "Authorization";      // Authentication header
    public static final String HTTP_ENCODING_GZIPPED        = "gzip";               // The encoding used for compression
    public static final String HTTP_CHARSET_UTF8            = "utf-8";              // The default charset we prefer
    public static final String HTTP_MULTIPART_FD_PREFIX     = "multipart/form-data; boundary=";        // prefix for content type in case of multipart form-data
    public static final String HTTP_MULTIPART_FD_DISPOSITION = "Content-Disposition: form-data; name="; // prefix for content disposition in case of multipart
    public static final String HTTP_WWW_FORM_URLENCODED     = "application/x-www-form-urlencoded";      // MIME type for form encoded data

    public static final String HTTP_AUTH_PREFIX_JWT         = "Bearer ";            // prefix of the Authorization header to transmit a JWT
    public static final String HTTP_AUTH_PREFIX_API_KEY     = "API-Key ";           // prefix of the Authorization header to transmit an API key
    public static final String HTTP_AUTH_PREFIX_USER_PW     = "Basic ";             // prefix of the Authorization header to transmit basic authentication

    public static final int HTTP_STATUS_INTERNAL_TIMEOUT    = 908;                  // we use this internal code to indicate we aborted a send attempt
    public static final int HTTP_STATUS_INTERNAL_EXCEPTION  = 998;                  // we use this internal code to indicate an uncaught exception while sending
    public static final int HTTP_STATUS_ILE_OTHER_EXCEPTION = 999;                  // uncaught exception (outer catch - should never happen, try catch missing)

    public static final int RESET_PASSWORD_VALIDITY         = 2; // in hours
    public static final int RESET_PASSWORD_REQUEST_LIMIT    = 1; // in minutes
}
