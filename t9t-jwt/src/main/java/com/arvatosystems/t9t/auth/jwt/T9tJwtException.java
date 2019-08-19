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
package com.arvatosystems.t9t.auth.jwt;

import de.jpaw.util.ApplicationException;

public class T9tJwtException extends ApplicationException {

    private static final long serialVersionUID = 365133524234L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET            = 510000;
    private static final int OFFSET                 = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_INTERNAL_LOGIC_ERROR;

    // decoding issues
    public static final int NUMBER_SEGMENTS         = OFFSET + 1;
    public static final int MISSING_SIGNATURE       = OFFSET + 2;
    public static final int ALGORITHM_NOT_SUPPORTED = OFFSET + 3;
    public static final int VERIFICATION_FAILED     = OFFSET + 4;

    // encoding issues



    public T9tJwtException(int errorCode) {
        super(errorCode);
    }

    /**
     * Create an exception for a specific error code. Please do not put redundant text (duplicating the text of the error code) into detailParameter, only additional info.
     *
     * @param errorCode
     *            The unique code describing the error cause.
     * @param params
     *            Any additional informations / parameters. Do not put redundant text from the error code itself here! In most cases this should be just the value causing the problem.
     */
    public T9tJwtException(int errorCode, String params) {
        super(errorCode, params);
    }

    /**
     * Method uploads textual descriptions only once they're needed for this type of exception class. The idea is that in working environments, we will never need them ;-). There is a small chance of
     * duplicate initialization, because the access to the flag textsInitialized is not synchronized, but duplicate upload does not hurt (is idempoten.t)
     */
    static {
        codeToDescription.put(NUMBER_SEGMENTS,          "Not enough or too many segments in base64 encoded token");
        codeToDescription.put(MISSING_SIGNATURE,        "Unsigned JWT - Signature is required for this application");
        codeToDescription.put(ALGORITHM_NOT_SUPPORTED,  "Algorithm not supported");
        codeToDescription.put(VERIFICATION_FAILED,      "Signature verification failed");
    }


    /** returns a text representation of an error code */
    public static String codeToString(int errorCode) {
        return new T9tJwtException(errorCode).getStandardDescription();
    }
}
