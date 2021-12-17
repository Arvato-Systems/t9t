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

import de.jpaw.util.ApplicationException;

/**
 * Container class that provides a consolidated list of exception that might occur in the scope of the t9t project.
 * This class extends the generic {@link ApplicationException} in order to provide error details
 * which are specific to all applications based on the t9t platform.
 */
public class T9tException extends ApplicationException {

    private static final long serialVersionUID = 36513359567514148L;

    /*
     * Offset for all codes in this class.
     */
    // @formatter:off
    private static final int CORE_OFFSET = 20000;
    private static final int OFFSET                     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;
    private static final int OFFSET_DENIED              = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_DENIED;
    private static final int OFFSET_TIMEOUT             = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_TIMEOUT;
    private static final int OFFSET_VALIDATION_ERROR    = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_VALIDATION_ERROR;
    private static final int OFFSET_LOGIC_ERROR         = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_INTERNAL_LOGIC_ERROR;
    private static final int OFFSET_DB_ERROR            = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_DATABASE_ERROR;
    // @formatter:on

    public static final int INVALID_CRUD_COMMAND = OFFSET + 1;
    public static final int RECORD_ALREADY_EXISTS = OFFSET + 2;
    public static final int RECORD_DOES_NOT_EXIST = OFFSET + 3;
    public static final int RECORD_INACTIVE = OFFSET + 4;
    public static final int MALFORMED_REQUEST_PARAMETER_NAME = OFFSET + 5;
    public static final int SERVICE_CLASS_NOT_FOUND = OFFSET + 6;
    public static final int INVALID_REQUEST_PARAMETER_TYPE = OFFSET + 8;
    public static final int TENANT_NOT_EXISTING = OFFSET + 10;
    public static final int TENANT_INACTIVE = OFFSET + 11;
    public static final int APP_USER_NOT_EXISTING = OFFSET + 12;
    public static final int APP_USER_INACTIVE = OFFSET + 13;
    public static final int APP_USER_TENANT_PERMISSION = OFFSET + 14;
    public static final int TOO_MANY_RECORDS = OFFSET + 15;
    public static final int CANT_OPEN_SESSION = OFFSET + 16;
    public static final int CANT_CLOSE_SESSION = OFFSET + 17;
    public static final int SESSION_OPEN_CLOSE_SEQUENCE_ERROR = OFFSET_LOGIC_ERROR + 18;
    public static final int MISSING_PARAMETER = OFFSET + 19;
    public static final int APP_USER_WRONG_AUTH_METHOD = OFFSET + 20;
    public static final int APP_USER_X500_AUTH_FAILED = OFFSET + 21;
    public static final int JTA_EXCEPTION = OFFSET_DB_ERROR + 22;
    public static final int ILLEGAL_CONFIG_ITEM = OFFSET + 23;
    public static final int ENTITY_KEY_EXCEPTION = OFFSET + 24;
    public static final int ENTITY_DATA_MAPPING_EXCEPTION = OFFSET + 25;
    public static final int RESPONSE_VALIDATION_ERROR = OFFSET_VALIDATION_ERROR + 26;
    public static final int REQUEST_VALIDATION_ERROR = OFFSET_VALIDATION_ERROR + 27;
    public static final int WRITE_ACCESS_ONLY_CURRENT_TENANT = OFFSET + 28;
    public static final int READ_ACCESS_ONLY_CURRENT_TENANT = OFFSET + 29;
    public static final int NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING = OFFSET_DENIED + 30;
    public static final int ILLEGAL_REQUEST_PARAMETER = OFFSET + 31;
    public static final int REQUEST_HANDLER_NOT_FOUND = OFFSET_LOGIC_ERROR + 32;

    public static final int UNIQUE_CONSTRAINT_VIOLATION = OFFSET + 33;
    public static final int SESSION_NOT_OPENED = OFFSET_LOGIC_ERROR + 34;
    public static final int SESSION_OPEN_ERROR = OFFSET + 35;
    public static final int REF_RESOLVER_REQUEST_PARAMETER = OFFSET_LOGIC_ERROR + 36;
    public static final int BAD_TIME_SLICE = OFFSET + 37;
    public static final int FIND_ON_NULL_KEY = OFFSET_LOGIC_ERROR + 38;
    public static final int NOT_REQUEST_PARAMETERS = OFFSET + 39;
    public static final int T9T_ACCESS_DENIED = OFFSET_DENIED + 40;
    public static final int WRITE_ACCESS_NOT_FOUND_PROBABLY_OTHER_TENANT = OFFSET + 41;
    public static final int COULD_NOT_ACQUIRE_LOCK = OFFSET_TIMEOUT + 42;
    public static final int ILLEGAL_CHARACTER = OFFSET + 43;

    public static final int STALLED_LOG_WRITER = OFFSET_TIMEOUT + 50;
    public static final int REQUEST_HANDLER_RETURNED_NULL = OFFSET_LOGIC_ERROR + 51;
    public static final int SHUTDOWN_IN_PROGRESS = OFFSET_TIMEOUT + 52;
    public static final int CANNOT_CLOSE_SINK = OFFSET_DB_ERROR + 55;

