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
package com.arvatosystems.t9t.embedded.tests.bpm;

import java.util.Collections;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole;
import com.arvatosystems.t9t.base.ITestConnection;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.T9tWorkflow;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefRequest;
import com.arvatosystems.t9t.bpmn.request.ExecuteProcessWithRefResponse;
import com.arvatosystems.t9t.bpmn.request.ProcessExecutionStatusSearchRequest;
import com.arvatosystems.t9t.bpmn.request.TriggerSingleProcessNowRequest;
import com.arvatosystems.t9t.embedded.connect.InMemoryConnection;
import com.arvatosystems.t9t.embedded.tests.bpmn.BPMTestThrowExceptionStep;
import com.arvatosystems.t9t.misc.extensions.BpmExtensions;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.NoTracking;

/**
 * Test that an unhandled exception within a workflow step result (nevertheless) in an updated process status with details of the exception.
 * UpdateErrorStatusRequestHandler should handle this.
 */
@SuppressWarnings("all")
public class BpmnErrorStatusWithExceptionTest extends AbstractBpmnTest {
    private static ITestConnection dlg;

    @BeforeAll
    public static void createConnection() {
        dlg = new InMemoryConnection();
    }

    @Test
    public void test() {
        final SetupUserTenantRole setup = new SetupUserTenantRole(dlg);
        setup.createUserTenantRole("bpmnExcTest1", UUID.randomUUID(), true);

        // build workflow/process
        final ProcessDefinitionDTO processDefinition = new ProcessDefinitionDTO();
        processDefinition.setIsActive(true);
        processDefinition.setProcessDefinitionId("throwExcepTest");
        processDefinition.setName("test that error status is set in case of thrown exception");

        final T9tWorkflow workflow = new T9tWorkflow();

        final T9tWorkflowStepJavaTask throwExceptionStep = new T9tWorkflowStepJavaTask();
        throwExceptionStep.setLabel("Throw Exception");
        throwExceptionStep.setStepName(BPMTestThrowExceptionStep.STEP_NAME);

        workflow.setSteps(Collections.singletonList(throwExceptionStep));

        processDefinition.setWorkflow(workflow);
        BpmExtensions.merge(processDefinition, dlg);

        // run it!
        final ExecuteProcessWithRefRequest executeProcess = new ExecuteProcessWithRefRequest();
        executeProcess.setTargetObjectRef(Long.valueOf(28L));
        executeProcess.setProcessDefinitionId("throwExcepTest");
        executeProcess.setInitialDelay(Integer.valueOf(0));

        final Long ref = dlg.typeIO(executeProcess, ExecuteProcessWithRefResponse.class).getProcessCtrlRef();
        final TriggerSingleProcessNowRequest triggerProcessNowRequest = new TriggerSingleProcessNowRequest();
        triggerProcessNowRequest.setProcessStatusRef(ref);

        try {
            dlg.okIO(triggerProcessNowRequest); // this will result in an exception
        } catch (final Exception exc) {
            // nothing todo...
        }

        // check error status
        final ProcessExecutionStatusSearchRequest statusSearchRequest = new ProcessExecutionStatusSearchRequest();
        final ReadAllResponse resp = dlg.typeIO(statusSearchRequest, ReadAllResponse.class);

        Assertions.assertEquals(1, resp.getDataList().size(), "mismatch in number of process exec status entries");
        if (resp.getDataList().size() > 0) {
            final Object status = resp.getDataList().get(0);
            final DataWithTrackingS<ProcessExecutionStatusDTO, NoTracking> castedStatus = (DataWithTrackingS<ProcessExecutionStatusDTO, NoTracking>) status;
            final ProcessExecutionStatusDTO dto = castedStatus.getData();
            Assertions.assertEquals(BPMTestThrowExceptionStep.ERROR_CODE, dto.getReturnCode());
            Assertions.assertNotNull(dto.getErrorDetails());
            Assertions.assertTrue(dto.getErrorDetails().contains(BPMTestThrowExceptionStep.ERROR_DETAILS));
        }
    }

}
