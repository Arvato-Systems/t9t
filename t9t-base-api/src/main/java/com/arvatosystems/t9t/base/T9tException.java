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
package com.arvatosystems.t9t.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.util.ApplicationException;

/**
 * Container class that provides a consolidated list of exception that might occur in the scope of the t9t project.
 * This class extends the generic {@link ApplicationException} in order to provide error details
 * which are specific to all applications based on the t9t platform.
 */
public class T9tException extends ApplicationException {
    private static final Logger LOGGER = LoggerFactory.getLogger(T9tException.class);

    private static final long serialVersionUID = 36513359567514148L;

    /*
     * Offset for all codes in this class.
     */
    // @formatter:off
    private static final int CORE_OFFSET = T9tConstants.EXCEPTION_OFFSET_BASE;
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
    public static final int NO_SUCH_REQUEST = OFFSET_DENIED + 44;
    public static final int UPDATE_DECLINED = OFFSET_DENIED + 45;
    public static final int NO_IMPLEMENTATION_FOR_SPECIFIED_QUALIFIER = OFFSET + 46;
    public static final int ATTEMPT_TO_CHANGE_FROZEN_FIELD = OFFSET_LOGIC_ERROR + 47;

    public static final int STALLED_LOG_WRITER = OFFSET_TIMEOUT + 50;
    public static final int REQUEST_HANDLER_RETURNED_NULL = OFFSET_LOGIC_ERROR + 51;
    public static final int SHUTDOWN_IN_PROGRESS = OFFSET_TIMEOUT + 52;
    public static final int CANNOT_CLOSE_SINK = OFFSET_DB_ERROR + 55;

    public static final int INDEX_OUT_OF_BOUNDS = OFFSET_LOGIC_ERROR + 92;
    public static final int CLASS_CAST = OFFSET_LOGIC_ERROR + 93;
    public static final int OPTIMISTIC_LOCKING_EXCEPTION = OFFSET_DB_ERROR + 94;  // causes retry!
    public static final int REQUEST_PARAMETER_BAD_INHERITANCE = OFFSET + 95;
    public static final int TRANSACTION_RETRY_REQUEST = OFFSET_DB_ERROR + 96;
    public static final int NULL_POINTER = OFFSET_LOGIC_ERROR + 97;
    public static final int GENERAL_EXCEPTION = OFFSET_DB_ERROR + 98;             // causes retry in some cases!
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
    public static final int ILE_UNREACHABLE_CODE = OFFSET_LOGIC_ERROR + 124;

    public static final int NOT_APPLICABLE = OFFSET + 125;
    public static final int NO_ACTIVE_FLAG = OFFSET + 126;
    public static final int MISSING_CHANGE_ID = OFFSET + 127;
    public static final int MISSING_DATA_FOR_CHANGE_REQUEST = OFFSET + 128;

    public static final int NO_DATA_CACHED = OFFSET_LOGIC_ERROR + 129;

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

    public static final int CHANGE_REQUEST_PERMISSION_ERROR = OFFSET_VALIDATION_ERROR + 139;
    public static final int INVALID_CHANGE_REQUEST_STATUS = OFFSET + 140;
    public static final int CHANGE_REQUEST_FINALIZED = OFFSET + 141;
    public static final int INVALID_CHANGE_REQUEST = OFFSET + 143;
    public static final int CHANGE_REQUEST_ACTIVATION_ERROR = OFFSET + 144;

    // Codes specific to getting sequence numbers (artificial keys for JPA)
    public static final int JDBC_BAD_TYPE_RETURNED      = OFFSET_DB_ERROR + 150;
    public static final int JDBC_NO_RESULT_RETURNED     = OFFSET_DB_ERROR + 151;
    public static final int JDBC_GENERAL_SQL            = OFFSET_DB_ERROR + 152;
    public static final int JDBC_UNKNOWN_DIALECT        = OFFSET_DB_ERROR + 153;

    // Codes specific to parameter validation
    public static final int INVALID_ENUM_VALUE                 = OFFSET + 160;
    public static final int INVALID_PARAMETER                  = OFFSET + 161;

    // Codes specific to config updater services
    public static final int UPDATER_NO_CRUDVIEWMODEL_FOR_CLASS = OFFSET + 170;
    public static final int UPDATER_UNSUPPORTED_CRUD_TYPE      = OFFSET + 171;
    public static final int UPDATER_KEY_CLASS_MISMATCH         = OFFSET + 172;
    public static final int UPDATER_AID_INVALID_OPERATION      = OFFSET + 173;
    public static final int UPDATER_NO_SEARCH_REQUEST          = OFFSET + 174;

    // Codes specific to MediaData resolving
    public static final int INVALID_LAZY_MEDIADATA_PARAMETERS  = OFFSET_LOGIC_ERROR + 180;
    public static final int FAILED_TO_RESOLVE_MEDIADATA        = OFFSET_DB_ERROR + 181;