    public static final int OPTIMISTIC_LOCKING_EXCEPTION = OFFSET_DB_ERROR + 94;
    public static final int REQUEST_PARAMETER_BAD_INHERITANCE = OFFSET + 95;
    public static final int TRANSACTION_RETRY_REQUEST = OFFSET_DB_ERROR + 96;
    public static final int GENERAL_EXCEPTION_CENTRAL = OFFSET_LOGIC_ERROR + 97;
    public static final int GENERAL_EXCEPTION = OFFSET_LOGIC_ERROR + 98;
    public static final int SELECT_INSERT_SELECT_ERROR = OFFSET_LOGIC_ERROR + 99;

    // Codes specific to CrudRequests
    public static final int MISSING_KEY_PARAMETER = OFFSET + 100;
    public static final int MISSING_DATA_PARAMETER = OFFSET + 101;
    public static final int EXTRA_KEY_PARAMETER = OFFSET + 102;
    public static final int EXTRA_DATA_PARAMETER = OFFSET + 103;
    public static final int MISSING_VERSION_PARAMETER = OFFSET + 104;
    public static final int FIELD_MAY_NOT_BE_CHANGED = OFFSET + 105;
    public static final int ONLY_ONE_ACTIVE_ALLOWED = OFFSET + 106;

    // general codes concerning configuration
    public static final int MISSING_CONFIGURATION = OFFSET + 107;
    public static final int INVALID_CONFIGURATION = OFFSET + 108;
    public static final int NOT_YET_IMPLEMENTED = OFFSET + 109;

    // Codes specific to search
    public static final int UNRECOGNIZED_FILTER_CRITERIA = OFFSET + 110;
    public static final int INVALID_FILTER_PARAMETERS = OFFSET + 111;
    public static final int UNDERSPECIFIED_FILTER_PARAMETERS = OFFSET + 112;
    public static final int OVERSPECIFIED_FILTER_PARAMETERS = OFFSET + 113;

    public static final int RERUN_NOT_APPLICABLE_RET = OFFSET + 114;
    public static final int RERUN_NOT_APPLICABLE_DONE = OFFSET + 115;

    public static final int CRUD_BOTH_KEYS_MISMATCH = OFFSET + 116;
    public static final int CRUD_NATURAL_KEY_MISSING = OFFSET + 117;
    public static final int RERUN_NOT_POSSIBLE_NO_RECORDED_REQUEST = OFFSET + 118;

    public static final int NO_LONGER_SUPPORTED = OFFSET + 119;

    // Codes specific to key resolver
    public static final int RESOLVE_ACCESS = OFFSET + 120;
    public static final int RESOLVE_PARAMETER = OFFSET + 121;
    public static final int RESOLVE_BAD_CLASS = OFFSET + 122;
    public static final int ERROR_FILLING_RESTRICTION_CACHE = OFFSET + 123;

    // Codes specific to sorting
    public static final int UNRECOGNIZED_SORT_PARAMETER = OFFSET + 130;
    public static final int ENUM_MAPPING                = OFFSET + 131;
    public static final int NOT_AN_ENUM                 = OFFSET_LOGIC_ERROR + 132;
    public static final int NOT_AN_XENUM                = OFFSET_LOGIC_ERROR + 133;
    public static final int NOT_AN_ENUMSET              = OFFSET_LOGIC_ERROR + 134;
    public static final int NOT_AN_XENUMSET             = OFFSET_LOGIC_ERROR + 135;
    public static final int MALFORMATTED_FIELDNAME      = OFFSET_LOGIC_ERROR + 136;
    public static final int NOT_ENUM_INSTANCE           = OFFSET_LOGIC_ERROR + 137;
    public static final int TOO_HIGH_RESULT_SIZE_FOR_SORTING = OFFSET_LOGIC_ERROR + 138;

    // Codes specific to getting sequence numbers (artificial keys for JPA)
    public static final int JDBC_BAD_TYPE_RETURNED      = OFFSET_DB_ERROR + 150;
    public static final int JDBC_NO_RESULT_RETURNED     = OFFSET_DB_ERROR + 151;
    public static final int JDBC_GENERAL_SQL            = OFFSET_DB_ERROR + 152;
    public static final int JDBC_UNKNOWN_DIALECT        = OFFSET_DB_ERROR + 153;

    // Codes specific to security functions (authentication / authorization)
    public static final int USER_NOT_FOUND = OFFSET + 200;
    public static final int TENANT_NOT_FOUND = OFFSET + 201;

    // Authentication request handler specific
    public static final int USER_INACTIVE = OFFSET + 202;
    public static final int USER_NOT_ALLOWED_TO_ACCESS_WITH_PW = OFFSET + 203;
    public static final int USER_STATUS_NOT_FOUND = OFFSET + 204;
    public static final int ACCOUNT_TEMPORARILY_FROZEN = OFFSET + 205;
    public static final int PASSWORD_NOT_FOUND = OFFSET + 206;
    public static final int PASSWORD_EXPIRED_DUE_TO_USER_INACTIVITY = OFFSET + 207;

    /** This code is an OK code, because the login was correct and must be recorded. It is responsibility of the UI to request a new PW. */
    public static final int PASSWORD_EXPIRED = 208;

