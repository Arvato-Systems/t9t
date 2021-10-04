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
package com.arvatosystems.t9t.io;

import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all output module specific exceptions.
 *
 *
 */
public class T9tIOException extends T9tException {
    private static final long serialVersionUID = -8665896096651910L;

    private static final int CORE_OFFSET = 28000;
    private static final int OFFSET                     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;
    private static final int OFFSET_DENIED              = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_DENIED;

    // Decline codes: This is not a technical error in order to allow the status to be written
    public static final int NOT_TRANSFERRED             = OFFSET_DENIED + 333;

    // Error codes
    public static final int NO_RECORD_BASED_OUTPUT      = OFFSET + 100;
    public static final int NO_FOLDING_SUPPORT          = OFFSET + 101;
    public static final int IO_EXCEPTION                = OFFSET + 102;

    public static final int WRONG_RECORD_TYPE           = OFFSET + 103;
    public static final int IMPORT_FINISHED_WITH_ERRORS = OFFSET + 104;

    // File specific
    public static final int OUTPUT_FILE_IS_DIRECTORY    = OFFSET + 120;
    public static final int OUTPUT_FILE_PATH_NOT_ABSOLUTE = OFFSET + 121;
    public static final int OUTPUT_FILE_OPEN_EXCEPTION  = OFFSET + 122;

    // Xml specific
    public static final int NO_JAXB_CONTEXT_PATH        = OFFSET + 200;
    public static final int XML_MARSHALLING_ERROR       = OFFSET + 201;
    public static final int XML_SETUP_ERROR             = OFFSET + 202;
    public static final int XML_SET_PROPERTY_ERROR      = OFFSET + 203;



    // IOutputSession specific
    public static final int FORMAT_UNSPECIFIED = OFFSET + 230;
    public static final int FORMAT_MISMATCH = OFFSET + 231;
    public static final int FORMAT_NO_STRUCTURED = OFFSET + 232;
    public static final int OUTPUT_FILE_EXCEPTION = OFFSET + 233;
    public static final int OUTPUT_JMS_EXCEPTION = OFFSET + 234;
    public static final int OUTPUT_XML_EXCEPTION = OFFSET + 235;
    public static final int OUTPUT_JSON_EXCEPTION = OFFSET + 236;
    public static final int OUTPUT_PRE_TRANSFORMER_NOT_FOUND = OFFSET + 237;
    public static final int OUTPUT_COMM_FORMAT_GENERATOR_NOT_FOUND = OFFSET + 238;
    public static final int OUTPUT_COMM_CHANNEL_NOT_FILE = OFFSET + 239;
    public static final int OUTPUT_COMM_CHANNEL_IO_ERROR = OFFSET + 240;
    public static final int OUTPUT_COMM_CHANNEL_REQUIRED = OFFSET + 241;
    public static final int OUTPUT_COMM_CHANNEL_NO_SRC_HANDLER = OFFSET + 242;
    public static final int FORBIDDEN_FILE_PATH_ELEMENTS = OFFSET + 253;
    public static final int FAILED_TO_BUILD_ABSOLUTE_PATH = OFFSET + 254;
    public static final int FAILED_TO_PREPARE_OUTPUT_LOCATION = OFFSET + 255;
    public static final int TOO_MANY_COLUMNS_FOR_EXCEL_EXPORT = OFFSET + 256;
    public static final int UNDEFINED_CAMEL_SUCCESS_DEST_PATH_ERROR = OFFSET + 257;
    public static final int UNDEFINED_CAMEL_FAILURE_DEST_PATH_ERROR = OFFSET + 258;
    public static final int FILE_TOO_BIG = OFFSET + 259;

    public static final int MISSING_KAFKA_CONFIGURAION = OFFSET + 270;