    // Codes specific to security functions (authentication / authorization)
    public static final int USER_NOT_FOUND                     = OFFSET + 200;
    public static final int TENANT_NOT_FOUND                   = OFFSET + 201;

    // Authentication request handler specific
    public static final int USER_INACTIVE = OFFSET + 202;
    public static final int USER_NOT_ALLOWED_TO_ACCESS_WITH_PW = OFFSET + 203;
    public static final int USER_STATUS_NOT_FOUND = OFFSET + 204;
    public static final int ACCOUNT_TEMPORARILY_FROZEN = OFFSET_DENIED + 205;  // for consistency with WRONG_PASSWORD this should be a DENIED as well.
    public static final int PASSWORD_NOT_FOUND = OFFSET + 206;
    public static final int PASSWORD_EXPIRED_DUE_TO_USER_INACTIVITY = OFFSET + 207;

    /** This code is an OK code, because the login was correct and must be recorded. It is responsibility of the UI to request a new PW. */
    public static final int PASSWORD_EXPIRED = 208;

    public static final int WRONG_PASSWORD = OFFSET_DENIED + 209;  // absolutely must be a "decline" code 1xxx in order to increment the failure counter!
    public static final int NEW_PASSWORD_MATCHES_ONE_OF_THE_LAST = OFFSET + 210;
    public static final int NEW_PASSWORD_MATCHES_ONE_AND_CANT_BE_REUSED_YET = OFFSET + 211;
    public static final int ROLE_NOT_FOUND = OFFSET + 212;
    public static final int CANNOT_RESET_PASSWORD_NO_EMAIL_SET_FOR_USER = OFFSET + 213;
    public static final int CANNOT_RESET_PASSWORD_PROVIDED_EMAIL_DOESNT_MATCH_STORED_ONE = OFFSET + 214;

    public static final int INCORRECT_RESPONSE_CLASS = OFFSET_LOGIC_ERROR + 215;
    public static final int RESTRICTED_ACCESS = OFFSET_DENIED + 216;
    public static final int ACCESS_DENIED = OFFSET_DENIED + 217;
    public static final int NO_SUITABLE_AUTHENTICATION_PROVIDER_FOUND = OFFSET_LOGIC_ERROR + 218;
    public static final int GENERAL_AUTH_PROBLEM             = OFFSET + 219;
    public static final int INVALID_EMAIL_FORMAT             = OFFSET + 220;
    public static final int MISSING_UPLINK_CONFIGURATION     = OFFSET + 227;
    public static final int MISSING_ENCRYPTION_CONFIGURATION = OFFSET + 228;
    public static final int ENCRYPTION_NO_SUCH_PROVIDER      = OFFSET + 229;
    public static final int ENCRYPTION_NO_SUCH_ALGORITHM     = OFFSET + 230;
    public static final int ENCRYPTION_NO_SUCH_PADDING       = OFFSET + 231;
    public static final int ENCRYPTION_INVALID_KEY           = OFFSET + 232;
    public static final int ENCRYPTION_NO_IV_DATA            = OFFSET + 233;
    public static final int ENCRYPTION_BAD_IV_LENGTH         = OFFSET + 234;
    public static final int ENCRYPTION_BAD_PARAMETER         = OFFSET + 235;
    public static final int ENCRYPTION_DECRYPTION_FAILED     = OFFSET + 236;
    public static final int ENCRYPTION_ENCRYPTION_FAILED     = OFFSET + 237;

    // http problems
    public static final int HTTP_MULTI_PART_NO_PARTS    = OFFSET_LOGIC_ERROR + 290;
    public static final int HTTP_MULTI_PART_EMPTY_KEY   = OFFSET_LOGIC_ERROR + 291;
    public static final int HTTP_MULTI_PART_EMPTY_DATA  = OFFSET_LOGIC_ERROR + 292;
    public static final int UNKNOWN_MEDIA_TYPE          = OFFSET_LOGIC_ERROR + 294;

    // IO errors
    public static final int FILE_NOT_FOUND_FOR_DOWNLOAD = OFFSET + 300;
    public static final int BAD_REMOTE_RESPONSE         = OFFSET_TIMEOUT + 304;

    public static final int UPSTREAM_NULL_RESPONSE      = OFFSET_TIMEOUT + 310;
    public static final int UPSTREAM_BAD_RESPONSE       = OFFSET_TIMEOUT + 311;
    public static final int UPSTREAM_BAD_MEDIA_TYPE     = OFFSET_TIMEOUT + 312;
    public static final int INVALID_WRAPPED_JSON        = OFFSET_TIMEOUT + 313;
    public static final int XML_EXCEPTION               = OFFSET_TIMEOUT + 314;

    public static final int REQUEST_STILL_PROCESSING    = OFFSET_TIMEOUT + 333;

    // Cross module call
    public static final int REF_RESOLVER_WRONG_RESPONSE_TYPE = OFFSET + 350;
    public static final int ILE_MISSING_DEPENDENCY = OFFSET_LOGIC_ERROR + 351;