    public static final int WRONG_PASSWORD = OFFSET + 209;
    public static final int NEW_PASSWORD_MATCHES_ONE_OF_THE_LAST = OFFSET + 210;
    public static final int NEW_PASSWORD_MATCHES_ONE_AND_CANT_BE_REUSED_YET = OFFSET + 211;
    public static final int ROLE_NOT_FOUND = OFFSET + 212;
    public static final int CANNOT_RESET_PASSWORD_NO_EMAIL_SET_FOR_USER = OFFSET + 213;
    public static final int CANNOT_RESET_PASSWORD_PROVIDED_EMAIL_DOESNT_MATCH_STORED_ONE = OFFSET + 214;

    public static final int INCORRECT_RESPONSE_CLASS = OFFSET_LOGIC_ERROR + 215;
    public static final int RESTRICTED_ACCESS = OFFSET_DENIED + 216;
    public static final int ACCESS_DENIED = OFFSET_DENIED + 217;
    public static final int NO_SUITABLE_AUTHENTICATION_PROVIDER_FOUND = OFFSET_LOGIC_ERROR + 218;
    public static final int GENERAL_AUTH_PROBLEM = OFFSET + 219;


    // IO errors
    public static final int FILE_NOT_FOUND_FOR_DOWNLOAD = OFFSET + 300;
    public static final int BAD_REMOTE_RESPONSE         = OFFSET_TIMEOUT + 304;

    public static final int UPSTREAM_NULL_RESPONSE      = OFFSET_TIMEOUT + 310;
    public static final int UPSTREAM_BAD_RESPONSE       = OFFSET_TIMEOUT + 311;
    public static final int UPSTREAM_BAD_MEDIA_TYPE     = OFFSET_TIMEOUT + 312;

    public static final int REQUEST_STILL_PROCESSING    = OFFSET_TIMEOUT + 333;

    // Cross module call
    public static final int REF_RESOLVER_WRONG_RESPONSE_TYPE = OFFSET + 350;
    public static final int ILE_MISSING_DEPENDENCY = OFFSET_LOGIC_ERROR + 351;

    // plugin issues
    public static final int NO_PLUGIN_METHOD_AVAILABLE  = OFFSET + 360;
    public static final int PLUGIN_LOADING_ERROR        = OFFSET + 361;
    public static final int NO_MAIN_IN_PLUGIN           = OFFSET + 362;
    public static final int MAIN_IS_NOT_PLUGIN          = OFFSET + 363;
    public static final int PLUGIN_INSTANTIATION_ERROR  = OFFSET + 364;
    public static final int PLUGIN_METHOD_WRONG_TYPE    = OFFSET + 365;

    public static final int NOT_AUTHENTICATED           = OFFSET + 401;
    public static final int NOT_AUTHORIZED              = OFFSET + 403;
    public static final int NOT_AUTHORIZED_WRONG_METHOD = OFFSET + 404;  // wrong authentication method
    public static final int JWT_TIMING                  = OFFSET_LOGIC_ERROR + 405;
    public static final int JWT_INCOMPLETE              = OFFSET_LOGIC_ERROR + 406;
    public static final int JWT_EXPIRED                 = OFFSET_DENIED + 407;

    public static final int DYNAMODB_EXCEPTION          = OFFSET_DB_ERROR + 501;

    // Error codes specific to solr search
    public static final int SOLR_SERVER_NOT_AVAILABLE   = OFFSET + 701;
    public static final int ILLEGAL_SOLR_CORE_URL       = OFFSET + 702;
    public static final int SOLR_EXCEPTION              = OFFSET + 703;
    public static final int NO_CORE_DEFINITION_FOUND    = OFFSET + 704;
    public static final int NO_DOCUMENT_NAME_DEFINED    = OFFSET + 705;
    public static final int ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION = OFFSET + 710;
    public static final int ILE_SOLR_DB_COMBINED_FILTERS = OFFSET_LOGIC_ERROR + 711;
    public static final int ILE_SOLR_DB_SORT = OFFSET + 712;

    // internal logic errors
    public static final int ILE_REQUIRED_PARAMETER_IS_NULL = OFFSET_LOGIC_ERROR + 800;
    public static final int ILE_RESULT_SET_WRONG_SIZE = OFFSET_LOGIC_ERROR + 801;
    public static final int ILE_NO_BPM_ENGINE_DEPLOYED = OFFSET_LOGIC_ERROR + 802;

    public static final int METHOD_ILLEGAL_ACCESS_EXCEPTION = OFFSET + 903;
    public static final int METHOD_INVOCATION_TARGET_EXCEPTION = OFFSET + 904;
    public static final int CLASS_NOT_FOUND_EXCEPTION = OFFSET + 905;
    public static final int METHOD_INSTANTIATION_EXCEPTION = OFFSET + 906;

    public static final int CONSTRUCTOR_ILLEGAL_ACCESS_EXCEPTION = OFFSET + 915;
    public static final int CONSTRUCTOR_INSTANTIATION_EXCEPTION = OFFSET + 917;

    // plausi checks
    public static final int DATASINK_UNSUPPORTED_FORMAT = OFFSET + 950;
    public static final int TENANT_CREATE_NOT_ALLOWED = OFFSET + 951;
    public static final int OTHER_TENANT_UPDATE_NOT_ALLOWED = OFFSET + 952;
    public static final int OTHER_TENANT_DELETE_NOT_ALLOWED = OFFSET + 953;
    public static final int TENANT_SELFDELETE_NOT_ALLOWED = OFFSET + 954;
    public static final int DATASINK_UNSUPPORTED_ENCODING = OFFSET + 955;
    public static final int ROLE_REFERENCED_AS_PRIMARY_ROLE = OFFSET + 956;
    public static final int SEARCH_FILTER_VALIDATION_ERROR = OFFSET + 957;
    public static final int UNEXPECTED_FILTER_VALUE = OFFSET + 958;
    public static final int PICKUP_DATE_VALIDATION_ERROR = OFFSET + 959;

