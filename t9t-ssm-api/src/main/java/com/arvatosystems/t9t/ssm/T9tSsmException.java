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
package com.arvatosystems.t9t.ssm;

import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in ssm module.
 */
public class T9tSsmException extends T9tException {
    private static final long serialVersionUID = -2311386486424675268L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = 22000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;
    @SuppressWarnings("unused")
    private static final int OFFSET_LOGIC_ERROR = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // CHECKSTYLE.OFF: JavadocVariable
    // CHECKSTYLE.OFF: DeclarationOrder

    // scheduler related exceptions

    public static final int SERVICE_SESSION_OPEN_EXCEPTION = OFFSET + 200;
    public static final int SCHEDULER_JOB_NOT_FOUND_EXCEPTION = OFFSET + 201;
    public static final int SCHEDULER_INIT_OR_START_EXCEPTION = OFFSET + 202;
    public static final int SCHEDULER_SHUTDOWN_EXCEPTION = OFFSET + 203;

    public static final int SCHEDULER_DELETE_JOB_EXCEPTION = OFFSET + 204;
    public static final int SCHEDULER_UPDATE_JOB_EXCEPTION = OFFSET + 205;
    public static final int SCHEDULER_CREATE_JOB_EXCEPTION = OFFSET + 207;

    public static final int SCHEDULER_READ_EXCEPTION = OFFSET + 208;
    public static final int VALIDATION_CREATION_NO_TENANT_REF = OFFSET + 209;
    public static final int SERVICESESSION_IS_NULL = OFFSET + 210;
    public static final int SCHEDULER_JOB_EXECUTION_FAILURE = OFFSET + 211;

    public static final int SCHEDULER_SETUP_FAILURE_INTERVAL = OFFSET + 212;
    public static final int SCHEDULE_VALID_FROM_NOT_PROVIDED = OFFSET + 213;
    public static final int SCHEDULE_EXECUTION_TIME_MISSING = OFFSET + 214;
    public static final int SCHEDULE_CRON_EXPRESSION_MISSING = OFFSET + 215;
    public static final int SCHEDULE_CRON_REGEX_PATTERN_MISMATCH = OFFSET + 216;


    public static final int SCHEDULE_SETUP_INTERVAL_VALIDATION_ERR = OFFSET + 220;
    public static final int IRRELEVANT_SCHEDULER_PARAM_ERR = OFFSET + 221;
    public static final int REQUIRED_SCHEDULER_PARAM_MISSING = OFFSET + 222;
    public static final int SCHEDULE_SETUP_PARAM_VALIDATION_ERR = OFFSET + 223;

    /**
     * static initialization of all error codes
     */
    static {
        codeToDescription.put(SCHEDULER_JOB_NOT_FOUND_EXCEPTION, "The scheduler contains not job with passed parameters.");
        codeToDescription.put(SCHEDULER_INIT_OR_START_EXCEPTION, "The scheduler could not be started correctly.");
        codeToDescription.put(SCHEDULER_SHUTDOWN_EXCEPTION, "The scheduler could not be shut down correctly.");
        codeToDescription.put(SCHEDULER_DELETE_JOB_EXCEPTION, "The scheduler was not able to delete a job.");
        codeToDescription.put(SCHEDULER_UPDATE_JOB_EXCEPTION, "The scheduler was not able to update a job.");
        codeToDescription.put(SCHEDULER_CREATE_JOB_EXCEPTION, "The scheduler was not able to create a job.");
        codeToDescription.put(SCHEDULER_READ_EXCEPTION, "The scheduler was not able to read a job.");
        codeToDescription.put(SERVICE_SESSION_OPEN_EXCEPTION, "The service session could not be opened.");
        codeToDescription.put(VALIDATION_CREATION_NO_TENANT_REF, "Tenant Ref has to be either in InternalHeaderParameters or in ReportDTO");
        codeToDescription.put(SERVICESESSION_IS_NULL, "The service session for the job execution is null");
        codeToDescription.put(SCHEDULER_JOB_EXECUTION_FAILURE, "Failed to execute job");
        codeToDescription.put(SCHEDULER_SETUP_FAILURE_INTERVAL, "If a minute interval should be used the fields for start hour, end hour and minute interval have to be filled");
        codeToDescription.put(SCHEDULE_VALID_FROM_NOT_PROVIDED, "Please provided Valid From Date.");
        codeToDescription.put(SCHEDULE_EXECUTION_TIME_MISSING, "Execution Time is missing");
        codeToDescription.put(SCHEDULE_CRON_EXPRESSION_MISSING, "Required cronExpression is missing");
        codeToDescription.put(SCHEDULE_CRON_REGEX_PATTERN_MISMATCH, "CRON native doesn't comply with CRON expression pattern");

        codeToDescription.put(SCHEDULE_SETUP_INTERVAL_VALIDATION_ERR, "Interval parameter validation failed for the scheduler");
        codeToDescription.put(IRRELEVANT_SCHEDULER_PARAM_ERR, "Found irrelevant scheduler parameter");
        codeToDescription.put(REQUIRED_SCHEDULER_PARAM_MISSING, "Missing relevant scheduler parameter.");
        codeToDescription.put(SCHEDULE_SETUP_PARAM_VALIDATION_ERR, "Validation on required setup parameter failed");
    }
}