    static {
        codeToDescription.put(NOT_TRANSFERRED,              "Camel transfer not successful");

        codeToDescription.put(NO_RECORD_BASED_OUTPUT,       "Output format does not support record based output");
        codeToDescription.put(NO_FOLDING_SUPPORT,           "Output format does not support selection of specific columns (folding)");
        codeToDescription.put(IO_EXCEPTION,                 "Input/Output exception");
        codeToDescription.put(WRONG_RECORD_TYPE,            "Received record of wrong data type");
        codeToDescription.put(IMPORT_FINISHED_WITH_ERRORS,  "Data import finished - not all records successful");

        codeToDescription.put(OUTPUT_FILE_IS_DIRECTORY,     "Specified output file name is a directory");
        codeToDescription.put(OUTPUT_FILE_PATH_NOT_ABSOLUTE,"Output file path is not absolute (required for security reasons)");
        codeToDescription.put(OUTPUT_FILE_OPEN_EXCEPTION,   "Exception during open file");

        codeToDescription.put(NO_JAXB_CONTEXT_PATH,         "XML output: No Jaxb context path has been configured for this data sink");
        codeToDescription.put(XML_MARSHALLING_ERROR,        "JAXB XML Marshalling error");
        codeToDescription.put(XML_SETUP_ERROR,              "Exception during JAXB context or marshaller creation");
        codeToDescription.put(XML_SET_PROPERTY_ERROR,       "Exception during JAXB property setting");

        // old stuff
        codeToDescription.put(OUTPUT_FILE_EXCEPTION, "Failed to output records to a file. Please make sure the file path is correct and it's writable");
        codeToDescription.put(OUTPUT_JMS_EXCEPTION, "Failed to output records to a JMS destination. Please make sure the queue/topic is accessible");
        codeToDescription.put(OUTPUT_XML_EXCEPTION, "Error while serializing output records to XML format");
        codeToDescription.put(OUTPUT_JSON_EXCEPTION, "Error while serializing output records to JSON format");
        codeToDescription.put(OUTPUT_PRE_TRANSFORMER_NOT_FOUND, "Unable to lookup output session pre-transformer");
        codeToDescription.put(OUTPUT_COMM_FORMAT_GENERATOR_NOT_FOUND, "Unable to lookup communication format generator");
        codeToDescription.put(DATASINK_UNSUPPORTED_FORMAT, "The requested format is not available for the category.");
        codeToDescription.put(DATASINK_UNSUPPORTED_ENCODING, "Unsupported datasink encoding.");
        codeToDescription.put(OUTPUT_COMM_CHANNEL_NOT_FILE, "Requested a download for a sink which does not correspond to a file");
        codeToDescription.put(OUTPUT_COMM_CHANNEL_IO_ERROR, "I/O error during file read");
        codeToDescription.put(OUTPUT_COMM_CHANNEL_REQUIRED, "explicit communication format channel required for uploads");
        codeToDescription.put(FORBIDDEN_FILE_PATH_ELEMENTS, "Forbidden file path element.");
        codeToDescription.put(FAILED_TO_BUILD_ABSOLUTE_PATH, "An error occurred on building absolute path.");
        codeToDescription.put(FAILED_TO_PREPARE_OUTPUT_LOCATION, "Failed to prepare output file location.");
        codeToDescription.put(TOO_MANY_COLUMNS_FOR_EXCEL_EXPORT, "Can't export report to excel (xls) because of too many columns (> 255).");
        codeToDescription.put(UNDEFINED_CAMEL_SUCCESS_DEST_PATH_ERROR, "Unknown destination where to move a file after successful Camel routing.");
        codeToDescription.put(UNDEFINED_CAMEL_FAILURE_DEST_PATH_ERROR, "Unknown destination where to move a file after failed Camel routing.");
        codeToDescription.put(OUTPUT_COMM_CHANNEL_NO_SRC_HANDLER, "No data source handler available for type");
        codeToDescription.put(FILE_TOO_BIG, "The file size is too big to be handled.");

        codeToDescription.put(MISSING_KAFKA_CONFIGURAION, "No / missing configuration for kafka");
    }
}