    // Codes specific for generic import
    public static final int IMPORT_NO_VALID_CLASS_IN_IMPORT_ROUTE = OFFSET + 960;
    public static final int IMPORT_NO_IMPORT_TYPE_CLASS = OFFSET + 961;
    public static final int IMPORT_FILE_NOT_FOUND = OFFSET + 962;
    public static final int IMPORT_GENERAL_ERROR = OFFSET + 963;
    public static final int LOAD_CAMEL_CONFIG_ERROR = OFFSET + 964;
    public static final int CANNOT_FIND_UI_COLUMN = OFFSET + 965;

    // IO failure notification
    public static final int IOF_UNKNOWN_TIME_WINDOW_ERROR = OFFSET + 970;
    public static final int IOF_COUNT_IOFAILURES_ERROR = OFFSET + 971;
    public static final int IOF_FIND_IOFAILURES_ERROR = OFFSET + 972;
    public static final int IOF_READ_RECIPIENTS_ERROR = OFFSET + 973;
    public static final int IOF_CREATE_NOTIFICATION_EMAIL_ERROR = OFFSET + 974;
    public static final int IOF_TIME_WINDOW_UPDATE_ERROR = OFFSET + 975;
    public static final int IOF_DUPLICATE = OFFSET + 976;

    public static final int FILE_PATH_PREFIX_NOT_AVAILABLE = OFFSET + 250;
    public static final int UNKNOWN_SYSTEM_PROPERTY_USER_HOME = OFFSET + 251;
    public static final int UNKNOWN_SYSTEM_PROPERTY_USER_NAME = OFFSET + 252;

    public static final int BAD_S3_BUCKET_NAME = OFFSET + 255;
    public static final int S3_WRITE_ERROR = OFFSET + 256;
    public static final int SQS_WRITE_ERROR = OFFSET + 257;

    // General event handling errors
    public static final int INVALID_EVENT_TYPE = OFFSET + 980;
    public static final int THREAD_INTERRUPTED = OFFSET_TIMEOUT + 991;
    public static final int UNSUPPORTED_OPERATION = OFFSET + 992;
    public static final int UNSUPPORTED_OPERAND = OFFSET + 993;

    public static final int HTTP_ERROR = OFFSET_VALIDATION_ERROR + 8000;  // whole range 8000..8999 is used, where the offset is the http status code


    // constants for messages which are accessed directly
    public static final String MSG_REQUEST_STILL_PROCESSING     = "Duplicate request detected - other thread still processing";
    public static final String MSG_SHUTDOWN_IN_PROGRESS         = "Server shutdown initiated, no more requests will be served - try again later";
    public static final String MSG_JWT_INCOMPLETE               = "The JWT is missing some required data";
    public static final String MSG_JWT_EXPIRED                  = "The JWT is no longer valid, please obtain a new one";


    /** Protected no args constructor - just there to avoid that instances of subclasses are created. */
    protected T9tException() {
        super(0);
    }

    public T9tException(final int errorCode) {
        super(errorCode);
    }

    /**
     * Creates an exception for a specific error code.
     * Please do not put redundant text (duplicating the text of the error code) into detailParameter, only additional info.
     *
     * @param errorCode         the unique code describing the error cause
     * @param detailParameters  Any additional information / parameters.
     *                          Do not put redundant text from the error code itself here!
     *                          In most cases this should be just the value causing the problem.
     */
    public T9tException(final int errorCode, final Object... detailParameters) {
        super(errorCode, createParamsString(detailParameters));
    }

