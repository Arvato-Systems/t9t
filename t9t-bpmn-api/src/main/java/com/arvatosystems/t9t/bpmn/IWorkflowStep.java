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
package com.arvatosystems.t9t.bpmn;

import java.time.Instant;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/** Defines the interface of an abstract base class used for all kinds of generation 2 workflows (workflows which do not persist the object as a first step). */
public interface IWorkflowStep<T> {
    Instant YIELD_UNTIL_FAR_FUTURE           = Instant.ofEpochSecond(3_2503_676_400L); // Wed Jan 01 3000 00:00:00 GMT+0100
    String PROCESS_VARIABLE_VARIANT          = "variant";       // the name in the parameters map used to transfer additional numeric configuration
    String PROCESS_VARIABLE_YIELD_UNTIL      = "yieldUntil";    // the name in the parameters map used to denote a pause
    String PROCESS_VARIABLE_RETURN_CODE      = "returnCode";    // the name in the parameters map used to denote the return code
    String PROCESS_VARIABLE_ERROR_DETAILS    = "errorDetails";  // the name in the parameters map used to store error details

    /**
     * Every step must return the factory to read objects from the DB. This serves as a plausi to validate
     * that the composition of steps is consistent (i.e. no SalesOrder step linked to a DeliveryOrderStep).
     * If this method returns null, then the factory doesn't matter to the step (for example because it's a just an unspecific logger).
     */
    @Nullable
    String getFactoryName();

    /**
     * Executes the workflow step on data of type T.
     *
     * @param data the object to work on
     * @param a JSON object of parameters provided by the BPMN workflow. This for example holds the documentTemplateId for user documents.
     *
     * @return true if the execution should terminate now (does not imply the workflow is complete).
     */
    WorkflowReturnCode execute(@Nonnull T data, @Nonnull Map<String, Object> parameters);

    /**
     * Determines if this step may be executed now.
     * Called before execute() is invoked.
     */
    WorkflowRunnableCode mayRun(@Nonnull T data, @Nonnull Map<String, Object> parameters);
}