    public static final int INVALID_PROCESSING = OFFSET_LOGIC_ERROR + 358;


    // plugin issues
    public static final int NO_PLUGIN_METHOD_AVAILABLE  = OFFSET + 360;
    public static final int PLUGIN_LOADING_ERROR        = OFFSET + 361;
    public static final int NO_MAIN_IN_PLUGIN           = OFFSET + 362;
    public static final int MAIN_IS_NOT_PLUGIN          = OFFSET + 363;
    public static final int PLUGIN_INSTANTIATION_ERROR  = OFFSET + 364;
    public static final int PLUGIN_METHOD_WRONG_TYPE    = OFFSET + 365;
    public static final int PLUGINS_NOT_ENABLED         = OFFSET + 366;

    public static final int NOT_AUTHENTICATED           = OFFSET + 401;
    public static final int NOT_AUTHORIZED              = OFFSET + 403;
    public static final int NOT_AUTHORIZED_WRONG_METHOD = OFFSET + 404;  // wrong authentication method
    public static final int JWT_TIMING                  = OFFSET_LOGIC_ERROR + 405;
    public static final int JWT_INCOMPLETE              = OFFSET_LOGIC_ERROR + 406;
    public static final int JWT_EXPIRED                 = OFFSET_DENIED + 407;
    public static final int CALLOUTS_NOT_ENABLED        = OFFSET + 408;

    public static final int DYNAMODB_EXCEPTION          = OFFSET_DB_ERROR + 501;
    public static final int MISSING_KAFKA_BOOTSTRAP     = OFFSET + 502;
    public static final int KAFKA_LISTENER_ERROR        = OFFSET + 503;

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
    public static final int METHOD_INSTANTIATION_EXCEPTION = OFFSET_LOGIC_ERROR + 906;

    public static final int CONSTRUCTOR_ILLEGAL_ACCESS_EXCEPTION = OFFSET_LOGIC_ERROR + 915;
    public static final int CONSTRUCTOR_INSTANTIATION_EXCEPTION = OFFSET_LOGIC_ERROR + 917;
    public static final int INVALID_DATETIME_FORMAT = OFFSET + 918;

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
    public static final int REST_BAD_LIST_SIZE = OFFSET + 966;

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
    public static final int GENERAL_SERVER_ERROR = OFFSET_DB_ERROR + 994;  // masked backend error which we do not want to forward to client
    public static final int INVALID_EXCEPTION_CODE = OFFSET_LOGIC_ERROR + 999;


    public static final int HTTP_ERROR = OFFSET_VALIDATION_ERROR + 8000;        // whole range 8000..8999 is used, where the offset is the http status code
    public static final int HTTP_ERROR_NOT_AUTHENTICATED = HTTP_ERROR + 401;    // alternate response code for 401
    public static final int HTTP_ERROR_NOT_AUTHORIZED    = HTTP_ERROR + 403;    // alternate response code for 403
    public static final int HTTP_ERROR_BAD_MEDIA_TYPE    = HTTP_ERROR + 415;    // alternate response code for 415


    // constants for messages which are accessed directly
    public static final String MSG_REQUEST_STILL_PROCESSING     = "Duplicate request detected - other thread still processing";
    public static final String MSG_SHUTDOWN_IN_PROGRESS         = "Server shutdown initiated, no more requests will be served - try again later";
    public static final String MSG_JWT_INCOMPLETE               = "The JWT is missing some required data";
    public static final String MSG_JWT_EXPIRED                  = "The JWT is no longer valid, please obtain a new one";


    /** Protected no args constructor - just there to avoid that instances of subclasses are created. */
    protected T9tException() {
        super(0);
    }

    /**
     * Checks that the exception has a meaningful code, because people keep throwing CL_* code.
     */
    private static int validateExceptionCode(final int errorCode) {
        if (errorCode >= 1 * CLASSIFICATION_FACTOR && errorCode < 10 * CLASSIFICATION_FACTOR) {
            return errorCode;
        }
        LOGGER.error("**** FIX THIS CODE, INVALID EXCEPTION CODE THROWN: {} ****", errorCode);
        LOGGER.error("Stack trace", new Exception());
        return INVALID_EXCEPTION_CODE;
    }

    public T9tException(final int errorCode) {
        super(validateExceptionCode(errorCode));
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
        super(validateExceptionCode(errorCode), createParamsString(detailParameters));
    }

