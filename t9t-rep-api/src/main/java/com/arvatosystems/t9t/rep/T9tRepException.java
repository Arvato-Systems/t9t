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
package com.arvatosystems.t9t.rep;

import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in rep module.
 */
public class T9tRepException extends T9tException {
    private static final long serialVersionUID = 5518912336567675416L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = 23000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // CHECKSTYLE.OFF: JavadocVariable
    // CHECKSTYLE.OFF: DeclarationOrder

    // scheduler related exceptions

    public static final int JASPER_REPORT_CREATION_SQL_EXCEPTION = OFFSET + 201;
    public static final int JASPER_REPORT_CREATION_JR_EXCEPTION = OFFSET + 202;
    public static final int JASPER_REPORT_CREATION_IO_EXCEPTION = OFFSET + 203;
    public static final int JASPER_REPORT_PATH_CONFIG_EXCEPTION = OFFSET + 204;
    public static final int BAD_INTERVAL                        = OFFSET + 205;
    public static final int BAD_INTERVAL_CLASS                  = OFFSET + 206;
    public static final int SCHEDULE_REPORT_FAILED              = OFFSET + 208;
    public static final int UNABLE_TO_NOTIFY_REPORT_COMPLETION  = OFFSET + 209;

    public static final int JASPER_REPORT_NOT_SUPPORTED_OUTPUT_FILE_TYPE = OFFSET + 210;

    static {
        initialize();
    }

    /**
     * static initialization of all error codes
     */
    public static void initialize() {
        codeToDescription.put(JASPER_REPORT_CREATION_SQL_EXCEPTION, "During jasper report generation an sql exception occurred.");
        codeToDescription.put(JASPER_REPORT_CREATION_JR_EXCEPTION, "During jasper report generation an jr exception occurred.");
        codeToDescription.put(JASPER_REPORT_CREATION_IO_EXCEPTION, "During jasper report generation an io exception occurred.");
        codeToDescription.put(JASPER_REPORT_NOT_SUPPORTED_OUTPUT_FILE_TYPE, "A output file type was passed that is not supported");
        codeToDescription.put(JASPER_REPORT_PATH_CONFIG_EXCEPTION, "No path configured to load reports from");
        codeToDescription.put(BAD_INTERVAL, "Bad interval");
        codeToDescription.put(BAD_INTERVAL_CLASS, "Unknown interval type");
        codeToDescription.put(SCHEDULE_REPORT_FAILED, "Unable to schedule report");
        codeToDescription.put(UNABLE_TO_NOTIFY_REPORT_COMPLETION, "Unable to send out email for notification");

    }
}
