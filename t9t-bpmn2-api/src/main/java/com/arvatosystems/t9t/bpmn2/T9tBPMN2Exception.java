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
package com.arvatosystems.t9t.bpmn2;

import com.arvatosystems.t9t.bpmn.T9tBPMException;

/**
 * Base exception for all BPMN 2 related exception in FortyTwo.
 */
public class T9tBPMN2Exception extends T9tBPMException {

    private static final long serialVersionUID = 22377754899440151L;

    private static final int CORE_OFFSET = 27000;
    private static final int OFFSET = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    public static final int BPMN2_MESSAGE_DELIVERY_FAILED = OFFSET + 1;


    /**
     * static initialization of all error codes
     */
    static {
        codeToDescription.put(BPMN2_MESSAGE_DELIVERY_FAILED, "BPMN Message could not be delivered - no subcription available");
    }
}