    static {
        registerRange(CORE_OFFSET, false, T9tException.class, ApplicationLevelType.FRAMEWORK, "t9t framework core");

        registerCode(INVALID_CRUD_COMMAND, "Attempted to use an invalid or unimplemented CRUD command");
        registerCode(RECORD_ALREADY_EXISTS, "Attempted to create a record which already exists");
        registerCode(RECORD_DOES_NOT_EXIST, "Attempted to access a record which does not exist");
        registerCode(RECORD_INACTIVE, "Attempted to run transactions on deactivated (logically deleted) masterdata");
        registerCode(MALFORMED_REQUEST_PARAMETER_NAME, "The class name of the request parameters did not end with ...Request");
        registerCode(SERVICE_CLASS_NOT_FOUND, "Could not load service class. Configuration or classpath problem?");
        registerCode(TRANSACTION_RETRY_REQUEST, "Additional attempt to run the operation is requierd.");
        registerCode(INDEX_OUT_OF_BOUNDS, "Index out of bounds exception (see logs)");
        registerCode(CLASS_CAST, "Class cast exception (see logs)");
        registerCode(NULL_POINTER, "Null pointer exception (see logs)");
        registerCode(GENERAL_EXCEPTION, "unhandled general exception");
        registerCode(INVALID_REQUEST_PARAMETER_TYPE, "The class of the request parameters did not have the expected inheritance");
        registerCode(TENANT_NOT_EXISTING, "Tenant is not existing");
        registerCode(TENANT_INACTIVE, "Tenant is inactive");
        registerCode(APP_USER_NOT_EXISTING, "App user is not existing");
        registerCode(APP_USER_INACTIVE, "App user is inactive");
        registerCode(APP_USER_TENANT_PERMISSION, "App user tenant has no permission for this tenant");
        registerCode(TOO_MANY_RECORDS, "Too many records");
        registerCode(MISSING_KEY_PARAMETER, "CRUD request did not supply required key parameter");
        registerCode(MISSING_DATA_PARAMETER, "CRUD request did not supply required data parameter");
        registerCode(EXTRA_KEY_PARAMETER, "CRUD request sent with superfluous key parameter");
        registerCode(EXTRA_DATA_PARAMETER, "CRUD request sent with superfluous data parameter");
        registerCode(MISSING_VERSION_PARAMETER, "CRUD request did not supply required version parameter");
        registerCode(SELECT_INSERT_SELECT_ERROR, "subsequent failure of SELECT, INSERT, SELECT for the same key");
        registerCode(FIELD_MAY_NOT_BE_CHANGED, "A field may not be updated to a different value");
        registerCode(ONLY_ONE_ACTIVE_ALLOWED, "Inserting the record would create more than one active rows with the same value of a key column");
        registerCode(MISSING_CONFIGURATION, "No active configuration record found");
        registerCode(MISSING_UPLINK_CONFIGURATION, "No configuration for uplink found in config.xml");
        registerCode(MISSING_ENCRYPTION_CONFIGURATION, "No configuration for encryption found in config.xml");
        registerCode(ENCRYPTION_NO_SUCH_PROVIDER, "No provider of requested ID found");
        registerCode(ENCRYPTION_NO_SUCH_ALGORITHM, "No Cipher for the requested transformation found");
        registerCode(ENCRYPTION_NO_SUCH_PADDING, "No padding available for the specified cipher");
        registerCode(ENCRYPTION_INVALID_KEY, "The secret key is invalid");
        registerCode(ENCRYPTION_NO_IV_DATA, "No data for initialization vector provided");
        registerCode(ENCRYPTION_BAD_IV_LENGTH, "Bad length of initialization vector specified");
        registerCode(ENCRYPTION_BAD_PARAMETER, "Bad parameter for encryption transformation");
        registerCode(ENCRYPTION_DECRYPTION_FAILED, "Decryption failed");
        registerCode(ENCRYPTION_ENCRYPTION_FAILED, "Encryption failed");

        registerCode(HTTP_MULTI_PART_NO_PARTS, "http multipart publishers must see at least one part");
        registerCode(HTTP_MULTI_PART_EMPTY_DATA, "A part of http multipart cannot be null or empty");
        registerCode(HTTP_MULTI_PART_EMPTY_KEY, "A key of http multipart cannot be null or empty");
        registerCode(UNKNOWN_MEDIA_TYPE, "Configuration inconsistency: MediaDataof type which has no descriptor in MediaTypeInfo");

        registerCode(INVALID_CONFIGURATION, "Configuration was done incorrectly");
        registerCode(NOT_YET_IMPLEMENTED, "The requested functionality has not yet been implemented.");
        registerCode(NO_LONGER_SUPPORTED, "The requested functionality is no longer supported.");
        registerCode(ILE_REQUIRED_PARAMETER_IS_NULL, "A required parameter has not been supplied");
        registerCode(ILE_RESULT_SET_WRONG_SIZE,
            "More result records retrieved than should be possible through table constraints. DB setup problem?");
        registerCode(ENTITY_KEY_EXCEPTION, "Could not extract the key of a JPA entity. Possibly incorrect enum token.");
        registerCode(ENTITY_DATA_MAPPING_EXCEPTION, "Exception mapping from JPA entity data to DTO. Possibly invalid enum token.");
        registerCode(RESPONSE_VALIDATION_ERROR, "Constructed a response which is invalid");
        registerCode(REQUEST_VALIDATION_ERROR, "Request or response fields don't satisfy the interface spec");
        registerCode(WRITE_ACCESS_ONLY_CURRENT_TENANT, "Creation and update of records only allowed for current tenant");
        registerCode(READ_ACCESS_ONLY_CURRENT_TENANT, "Access to other tenant's records is not allowed");
        registerCode(NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING,
            "Not updating record because someone else has modified it already. Please reread and retry.");
        registerCode(ILLEGAL_REQUEST_PARAMETER, "The supplied request parameter class cannot be instantiated.");
        registerCode(OPTIMISTIC_LOCKING_EXCEPTION, "Optimistic locking exception");
        registerCode(REQUEST_PARAMETER_BAD_INHERITANCE, "The supplied request parameter class does not inherited the expected superclass.");
        registerCode(REQUEST_HANDLER_NOT_FOUND, "There is no request handler for the request parameter class");
        registerCode(T9T_ACCESS_DENIED, "Access denied, reason undisclosed for security reasons, see server logs");
        registerCode(REQUEST_HANDLER_RETURNED_NULL, "A request handler returned a null response");
        registerCode(WRITE_ACCESS_NOT_FOUND_PROBABLY_OTHER_TENANT,
            "Record for update not found, probably due to existing one in different tenant");
        registerCode(COULD_NOT_ACQUIRE_LOCK, "Could not acquire lock (Semaphore) on object within allowed time");
        registerCode(ILLEGAL_CHARACTER, "An illegal character has been identified in a string field of the request");
        registerCode(NO_SUCH_REQUEST, "Request not received or not yet complete");
        registerCode(UPDATE_DECLINED, "Data record is modified. Update is declined.");
        registerCode(NO_IMPLEMENTATION_FOR_SPECIFIED_QUALIFIER, "There is no implementation for the specified qualifier");
        registerCode(ATTEMPT_TO_CHANGE_FROZEN_FIELD, "Attempt to change a field which is frozen");

        registerCode(JWT_EXPIRED, MSG_JWT_EXPIRED);
        registerCode(JWT_TIMING,  "The JWT has unplausible time information");
        registerCode(JWT_INCOMPLETE,  MSG_JWT_INCOMPLETE);
        registerCode(BAD_TIME_SLICE, "The time slice should allow an integral number of runs per day, i.e. be a divisor of 86400.");
        registerCode(FIND_ON_NULL_KEY, "Attempt to find a record with a null for ID");
        registerCode(STALLED_LOG_WRITER, "Failed to write log, logwriter thread crashed?");
        registerCode(SHUTDOWN_IN_PROGRESS, MSG_SHUTDOWN_IN_PROGRESS);
        registerCode(CANNOT_CLOSE_SINK, "Exception while closing data sink");
        registerCode(UPSTREAM_NULL_RESPONSE, "Received no response from upstream");
        registerCode(UPSTREAM_BAD_RESPONSE, "Received a response of bad type, expected ServiceResponse");
        registerCode(UPSTREAM_BAD_MEDIA_TYPE, "Bad media type for uplink configured, only Bonaparte or ConmpactBonaparte are possible");
        registerCode(INVALID_WRAPPED_JSON, "JSON wrapped into XML kvp structure not valid");
        registerCode(XML_EXCEPTION, "XML parsing problem");

        registerCode(SESSION_NOT_OPENED, "Attempted to execute a request on a session which was not opened (or closed already)");
        registerCode(SESSION_OPEN_ERROR, "Attempted to execute a request on a session which was not opened successfully");
        registerCode(REF_RESOLVER_REQUEST_PARAMETER,
            "Cross module resolver was called with a missing parameter (first parameter may not be null)");
        registerCode(NOT_REQUEST_PARAMETERS, "Object is not of type RequestParameters");
        registerCode(ENUM_MAPPING,                 "Cannot map enum instance");
        registerCode(NOT_AN_ENUM,                  "Not an enum");
        registerCode(NOT_AN_XENUM,                 "Not an xenum");
        registerCode(NOT_AN_ENUMSET,               "Not an enumset");
        registerCode(NOT_AN_XENUMSET,              "Not an xenumset");
        registerCode(MALFORMATTED_FIELDNAME,       "Badly formatted field name: array index not of form [ (digits) ]");
        registerCode(NOT_ENUM_INSTANCE,            "enum(set) does not have requested instance name");
        registerCode(TOO_HIGH_RESULT_SIZE_FOR_SORTING,  "Too high result size for sorting");
        registerCode(CHANGE_REQUEST_PERMISSION_ERROR,   "No permission for this action on the change request");
        registerCode(INVALID_CHANGE_REQUEST_STATUS,     "Invalid status for change request");
        registerCode(CHANGE_REQUEST_FINALIZED,          "Change request is already finalized");
        registerCode(INVALID_CHANGE_REQUEST,            "Change request is invalid!");
        registerCode(CHANGE_REQUEST_ACTIVATION_ERROR,   "Change request activation failed!");


        // Codes specific to getting sequence numbers (artificial keys for JPA)
        registerCode(JDBC_BAD_TYPE_RETURNED,       "Sequence query returned a type which cannot be processed (yet)");
        registerCode(JDBC_NO_RESULT_RETURNED,      "No result returned from sequence query");
        registerCode(JDBC_GENERAL_SQL,             "General SQL exception when trying to obtain next sequence value");
        registerCode(JDBC_UNKNOWN_DIALECT,         "Database dialect not yet implemented");

        registerCode(UNRECOGNIZED_FILTER_CRITERIA, "Search supplied with a filter criteria which is not recognized");
        registerCode(INVALID_FILTER_PARAMETERS, "A filter has been provided with some parameters problem");
        registerCode(UNDERSPECIFIED_FILTER_PARAMETERS, "A filter has been provided with underspecified parameters (all null)");
        registerCode(OVERSPECIFIED_FILTER_PARAMETERS, "A filter has been provided with overspecified parameters (contradicting conditions)");
        registerCode(RERUN_NOT_APPLICABLE_RET, "Rerun of request not applicable due to successful return code");
        registerCode(RERUN_NOT_APPLICABLE_DONE, "Rerun of request not applicable, because already done");
        registerCode(RERUN_NOT_POSSIBLE_NO_RECORDED_REQUEST, "Rerun of request not possible, parameters have not been recorded.");

        registerCode(CANT_OPEN_SESSION, "An attemped to open a service session failed with exception");
        registerCode(CANT_CLOSE_SESSION, "An attemped to close a service session failed with exception");
        registerCode(SESSION_OPEN_CLOSE_SEQUENCE_ERROR, "Attempt to reopen an already open ServiceSession or to close a closed one.");
        registerCode(MISSING_PARAMETER, "A Request did not supply a required parameter");
        registerCode(APP_USER_WRONG_AUTH_METHOD, "Authentication method not allowed for this user");
        registerCode(APP_USER_X500_AUTH_FAILED, "X509 certificate's DN did not match");
        registerCode(JTA_EXCEPTION, "Transaction handling error");
        registerCode(ILLEGAL_CONFIG_ITEM, "A configuration item was corrupt or not usable");

        registerCode(RESOLVE_ACCESS, "key resolver access problem - key columns must be public");
        registerCode(RESOLVE_PARAMETER, "illegal parameter in key resolver");
        registerCode(RESOLVE_BAD_CLASS, "Bad class passed to key resolver (candidates must be final and not an interface)");
        registerCode(ERROR_FILLING_RESTRICTION_CACHE, "Exception while filling the restriction cache");
        registerCode(ILE_UNREACHABLE_CODE, "Executing unreachable code (should not happen!)");

        registerCode(NOT_APPLICABLE, "Operation not applicable");
        registerCode(NO_ACTIVE_FLAG, "No active flag - activate / deactivate not possible");
        registerCode(NO_DATA_CACHED, "No cached data found for tenant");
        registerCode(MISSING_CHANGE_ID, "ChangeId is required for approval request");
        registerCode(MISSING_DATA_FOR_CHANGE_REQUEST, "Unable to found data for the CRUD request!");

        registerCode(UNRECOGNIZED_SORT_PARAMETER, "Passed sortring parameter is unknown.");
        registerCode(INCORRECT_RESPONSE_CLASS, "The result class was not of the expected type");

        // authorization / authentication specific codes
        registerCode(NOT_AUTHENTICATED, "Not authenticated");
        registerCode(NOT_AUTHORIZED, "Not authorized to perform this operation");
        registerCode(NOT_AUTHORIZED_WRONG_METHOD, "Request not allowed for this authentication method");
        registerCode(USER_NOT_FOUND, "User not found");
        registerCode(TENANT_NOT_FOUND, "Tenant not found");
        registerCode(PASSWORD_NOT_FOUND, "No password found for given user ID");
        registerCode(USER_INACTIVE, "User is inactive");
        registerCode(USER_NOT_ALLOWED_TO_ACCESS_WITH_PW, "External user should access with password");
        registerCode(USER_STATUS_NOT_FOUND, "User status not found");
        registerCode(ACCOUNT_TEMPORARILY_FROZEN, "Account temporarily frozen");
        registerCode(WRONG_PASSWORD, "Wrong password");
        registerCode(PASSWORD_EXPIRED_DUE_TO_USER_INACTIVITY, "Password has expired due to user inactivity");
        registerCode(PASSWORD_EXPIRED, "Password has expired");
        registerCode(NEW_PASSWORD_MATCHES_ONE_OF_THE_LAST, "New password matches one the last n passwords (see tenant configuration)");
        registerCode(NEW_PASSWORD_MATCHES_ONE_AND_CANT_BE_REUSED_YET, "New password matches one and can't be reused yet");
        registerCode(ROLE_NOT_FOUND, "Role not found");
        registerCode(CANNOT_RESET_PASSWORD_NO_EMAIL_SET_FOR_USER, "Can't reset password, no email address has been set on the user account.");
        registerCode(CANNOT_RESET_PASSWORD_PROVIDED_EMAIL_DOESNT_MATCH_STORED_ONE,
            "Can't reset password, the provided password does not match the stored one.");
        registerCode(GENERAL_AUTH_PROBLEM,
            "Password should be correct and the new one should differ from the old one and fit password requirements.");
        registerCode(INVALID_EMAIL_FORMAT, "Email format seems to be corrupt");

        registerCode(METHOD_ILLEGAL_ACCESS_EXCEPTION, "Could not access class or method");
        registerCode(METHOD_INVOCATION_TARGET_EXCEPTION, "Could not invoke method (by reflection)");
        registerCode(CLASS_NOT_FOUND_EXCEPTION, "Could not find class to load");
        registerCode(METHOD_INSTANTIATION_EXCEPTION, "Cannot instantiate method");
        registerCode(CONSTRUCTOR_ILLEGAL_ACCESS_EXCEPTION, "Could not access instance to perform operation.");
        registerCode(CONSTRUCTOR_INSTANTIATION_EXCEPTION, "Creating instance failed.");
        registerCode(INVALID_DATETIME_FORMAT, "Invalid date/time format");

        registerCode(CRUD_BOTH_KEYS_MISMATCH, "Both natural and artifical keys have been supplied, but mismatch.");
        registerCode(CRUD_NATURAL_KEY_MISSING, "The CRUD operation requires a natural key.");

        // IO errors
        registerCode(FILE_NOT_FOUND_FOR_DOWNLOAD, "File was not found.");
        registerCode(BAD_REMOTE_RESPONSE, "received HTTP OK return code, but empty remote response");

        // IO errors
        registerCode(REF_RESOLVER_WRONG_RESPONSE_TYPE, "Unexpected service response type.");
        registerCode(ILE_MISSING_DEPENDENCY, "A dependency is missing because the implementation for an interface cannot be found.");

        // Plugin errors
        registerCode(NO_PLUGIN_METHOD_AVAILABLE, "Plugin method not available.");
        registerCode(PLUGIN_LOADING_ERROR, "Plugin could not be loaded");
        registerCode(NO_MAIN_IN_PLUGIN, "Plugin has no Main class");
        registerCode(MAIN_IS_NOT_PLUGIN, "Main class in plugin does not implement interface Plugin");
        registerCode(PLUGIN_INSTANTIATION_ERROR, "Problem instantiating the plugin");
        registerCode(PLUGIN_METHOD_WRONG_TYPE, "The plugin provides an implemention of different type than expected");
        registerCode(PLUGINS_NOT_ENABLED, "Loading plugins has not been enabled for this environment");

        registerCode(CALLOUTS_NOT_ENABLED, "Callouts to other instances has not been enabled for this environment");

        // output session specific error descriptions

        registerCode(TENANT_CREATE_NOT_ALLOWED, "Creating new tenant is not allowed.");
        registerCode(OTHER_TENANT_UPDATE_NOT_ALLOWED, "Updating other tenant data is not allowed.");
        registerCode(OTHER_TENANT_DELETE_NOT_ALLOWED, "Deleting other tenants is not allowed.");
        registerCode(TENANT_SELFDELETE_NOT_ALLOWED, "Selfdelete is not allowed.");

        // solr search specific error descriptions
        registerCode(SOLR_SERVER_NOT_AVAILABLE, "The SOLR Server is not available.");
        registerCode(ILLEGAL_SOLR_CORE_URL, "The passed value is not a valid SOLR core URL.");
        registerCode(SOLR_EXCEPTION, "Solr based search currently unavailable, exception caught.");
        registerCode(NO_CORE_DEFINITION_FOUND, "No Solr core (URL) was found on the database.");
        registerCode(NO_DOCUMENT_NAME_DEFINED, "No documentName defined as property on SOLR search request class");
        registerCode(ILLEGAL_SOLR_DB_COMBINED_FILTER_EXPRESSION, "Combined search only allows FieldFilters and AND conditions");
        registerCode(ILE_SOLR_DB_COMBINED_FILTERS, "Logic error: missing search filter, which should be there");
        registerCode(ILE_SOLR_DB_SORT, "Combined SOLR and DB search only allows a single sort column");

        registerCode(IMPORT_NO_VALID_CLASS_IN_IMPORT_ROUTE, "No valid class name in the configuration of a camel route");
        registerCode(IMPORT_NO_IMPORT_TYPE_CLASS, "No class for input type in camel route header");
        registerCode(IMPORT_FILE_NOT_FOUND, "File not found for import");
        registerCode(IMPORT_GENERAL_ERROR, "Error in the service during the programmatic import process");
        registerCode(LOAD_CAMEL_CONFIG_ERROR, "Camel configuration could not be loaded.");
        registerCode(CANNOT_FIND_UI_COLUMN, "No UI column configuration found for specified column.");
        registerCode(REST_BAD_LIST_SIZE, "Main object list must have size 1 for REST calls.");

        registerCode(ILE_NO_BPM_ENGINE_DEPLOYED, "Execution of a BPMN 2.0 process has been requested, but no engine is deployed.");

        // crud exception causes
        registerCode(UNIQUE_CONSTRAINT_VIOLATION, "Inserting or updating the record would cause a duplicate key on a unique index.");
        registerCode(ROLE_REFERENCED_AS_PRIMARY_ROLE, "The role cannot be removed because it is still assigned to a user as primary role.");
        // crud exception causes
        registerCode(SEARCH_FILTER_VALIDATION_ERROR, "Search filter is not of the expected type");

        registerCode(RESTRICTED_ACCESS, "Access to the following request is restricted for the user");

        // IO failure notification
        registerCode(IOF_UNKNOWN_TIME_WINDOW_ERROR, "Time window for checking IO failures could not be determined.");
        registerCode(IOF_COUNT_IOFAILURES_ERROR, "Failed to count IO failures.");
        registerCode(IOF_FIND_IOFAILURES_ERROR, "Failed to fetch IO failures information.");
        registerCode(IOF_READ_RECIPIENTS_ERROR, "Failed to read IO failure recipients list.");
        registerCode(IOF_CREATE_NOTIFICATION_EMAIL_ERROR, "Failed to create IO failure notification email.");
        registerCode(IOF_TIME_WINDOW_UPDATE_ERROR, "Failed to update time window for checking IO failures.");
        registerCode(IOF_DUPLICATE, "Duplicate import/export");
        registerCode(PICKUP_DATE_VALIDATION_ERROR, "Failed to validate Pickup date notification");
        registerCode(UNEXPECTED_FILTER_VALUE, "Unexpected filter value is found");

        registerCode(FILE_PATH_PREFIX_NOT_AVAILABLE, "Default file path prefix is not defined.");
        registerCode(UNKNOWN_SYSTEM_PROPERTY_USER_HOME, "user.home property is not set.");
        registerCode(UNKNOWN_SYSTEM_PROPERTY_USER_NAME, "user.name property is not set.");
        registerCode(BAD_S3_BUCKET_NAME, "Bad S3 Bucket name (must be bucket:path)");
        registerCode(S3_WRITE_ERROR, "Exception writing to S3 bucket");
        registerCode(SQS_WRITE_ERROR, "Exception writing to SQS bucket");

        registerCode(INVALID_PROCESSING, "Logic error: cannot route to different server in single node environment");

        registerCode(REQUEST_STILL_PROCESSING, MSG_REQUEST_STILL_PROCESSING);

        registerCode(INVALID_EVENT_TYPE, "Can't handle this type of event");
        registerCode(THREAD_INTERRUPTED, "The thread was interrupted (got a termination signal)");
        registerCode(UNSUPPORTED_OPERATION, "The requested operation is not supported");
        registerCode(UNSUPPORTED_OPERAND, "The provided operand or parameter is not supported");

        registerCode(DYNAMODB_EXCEPTION, "DynamoDB returned an Exception");
        registerCode(MISSING_KAFKA_BOOTSTRAP, "Kafka bootstrap servers not specified");
        registerCode(KAFKA_LISTENER_ERROR, "Could not create kafka listener");
        registerCode(GENERAL_SERVER_ERROR, "Server error");

        registerCode(INVALID_ENUM_VALUE, "Invalid instance value");
        registerCode(INVALID_PARAMETER, "Invalid parameter");
        registerCode(INVALID_EXCEPTION_CODE, "Invalid exception code passed to T9tException");

        registerCode(UPDATER_NO_CRUDVIEWMODEL_FOR_CLASS, "No CrudViewModel has been registered for the provided DTO class");
        registerCode(UPDATER_UNSUPPORTED_CRUD_TYPE, "The CRUD request implementation of the given type is not supported");
        registerCode(UPDATER_KEY_CLASS_MISMATCH, "Surrogate key CRUD request does not work for provided key type");
        registerCode(UPDATER_AID_INVALID_OPERATION, "Invalid operation for AidDataRequest, only ACTIVATE, INACTIVATE and DELETE are supported");
        registerCode(UPDATER_NO_SEARCH_REQUEST, "No SEARCH request has been defined for CrudViewModel of the given DTO class");

        registerCode(INVALID_LAZY_MEDIADATA_PARAMETERS, "Lazy MediaData without appropriate URL");
        registerCode(FAILED_TO_RESOLVE_MEDIADATA, "Could not resolve lazy MediaData");

        registerCode(HTTP_ERROR + 400, "Bad request");
        registerCode(HTTP_ERROR + 401, "Not authorized");
        registerCode(HTTP_ERROR + 403, "Forbidden");
        registerCode(HTTP_ERROR + 415, "Mediatype not supported");
        registerCode(HTTP_ERROR + 500, "Server error");
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
}
