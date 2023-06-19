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
package com.arvatosystems.t9t.doc;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

/**
 * This class contains all exception codes used in doc module.
 */
public class T9tDocExtException extends T9tException {
    private static final long serialVersionUID = -7128619681841773722L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = T9tConstants.EXCEPTION_OFFSET_DOC_EXT;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // CHECKSTYLE.OFF: JavadocVariable
    // CHECKSTYLE.OFF: DeclarationOrder

    // comm related exceptions
    public static final int DOCUMENT_CREATION_ERROR         = OFFSET + 100;
    public static final int DOCUMENT_DOWNLOAD_ERROR         = OFFSET + 110;
    public static final int UNDEFINED_MIMETYPE_ERROR        = OFFSET + 111;


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
        registerCode(DOCUMENT_CREATION_ERROR,          "Error occured during document creation.");
        registerCode(DOCUMENT_DOWNLOAD_ERROR,          "Error occured during file download");
        registerCode(UNDEFINED_MIMETYPE_ERROR,         "Unrecognised file mime type was received.");

        registerCode(TEXT_TOO_LONG_FOR_BARCODE,        "Provided text is too long for selected barcode");
        registerCode(UNSUPPORTED_CHARACTERS,           "Provided text contains characters not supported by selected barcode.");

        registerCode(EMAIL_CONFIGURATION_ERROR,        "Error occured during loading configuration email.");
        registerCode(EMAIL_SEND_ERROR,                 "Error occured during sending email.");
        registerCode(EMAIL_RECIPIENT_ERROR,            "Recipient cannot be null or empty.");
        registerCode(TEMPLATE_CREATION_ERROR,          "Template cannot be null.");
        registerCode(RESOURCE_TYPE_NOT_FOUND_ERROR,    "Resource type not found.");
        registerCode(IMAGE_NOT_FOUND_ERROR,            "Image file not found.");
        registerCode(CONFIGURATION_NOT_FOUND_ERROR,    "Configuration data not found.");
        registerCode(TEMPLATE_UNSUPPORTED_ENCODING,    "Unsupported encoding for template.");
    }
}
