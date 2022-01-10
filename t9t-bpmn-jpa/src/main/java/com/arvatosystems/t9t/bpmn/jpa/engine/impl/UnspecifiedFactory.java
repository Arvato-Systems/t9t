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
package com.arvatosystems.t9t.bpmn.jpa.engine.impl;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.bpmn.IBPMObjectFactory;
import com.arvatosystems.t9t.bpmn.T9tBPMException;

/** A dummy object which is used in workflow execution when no factory has been specified. */
public final class UnspecifiedFactory {
    public static final UnspecifiedFactory INSTANCE = new UnspecifiedFactory();
    public static final UFactory FACTORY = new UFactory();

    private UnspecifiedFactory() {
    }

    /** A factory to return the INSTANCE. */
    public static final class UFactory implements IBPMObjectFactory<Object> {
        private UFactory() {
        }

        @Override
        public Long getRefForLock(final Long objectRef) {
            return null; // no locking desired
        }

        @Override
        public Object read(final Long objectRef, final Long lockObjectRef, final boolean jvmLockAcquired) {
            return INSTANCE; // returns the same instance for any parameter
        }

        @Override
        public Object getVariable(final String path, final Object workflowData) {
            throw new T9tException(T9tBPMException.BPM_INVALID_VARIABLE_NAME, path);
        }
    }
}
