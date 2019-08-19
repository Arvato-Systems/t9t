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
package com.arvatosystems.t9t.bpmn;

import java.util.Map;

import de.jpaw.util.ApplicationException;

/** Defines the interface of an abstract base class used for all kinds of generation 2 workflows (workflows which do not persist the object as a first step). */
public interface IWorkflowStep<T> {

    /** Every step must return the factory to read objects from the DB. This serves as a plausi to validate
     * that the composition of steps is consistent (i.e. no SalesOrder step linked to a DeliveryOrderStep).
     * If this method returns null, then the factory doesn't matter to the step (for example because it's a just an unspecific logger). */
    String getFactoryName();
    /** Executes the workflow step on data of type T.
     * @param data the object to work on
     * @param a JSON object of parameters provided by the BPMN workflow. This for example holds the documentTemplateId for user documents.
     *
     * @return true if the execution should terminate now (does not imply the workflow is complete).
     * @throws ApplicationException
     */
    WorkflowReturnCode execute(T data, Map<String, Object> parameters);

    /** method to determine if this step may be executed now.
        Called before execute() is invoked. */
    WorkflowRunnableCode mayRun(T data, Map<String, Object> parameters);
}
