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
package com.arvatosystems.t9t.doc;

import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in doc module.
 */
public class T9tDocExtException extends T9tException {
    private static final long serialVersionUID = -7128619681841773722L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = 24000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // CHECKSTYLE.OFF: JavadocVariable
    // CHECKSTYLE.OFF: DeclarationOrder

    // comm related exceptions
    public static final int DOCUMENT_CREATION_ERROR         = OFFSET + 100;
    public static final int DOCUMENT_DOWNLOAD_ERROR         = OFFSET + 110;
    public static final int UNDEFINED_MIMETYPE_ERROR        = OFFSET + 110;


    // barcode related exceptions
    public static final int TEXT_TOO_LONG_FOR_BARCODE       = OFFSET + 210;
    public static final int UNSUPPORTED_CHARACTERS          = OFFSET + 211;

    // communication related exceptions
    public static final int EMAIL_CONFIGURATION_ERROR       = OFFSET + 966;
    public static final int EMAIL_SEND_ERROR                = OFFSET + 967;
    public static final int EMAIL_RECIPIENT_ERROR           = OFFSET + 968;
    public static final int TEMPLATE_CREATION_ERROR         = OFFSET + 969;
    public static final int RESOURCE_TYPE_NOT_FOUND_ERROR   = OFFSET + 970;
    public static final int IMAGE_NOT_FOUND_ERROR           = OFFSET + 971;
    public static final int CONFIGURATION_NOT_FOUND_ERROR   = OFFSET + 972;
    public static final int TEMPLATE_UNSUPPORTED_ENCODING   = OFFSET + 973;

    /**
     * static initialization of all error codes
     */
    static {
        codeToDescription.put(DOCUMENT_CREATION_ERROR,          "Error occured during document creation.");
        codeToDescription.put(DOCUMENT_DOWNLOAD_ERROR,          "Error occured during file download");
        codeToDescription.put(UNDEFINED_MIMETYPE_ERROR,         "Unrecognised file mime type was received.");

        codeToDescription.put(TEXT_TOO_LONG_FOR_BARCODE,        "Provided text is too long for selected barcode");
        codeToDescription.put(UNSUPPORTED_CHARACTERS,           "Provided text contains characters not supported by selected barcode.");

        codeToDescription.put(EMAIL_CONFIGURATION_ERROR,        "Error occured during loading configuration email.");
        codeToDescription.put(EMAIL_SEND_ERROR,                 "Error occured during sending email.");
        codeToDescription.put(EMAIL_RECIPIENT_ERROR,            "Recipient cannot be null or empty.");
        codeToDescription.put(TEMPLATE_CREATION_ERROR,          "Template cannot be null.");
        codeToDescription.put(RESOURCE_TYPE_NOT_FOUND_ERROR,    "Resource type not found.");
        codeToDescription.put(IMAGE_NOT_FOUND_ERROR,            "Image file not found.");
        codeToDescription.put(CONFIGURATION_NOT_FOUND_ERROR,    "Configuration data not found.");
        codeToDescription.put(TEMPLATE_UNSUPPORTED_ENCODING,    "Unsupported encoding for template.");
    }
}
