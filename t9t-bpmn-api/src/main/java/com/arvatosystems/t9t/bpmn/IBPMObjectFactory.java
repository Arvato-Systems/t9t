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
package com.arvatosystems.t9t.bpmn;

public interface IBPMObjectFactory<T> {
    /** Returns the object ref on which a lock should be performed while executing this workflow, or null if no locking is required. */
    Long getRefForLock(Long objectRef);

    /** Reads an object specified by its ref from disk.
     * instances are qualified by the object identifier, for example salesOrder, deliveryOrder etc.
     */
    T read(Long objectRef, Long lockObjectRef, boolean jvmLockAcquired);

    /** Returns the data object for a certain path. */
    Object getVariable(String path, T data);
}
