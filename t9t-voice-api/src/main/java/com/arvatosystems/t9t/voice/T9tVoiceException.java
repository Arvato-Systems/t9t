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
package com.arvatosystems.t9t.voice;

import com.arvatosystems.t9t.base.T9tException;

/**
 * exception class for all t9t voice module specific exceptions.
 *
 */
public class T9tVoiceException extends T9tException {
    private static final long serialVersionUID = -866589603331210L;
    private static final int OFFSET = (CL_PARAMETER_ERROR * CLASSIFICATION_FACTOR) + 178000;

    // Error codes
    public static final int UNKNOWN_SKU_KEY_IMPLEMENTATION = OFFSET + 59;

    static {
        codeToDescription.put(UNKNOWN_SKU_KEY_IMPLEMENTATION, "unknown sku key implementation");
    }
}
