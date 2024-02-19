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
package com.arvatosystems.t9t.auth.jwt;

import de.jpaw.util.ApplicationException;

public class T9tJwtException extends ApplicationException {

    private static final long serialVersionUID = 365133524234L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET            = 25000;  // cannot reference T9tConstants due to missing dependency (it is also not wanted to add it!)
    private static final int OFFSET_LOGIC_ERROR     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_INTERNAL_LOGIC_ERROR;

    // decoding issues
    public static final int NUMBER_SEGMENTS         = OFFSET_LOGIC_ERROR + 1;
    public static final int MISSING_SIGNATURE       = OFFSET_LOGIC_ERROR + 2;
    public static final int ALGORITHM_NOT_SUPPORTED = OFFSET_LOGIC_ERROR + 3;
    public static final int VERIFICATION_FAILED     = OFFSET_LOGIC_ERROR + 4;

    // encoding issues



    public T9tJwtException(final int errorCode) {
        super(errorCode);
    }

    /**
     * Creates an exception for a specific error code.
     * Please do not put redundant text (duplicating the text of the error code) into detailParameter, only additional info.
     *
     * @param errorCode
     *            The unique code describing the error cause.
     * @param params
     *            Any additional informations / parameters.
     *            Do not put redundant text from the error code itself here! In most cases this should be just the value causing the problem.
     */
    public T9tJwtException(final int errorCode, final String params) {
        super(errorCode, params);
    }

    /**
     * Uploads textual descriptions only once they're needed for this type of exception class.
     * The idea is that in working environments, we will never need them ;-). There is a small chance of
     * duplicate initialization, because the access to the flag textsInitialized is not synchronized, but duplicate upload does not hurt (is idempotent).
     */
    static {
        registerRange(CORE_OFFSET, false, T9tJwtException.class, ApplicationLevelType.FRAMEWORK, "t9t JWT module");

        registerCode(NUMBER_SEGMENTS,          "Not enough or too many segments in base64 encoded token");
        registerCode(MISSING_SIGNATURE,        "Unsigned JWT - Signature is required for this application");
        registerCode(ALGORITHM_NOT_SUPPORTED,  "Algorithm not supported");
        registerCode(VERIFICATION_FAILED,      "Signature verification failed");
    }


    /** returns a text representation of an error code */
    public static String codeToString(final int errorCode) {
        return new T9tJwtException(errorCode).getStandardDescription();
    }
}
