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

import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all t9t doc module specific exceptions.
 *
 */
public class T9tDocException extends T9tException {
    private static final long serialVersionUID = -8665896096631210L;

    private static final int CORE_OFFSET = 34000;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    // Error codes
    public static final int CONVERSION_EXCEEDS_MAX_TEMPLATE_SIZE = OFFSET + 60;
    public static final int CONVERSION_EXCEEDS_MAX_SUBJECT_SIZE  = OFFSET + 61;
    public static final int CANNOT_ADD_FONT_IO                   = OFFSET + 62;
    public static final int CANNOT_ADD_FONT_DOC                  = OFFSET + 63;
    public static final int DOCUMENT_PDF_CONVERSION_ERROR_IO     = OFFSET + 64;
    public static final int DOCUMENT_PDF_CONVERSION_ERROR_DOC    = OFFSET + 65;
    public static final int FORMATTING_ERROR                     = OFFSET + 66;

    static {
        registerCode(CONVERSION_EXCEEDS_MAX_TEMPLATE_SIZE, "The conversion would exceed the maximum size of a template.");
        registerCode(CONVERSION_EXCEEDS_MAX_SUBJECT_SIZE,  "The conversion would exceed the maximum size of an inline email subject template.");
        registerCode(CANNOT_ADD_FONT_IO,                   "Could not add font to document (I/O issue)");
        registerCode(CANNOT_ADD_FONT_DOC,                  "Could not add font to document (Doc issue)");
        registerCode(DOCUMENT_PDF_CONVERSION_ERROR_IO,     "Could not convert document to PDF (I/O issue)");
        registerCode(DOCUMENT_PDF_CONVERSION_ERROR_DOC,    "Could not convert document to PDF (Doc issue)");
        registerCode(FORMATTING_ERROR,                     "Problem formatting the document (malformed template?)");
    }
}
