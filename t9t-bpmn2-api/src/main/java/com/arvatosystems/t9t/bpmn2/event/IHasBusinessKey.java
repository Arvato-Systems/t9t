/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.event;

/**
 * To correlate messages created by events with particular BPMN process instances, a business key is used.
 * This interface allows different events to provide a business key for this kind of correlation.
 *
 * @author TWEL006
 */
public interface IHasBusinessKey {

    /**
     * Provide the business key for correlation or NULL, if no business key is available.
     *
     * @return Business key or NULL
     */
    String getBusinessKey();

}