    static {
            codeToDescription.put(INVALID_CRUD_COMMAND, "Attempted to use an invalid or unimplemented CRUD command");
            codeToDescription.put(RECORD_ALREADY_EXISTS, "Attempted to create a record which already exists");
            codeToDescription.put(RECORD_DOES_NOT_EXIST, "Attempted to access a record which does not exist");
            codeToDescription.put(RECORD_INACTIVE, "Attempted to run transactions on deactivated (logically deleted) masterdata");
            codeToDescription.put(MALFORMED_REQUEST_PARAMETER_NAME, "The class name of the request parameters did not end with ...Request");
            codeToDescription.put(SERVICE_CLASS_NOT_FOUND, "Could not load service class. Configuration or classpath problem?");
            codeToDescription.put(TRANSACTION_RETRY_REQUEST, "Additional attempt to run the operation is requierd.");
            codeToDescription.put(GENERAL_EXCEPTION_CENTRAL, "unhandled general exception in central message processing execute method");
            codeToDescription.put(GENERAL_EXCEPTION, "unhandled general exception");
            codeToDescription.put(INVALID_REQUEST_PARAMETER_TYPE, "The class of the request parameters did not have the expected inheritance");
            codeToDescription.put(TENANT_NOT_EXISTING, "Tenant is not existing");
            codeToDescription.put(TENANT_INACTIVE, "Tenant is inactive");
            codeToDescription.put(APP_USER_NOT_EXISTING, "App user is not existing");
            codeToDescription.put(APP_USER_INACTIVE, "App user is inactive");
            codeToDescription.put(APP_USER_TENANT_PERMISSION, "App user tenant has no permission for this tenant");
            codeToDescription.put(TOO_MANY_RECORDS, "Too many records");
            codeToDescription.put(MISSING_KEY_PARAMETER, "CRUD request did not supply required key parameter");
            codeToDescription.put(MISSING_DATA_PARAMETER, "CRUD request did not supply required data parameter");
            codeToDescription.put(EXTRA_KEY_PARAMETER, "CRUD request sent with superfluous key parameter");
            codeToDescription.put(EXTRA_DATA_PARAMETER, "CRUD request sent with superfluous data parameter");
            codeToDescription.put(MISSING_VERSION_PARAMETER, "CRUD request did not supply required version parameter");
            codeToDescription.put(SELECT_INSERT_SELECT_ERROR, "subsequent failure of SELECT, INSERT, SELECT for the same key");
            codeToDescription.put(FIELD_MAY_NOT_BE_CHANGED, "A field may not be updated to a different value");
            codeToDescription.put(ONLY_ONE_ACTIVE_ALLOWED, "Inserting the record would create more than one active rows with the same value of a key column");
            codeToDescription.put(MISSING_CONFIGURATION, "No active configuration record found");
            codeToDescription.put(INVALID_CONFIGURATION, "Configuration was done incorrectly");
            codeToDescription.put(NOT_YET_IMPLEMENTED, "The requested functionality has not yet been implemented.");
            codeToDescription.put(NO_LONGER_SUPPORTED, "The requested functionality is no longer supported.");
            codeToDescription.put(ILE_REQUIRED_PARAMETER_IS_NULL, "A required parameter has not been supplied");
            codeToDescription.put(ILE_RESULT_SET_WRONG_SIZE,
              "More result records retrieved than should be possible through table constraints. DB setup problem?");
            codeToDescription.put(ENTITY_KEY_EXCEPTION, "Could not extract the key of a JPA entity. Possibly incorrect enum token.");
            codeToDescription.put(ENTITY_DATA_MAPPING_EXCEPTION, "Exception mapping from JPA entity data to DTO. Possibly invalid enum token.");
            codeToDescription.put(RESPONSE_VALIDATION_ERROR, "Constructed a response which is invalid");
            codeToDescription.put(REQUEST_VALIDATION_ERROR, "Received response parameters which don't satisfy the interface spec");
            codeToDescription.put(WRITE_ACCESS_ONLY_CURRENT_TENANT, "Creation and update of records only allowed for current tenant");
            codeToDescription.put(READ_ACCESS_ONLY_CURRENT_TENANT, "Access to other tenant's records is not allowed");
            codeToDescription.put(NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING,
              "Not updating record because someone else has modified it already. Please reread and retry.");
            codeToDescription.put(ILLEGAL_REQUEST_PARAMETER, "The supplied request parameter class cannot be instantiated.");
            codeToDescription.put(OPTIMISTIC_LOCKING_EXCEPTION, "Optimistic locking exception");
            codeToDescription.put(REQUEST_PARAMETER_BAD_INHERITANCE, "The supplied request parameter class does not inherited the expected superclass.");
            codeToDescription.put(REQUEST_HANDLER_NOT_FOUND, "There is no request handler for the request parameter class");
            codeToDescription.put(T9T_ACCESS_DENIED, "Access denied, reason undisclosed for security reasons, see server logs");
            codeToDescription.put(REQUEST_HANDLER_RETURNED_NULL, "A request handler returned a null response");
            codeToDescription.put(WRITE_ACCESS_NOT_FOUND_PROBABLY_OTHER_TENANT,
              "Record for update not found, probably due to existing one in different tenant");
            codeToDescription.put(COULD_NOT_ACQUIRE_LOCK, "Could not acquire lock (Semaphore) on object within allowed time");
            codeToDescription.put(ILLEGAL_CHARACTER, "An illegal character has been identified in a string field of the request");

            codeToDescription.put(JWT_EXPIRED, MSG_JWT_EXPIRED);
            codeToDescription.put(JWT_TIMING,  "The JWT has unplausible time information");
            codeToDescription.put(JWT_INCOMPLETE,  MSG_JWT_INCOMPLETE);
            codeToDescription.put(BAD_TIME_SLICE, "The time slice should allow an integral number of runs per day, i.e. be a divisor of 86400.");
            codeToDescription.put(FIND_ON_NULL_KEY, "Attempt to find a record with a null for ID");
            codeToDescription.put(STALLED_LOG_WRITER, "Failed to write log, logwriter thread crashed?");
            codeToDescription.put(SHUTDOWN_IN_PROGRESS, MSG_SHUTDOWN_IN_PROGRESS);
            codeToDescription.put(CANNOT_CLOSE_SINK, "Exception while closing data sink");
            codeToDescription.put(UPSTREAM_NULL_RESPONSE, "Received no response from upstream");
            codeToDescription.put(UPSTREAM_BAD_RESPONSE, "Received a response of bad type, expected ServiceResponse");
            codeToDescription.put(UPSTREAM_BAD_MEDIA_TYPE, "Bad media type for uplink configured, only Bonaparte or ConmpactBonaparte are possible");

            codeToDescription.put(SESSION_NOT_OPENED, "Attempted to execute a request on a session which was not opened (or closed already)");
            codeToDescription.put(SESSION_OPEN_ERROR, "Attempted to execute a request on a session which was not opened successfully");
            codeToDescription.put(REF_RESOLVER_REQUEST_PARAMETER,
              "Cross module resolver was called with a missing parameter (first parameter may not be null)");
            codeToDescription.put(NOT_REQUEST_PARAMETERS, "Object is not of type RequestParameters");
            codeToDescription.put(ENUM_MAPPING,                 "Cannot map enum instance");
            codeToDescription.put(NOT_AN_ENUM,                  "Not an enum");
            codeToDescription.put(NOT_AN_XENUM,                 "Not an xenum");
            codeToDescription.put(NOT_AN_ENUMSET,               "Not an enumset");
            codeToDescription.put(NOT_AN_XENUMSET,              "Not an xenumset");
            codeToDescription.put(MALFORMATTED_FIELDNAME,       "Badly formatted field name: array index not of form [ (digits) ]");
            codeToDescription.put(NOT_ENUM_INSTANCE,            "enum(set) does not have requested instance name");
            codeToDescription.put(TOO_HIGH_RESULT_SIZE_FOR_SORTING,  "Too high result size for sorting");


            // Codes specific to getting sequence numbers (artificial keys for JPA)
            codeToDescription.put(JDBC_BAD_TYPE_RETURNED,       "Sequence query returned a type which cannot be processed (yet)");
            codeToDescription.put(JDBC_NO_RESULT_RETURNED,      "No result returned from sequence query");
            codeToDescription.put(JDBC_GENERAL_SQL,             "General SQL exception when trying to obtain next sequence value");
            codeToDescription.put(JDBC_UNKNOWN_DIALECT,         "Database dialect not yet implemented");

            codeToDescription.put(UNRECOGNIZED_FILTER_CRITERIA, "Search supplied with a filter criteria which is not recognized");
            codeToDescription.put(INVALID_FILTER_PARAMETERS, "A filter has been provided with some parameters problem");
            codeToDescription.put(UNDERSPECIFIED_FILTER_PARAMETERS, "A filter has been provided with underspecified parameters (all null)");
            codeToDescription.put(OVERSPECIFIED_FILTER_PARAMETERS, "A filter has been provided with overspecified parameters (contradicting conditions)");
            codeToDescription.put(RERUN_NOT_APPLICABLE_RET, "Rerun of request not applicable due to successful return code");
            codeToDescription.put(RERUN_NOT_APPLICABLE_DONE, "Rerun of request not applicable, because already done");
            codeToDescription.put(RERUN_NOT_POSSIBLE_NO_RECORDED_REQUEST, "Rerun of request not possible, parameters have not been recorded.");

            codeToDescription.put(CANT_OPEN_SESSION, "An attemped to open a service session failed with exception");
            codeToDescription.put(CANT_CLOSE_SESSION, "An attemped to close a service session failed with exception");
            codeToDescription.put(SESSION_OPEN_CLOSE_SEQUENCE_ERROR, "Attempt to reopen an already open ServiceSession or to close a closed one.");
            codeToDescription.put(MISSING_PARAMETER, "A Request did not supply a required parameter");
            codeToDescription.put(APP_USER_WRONG_AUTH_METHOD, "Authentication method not allowed for this user");
            codeToDescription.put(APP_USER_X500_AUTH_FAILED, "X509 certificate's DN did not match");
            codeToDescription.put(JTA_EXCEPTION, "Transaction handling error");
            codeToDescription.put(ILLEGAL_CONFIG_ITEM, "A configuration item was corrupt or not usable");

            codeToDescription.put(RESOLVE_ACCESS, "key resolver access problem - key columns must be public");
            codeToDescription.put(RESOLVE_PARAMETER, "illegal parameter in key resolver");
            codeToDescription.put(RESOLVE_BAD_CLASS, "Bad class passed to key resolver (candidates must be final and not an interface)");
            codeToDescription.put(ERROR_FILLING_RESTRICTION_CACHE, "Exception while filling the restriction cache");

            codeToDescription.put(UNRECOGNIZED_SORT_PARAMETER, "Passed sortring parameter is unknown.");
            codeToDescription.put(INCORRECT_RESPONSE_CLASS, "The result class was not of the expected type");

            // authorization / authentication specific codes
            codeToDescription.put(NOT_AUTHENTICATED, "Not authenticated");
            codeToDescription.put(NOT_AUTHORIZED, "Not authorized to perform this operation");
            codeToDescription.put(NOT_AUTHORIZED_WRONG_METHOD, "Request not allowed for this authentication method");
            codeToDescription.put(USER_NOT_FOUND, "User not found");
            codeToDescription.put(TENANT_NOT_FOUND, "Tenant not found");
            codeToDescription.put(PASSWORD_NOT_FOUND, "No password found for given user ID");
            codeToDescription.put(USER_INACTIVE, "User is inactive");
            codeToDescription.put(USER_NOT_ALLOWED_TO_ACCESS_WITH_PW, "External user should access with password");
            codeToDescription.put(USER_STATUS_NOT_FOUND, "User status not found");
            codeToDescription.put(ACCOUNT_TEMPORARILY_FROZEN, "Account temporarily frozen");
            codeToDescription.put(WRONG_PASSWORD, "Wrong password");
            codeToDescription.put(PASSWORD_EXPIRED_DUE_TO_USER_INACTIVITY, "Password has expired due to user inactivity");
            codeToDescription.put(PASSWORD_EXPIRED, "Password has expired");
            codeToDescription.put(NEW_PASSWORD_MATCHES_ONE_OF_THE_LAST, "New password matches one the last n passwords (see tenant configuration)");
            codeToDescription.put(NEW_PASSWORD_MATCHES_ONE_AND_CANT_BE_REUSED_YET, "New password matches one and can't be reused yet");
            codeToDescription.put(ROLE_NOT_FOUND, "Role not found");
            codeToDescription.put(CANNOT_RESET_PASSWORD_NO_EMAIL_SET_FOR_USER, "Can't reset password, no email address has been set on the user account.");
            codeToDescription.put(CANNOT_RESET_PASSWORD_PROVIDED_EMAIL_DOESNT_MATCH_STORED_ONE,
              "Can't reset password, the provided password does not match the stored one.");
            codeToDescription.put(GENERAL_AUTH_PROBLEM,
              "Password should be correct and the new one should differ from the old one and fit password requirements.");


            codeToDescription.put(METHOD_ILLEGAL_ACCESS_EXCEPTION, "Could not access class or method");
            codeToDescription.put(METHOD_INVOCATION_TARGET_EXCEPTION, "Could not invoke method (by reflection)");
            codeToDescription.put(CLASS_NOT_FOUND_EXCEPTION, "Could not find class to load");
            codeToDescription.put(METHOD_INSTANTIATION_EXCEPTION, "Cannot instantiate method");
            codeToDescription.put(CONSTRUCTOR_ILLEGAL_ACCESS_EXCEPTION, "Could not access instance to perform operation.");
            codeToDescription.put(CONSTRUCTOR_INSTANTIATION_EXCEPTION, "Creating instance failed.");

            codeToDescription.put(CRUD_BOTH_KEYS_MISMATCH, "Both natural and artifical keys have been supplied, but mismatch.");
            codeToDescription.put(CRUD_NATURAL_KEY_MISSING, "The CRUD operation requires a natural key.");

            // IO errors
            codeToDescription.put(FILE_NOT_FOUND_FOR_DOWNLOAD, "File was not found.");
            codeToDescription.put(BAD_REMOTE_RESPONSE, "received HTTP OK return code, but empty remote response");

            // IO errors
            codeToDescription.put(REF_RESOLVER_WRONG_RESPONSE_TYPE, "Unexpected service response type.");
            codeToDescription.put(ILE_MISSING_DEPENDENCY, "A dependency is missing because the implementation for an interface cannot be found.");

            // Plugin errors
            codeToDescription.put(NO_PLUGIN_METHOD_AVAILABLE, "Plugin method not available.");
            codeToDescription.put(PLUGIN_LOADING_ERROR, "Plugin could not be loaded");
            codeToDescription.put(NO_MAIN_IN_PLUGIN, "Plugin has no Main class");
            codeToDescription.put(MAIN_IS_NOT_PLUGIN, "Main class in plugin does not implement interface Plugin");
            codeToDescription.put(PLUGIN_INSTANTIATION_ERROR, "Problem instantiating the plugin");
            codeToDescription.put(PLUGIN_METHOD_WRONG_TYPE, "The plugin provides an implemention of different type than expected");

            // output session specific error descriptions

            codeToDescription.put(TENANT_CREATE_NOT_ALLOWED, "Creating new tenant is not allowed.");
            codeToDescription.put(OTHER_TENANT_UPDATE_NOT_ALLOWED, "Updating other tenant data is not allowed.");
            codeToDescription.put(OTHER_TENANT_DELETE_NOT_ALLOWED, "Deleting other tenants is not allowed.");
            codeToDescription.put(TENANT_SELFDELETE_NOT_ALLOWED, "Selfdelete is not allowed.");

            // solr search specific error descriptions
            codeToDescription.put(SOLR_SERVER_NOT_AVAILABLE, "The SOLR Server is not available.");
            codeToDescription.put(ILLEGAL_SOLR_CORE_URL, "The passed value is not a valid SOLR core URL.");
            codeToDescription.put(SOLR_EXCEPTION, "Solr based search currently unavailable, exception caught.");
            codeToDescription.put(NO_CORE_DEFINITION_FOUND, "No Solr core (URL) was found on the database.");
            codeToDescription.put(NO_DOCUMENT_NAME_DEFINED, "No documentName defined as property on SOLR search request class");
            codeToDescription.put(ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, "Combined search only allows FieldFilters and AND conditions");
            codeToDescription.put(ILE_SOLR_DB_COMBINED_FILTERS, "Logic error: missing search filter, which should be there");
            codeToDescription.put(ILE_SOLR_DB_SORT, "Combined SOLR and DB search only allows a single sort column");

            codeToDescription.put(IMPORT_NO_VALID_CLASS_IN_IMPORT_ROUTE, "No valid class name in the configuration of a camel route");
            codeToDescription.put(IMPORT_NO_IMPORT_TYPE_CLASS, "No class for input type in camel route header");
            codeToDescription.put(IMPORT_FILE_NOT_FOUND, "File not found for import");
            codeToDescription.put(IMPORT_GENERAL_ERROR, "Error in the service during the programmatic import process");
            codeToDescription.put(LOAD_CAMEL_CONFIG_ERROR, "Camel configuration could not be loaded.");
            codeToDescription.put(CANNOT_FIND_UI_COLUMN, "No UI column configuration found for specified column.");

            codeToDescription.put(ILE_NO_BPM_ENGINE_DEPLOYED, "Execution of a BPMN 2.0 process has been requested, but no engine is deployed.");

            // crud exception causes
            codeToDescription.put(UNIQUE_CONSTRAINT_VIOLATION, "Inserting or updating the record would cause a duplicate key on a unique index.");
            codeToDescription.put(ROLE_REFERENCED_AS_PRIMARY_ROLE, "The role cannot be removed because it is still assigned to a user as primary role.");
            // crud exception causes
            codeToDescription.put(SEARCH_FILTER_VALIDATION_ERROR, "Search filter is not of the expected type");

            codeToDescription.put(RESTRICTED_ACCESS, "Access to the following request is restricted for the user");

            // IO failure notification
            codeToDescription.put(IOF_UNKNOWN_TIME_WINDOW_ERROR, "Time window for checking IO failures could not be determined.");
            codeToDescription.put(IOF_COUNT_IOFAILURES_ERROR, "Failed to count IO failures.");
            codeToDescription.put(IOF_FIND_IOFAILURES_ERROR, "Failed to fetch IO failures information.");
            codeToDescription.put(IOF_READ_RECIPIENTS_ERROR, "Failed to read IO failure recipients list.");
            codeToDescription.put(IOF_CREATE_NOTIFICATION_EMAIL_ERROR, "Failed to create IO failure notification email.");
            codeToDescription.put(IOF_TIME_WINDOW_UPDATE_ERROR, "Failed to update time window for checking IO failures.");
            codeToDescription.put(IOF_DUPLICATE, "Duplicate import/export");
            codeToDescription.put(PICKUP_DATE_VALIDATION_ERROR, "Failed to validate Pickup date notification");
            codeToDescription.put(UNEXPECTED_FILTER_VALUE, "Unexpected filter value is found");

            codeToDescription.put(FILE_PATH_PREFIX_NOT_AVAILABLE, "Default file path prefix is not defined.");
            codeToDescription.put(UNKNOWN_SYSTEM_PROPERTY_USER_HOME, "user.home property is not set.");
            codeToDescription.put(UNKNOWN_SYSTEM_PROPERTY_USER_NAME, "user.name property is not set.");
            codeToDescription.put(BAD_S3_BUCKET_NAME, "Bad S3 Bucket name (must be bucket:path)");
            codeToDescription.put(S3_WRITE_ERROR, "Exception writing to S3 bucket");
            codeToDescription.put(SQS_WRITE_ERROR, "Exception writing to SQS bucket");

            codeToDescription.put(REQUEST_STILL_PROCESSING, MSG_REQUEST_STILL_PROCESSING);

            codeToDescription.put(INVALID_EVENT_TYPE, "Can't handle this type of event");
            codeToDescription.put(THREAD_INTERRUPTED, "The thread was interrupted (got a termination signal)");
            codeToDescription.put(UNSUPPORTED_OPERATION, "The requested operation is not supported");
            codeToDescription.put(UNSUPPORTED_OPERAND, "The provided operand or parameter is not supported");

            codeToDescription.put(DYNAMODB_EXCEPTION, "DynamoDB returned an Exception");

            codeToDescription.put(HTTP_ERROR + 400, "Bad request");
            codeToDescription.put(HTTP_ERROR + 401, "Not authorized");
            codeToDescription.put(HTTP_ERROR + 403, "Forbidden");
            codeToDescription.put(HTTP_ERROR + 415, "Mediatype not supported");
            codeToDescription.put(HTTP_ERROR + 500, "Server error");
    }

