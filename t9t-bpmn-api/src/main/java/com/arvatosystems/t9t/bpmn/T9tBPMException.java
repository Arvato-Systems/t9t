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

import com.arvatosystems.t9t.base.T9tException;

/**
 * Base exception for all BPM related exception in FortyTwo.
 * @author LIEE001
 */
public class T9tBPMException extends T9tException {

    private static final long serialVersionUID = 22377754899440151L;

    private static final int CORE_OFFSET = 26000;
    private static final int OFFSET = (CL_INTERNAL_LOGIC_ERROR * CLASSIFICATION_FACTOR) + CORE_OFFSET;

    public static final int BPM_EXECUTE_PROCESS_ERROR = OFFSET + 1;
    public static final int BPM_PROCESS_DEFINITION_NOT_EXIST = OFFSET + 2;
    public static final int BPM_GET_DEPLOYMENT_RESOURCE_ERROR = OFFSET + 3;
    public static final int BPM_GET_PROCESS_CONTENT_ERROR = OFFSET + 4;
    public static final int BPM_GET_PROCESS_DIAGRAM_ERROR = OFFSET + 5;
    public static final int BPM_DEPLOYMENT_ERROR = OFFSET + 6;
    public static final int BPM_DEPLOYMENT_MULTI_PROCESS_DEFINITION = OFFSET + 7;
    public static final int BPM_DEPLOYMENT_DATA_NOT_IN_SYNC = OFFSET + 8;
    public static final int BPM_DEPLOYMENT_PROCESS_DEFINITION_ID_NOT_IN_SYNC = OFFSET + 9;
    public static final int BPM_DEPLOYMENT_PROCESS_DEFINITION_EXIST = OFFSET + 10;
    public static final int BPM_AUTO_DEPLOYMENT_ERROR = OFFSET + 11;
    public static final int BPM_PROCESS_CONTENT_ERROR = OFFSET + 12;
    public static final int BPM_DEPLOYMENT_TENANT_PERMISSION = OFFSET + 13;

    public static final int BPM_EXECUTE_JAVA_TASK_RETURNED_NULL = OFFSET + 20;
    public static final int BPM_NO_CURRENT_PROCESS = OFFSET + 21;
    public static final int BPM_CURRENT_PROCESS_EXISTS = OFFSET + 22;

    public static final int BPM_STEP_NOT_FOUND = OFFSET + 50;
    public static final int BPM_OBJECT_FACTORY_NOT_FOUND = OFFSET + 51;
    public static final int BPM_LABEL_NOT_FOUND = OFFSET + 52;
    public static final int BPM_NO_ERROR = OFFSET + 53;
    public static final int BPM_NO_BPMN_ENGINE = OFFSET + 54;
    public static final int BPM_INVALID_VARIABLE_NAME = OFFSET + 55;
    public static final int BPM_NO_LABEL = OFFSET + 56;


    /**
     * static initialization of all error codes
     */
    static {
        codeToDescription.put(BPM_EXECUTE_PROCESS_ERROR, "An error occurred during process execution.");
        codeToDescription.put(BPM_PROCESS_DEFINITION_NOT_EXIST, "Can't found process definition with the given process definition reference.");
        codeToDescription.put(BPM_GET_DEPLOYMENT_RESOURCE_ERROR, "An error occurred during retrieval of BPMN deployment resources.");
        codeToDescription.put(BPM_GET_PROCESS_CONTENT_ERROR, "An error occurred while trying to get BPMN process content.");
        codeToDescription.put(BPM_GET_PROCESS_DIAGRAM_ERROR, "An error occurred while trying to get BPMN process diagram.");
        codeToDescription.put(BPM_DEPLOYMENT_ERROR, "An error occured during BPMN deployment.");
        codeToDescription.put(BPM_DEPLOYMENT_MULTI_PROCESS_DEFINITION, "Attempted to deploy more than 1 process definition in 1 single file.");
        codeToDescription.put(BPM_DEPLOYMENT_DATA_NOT_IN_SYNC, "Deployment data doesn't seems to be in sync with Activiti tables data.");
        codeToDescription.put(BPM_DEPLOYMENT_PROCESS_DEFINITION_ID_NOT_IN_SYNC, "Deployed process definition id data doesn't seems to be in sync with process definition entity configuration data.");
        codeToDescription.put(BPM_DEPLOYMENT_PROCESS_DEFINITION_EXIST, "An error occured during BPMN deployment. Process definition with the same id already exist.");
        codeToDescription.put(BPM_AUTO_DEPLOYMENT_ERROR,           "An error occured during auto deployment.");
        codeToDescription.put(BPM_PROCESS_CONTENT_ERROR,           "An error occurred while trying to decorate/undecorate process definition XML with tenant id information.");
        codeToDescription.put(BPM_DEPLOYMENT_TENANT_PERMISSION,    "Attempted deployment to a tenant to which we have no write permission.");

        codeToDescription.put(BPM_EXECUTE_JAVA_TASK_RETURNED_NULL, "Java task returned null instead of a proper status");
        codeToDescription.put(BPM_NO_CURRENT_PROCESS,              "Expected existing running process, but did not find any");
        codeToDescription.put(BPM_CURRENT_PROCESS_EXISTS,          "Workflow already active");

        codeToDescription.put(BPM_STEP_NOT_FOUND,                  "No implementation found for BPM workflow step");
        codeToDescription.put(BPM_OBJECT_FACTORY_NOT_FOUND,        "No implementation found for BPM object factory");
        codeToDescription.put(BPM_LABEL_NOT_FOUND,                 "Referenced label not found in BPM workflow");
        codeToDescription.put(BPM_NO_ERROR,                        "Workflow returned ERROR, but no (integral) returnCode was set");
        codeToDescription.put(BPM_NO_BPMN_ENGINE,                  "No BPMN Engine found.");
        codeToDescription.put(BPM_INVALID_VARIABLE_NAME,           "A variable of this pathname is not supported.");
        codeToDescription.put(BPM_NO_LABEL,                        "No label provided for workflow step at top level");
    }
}