    /**
     * Concatenates all parameters to one comma separated string.
     *
     * @param detailParameters  various parameters to the exception
     * @return                  concatenated string of all parameters
     */
    private static String createParamsString(final Object... detailParameters) {

        if ((detailParameters != null) && (detailParameters.length > 0)) {
            if (detailParameters.length == 1) {
                // shortcut in case a single parameter exists: avoid GC due to object creation
                return detailParameters[0] == null ? "NULL" : detailParameters[0].toString();
            }

            final StringBuilder paramsSb = new StringBuilder();
            paramsSb.append("[ ");
            for (int i = 0; i < detailParameters.length; i++) {
                if (i != 0) {
                    paramsSb.append(", ");
                }
                final Object obj = detailParameters[i];
                paramsSb.append(obj == null ? "NULL" : obj.toString());
            }
            paramsSb.append(" ]");
            return paramsSb.toString();
        }
        return null;
    }

    /** Decision function to determine if a transaction should be rolled back or is a nonzero return code which anyway should proceed. */
    public static boolean codeImpliesRollback(final int returnCode) {
        final int classification = returnCode / CLASSIFICATION_FACTOR;
        if ((classification == ApplicationException.SUCCESS) || (classification == ApplicationException.CL_DENIED)) {
            return false; // these two are OK
        }
        // everything else should be rolled back
        return true;
    }

    /** Returns a text representation of an error code. */
    public static String codeToString(final int errorCode) {
        return new T9tException(errorCode).getStandardDescription();
    }
}
