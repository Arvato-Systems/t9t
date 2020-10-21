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
package com.arvatosystems.t9t.viewmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.AMedia;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionRef;
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowCondition;
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tWorkflow;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepCondition;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepGoto;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepRestart;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepYield;
import com.arvatosystems.t9t.bpmn.UiOnlyWorkflowStep;
import com.arvatosystems.t9t.bpmn.WorkflowStepType;
import com.arvatosystems.t9t.bpmn.request.DeployNewProcessRequest;
import com.arvatosystems.t9t.bpmn.request.DeployProcessRequest;
import com.arvatosystems.t9t.bpmn.request.GetProcessContentRequest;
import com.arvatosystems.t9t.bpmn.request.GetProcessContentResponse;
import com.arvatosystems.t9t.components.ModalWindows;
import com.arvatosystems.t9t.components.crud.CrudSurrogateKeyVM;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.json.JsonParser;
import de.jpaw.util.ByteArray;

@Init(superclass = true)
public class ProcessDefinitionVM
        extends CrudSurrogateKeyVM<ProcessDefinitionRef, ProcessDefinitionDTO, FullTrackingWithVersion> {
    private ByteArray bpmnByte = null;

    //For workflow step configuration
    private UiOnlyWorkflowStep uiOnlyWorkflowStep;
    private List<UiOnlyWorkflowStep> uiOnlyWorkflowSteps;

    @Override
    protected void loadData(DataWithTracking<ProcessDefinitionDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        buildUiOnlyWorkflowSteps();
    }

    @Override
    protected void clearData() {
        super.clearData();
        bpmnByte = null;
        uiOnlyWorkflowStep = new UiOnlyWorkflowStep();
        uiOnlyWorkflowSteps = new ArrayList<>();
    }


    @Override
    protected void saveHook() {
        List<T9tAbstractWorkflowStep> steps = new ArrayList<>(uiOnlyWorkflowSteps.size());

        for (UiOnlyWorkflowStep uiStep : uiOnlyWorkflowSteps) {
            T9tAbstractWorkflowStep step = null;

            if (uiStep.getWorkflowStepType().equals(WorkflowStepType.ADD_PARAMETER)) {
                step = new T9tWorkflowStepAddParameters(uiStep.getLabel(), uiStep.getComment(), uiStep.getParameters());
            } else if (uiStep.getWorkflowStepType().equals(WorkflowStepType.CONDITION)) {
                step = new T9tWorkflowStepCondition(uiStep.getLabel(), uiStep.getComment(), uiStep.getCondition(), uiStep.getThenDo(), uiStep.getElseDo());
            } else if (uiStep.getWorkflowStepType().equals(WorkflowStepType.JAVA_TASK)) {
                step = new T9tWorkflowStepJavaTask(uiStep.getLabel(), uiStep.getComment(), uiStep.getStepName());
            } else if (uiStep.getWorkflowStepType().equals(WorkflowStepType.STEP_GOTO)) {
                step = new T9tWorkflowStepGoto(uiStep.getLabel(), uiStep.getComment(), uiStep.getToLabel());
            } else if (uiStep.getWorkflowStepType().equals(WorkflowStepType.YIELD)) {
                step = new T9tWorkflowStepYield(uiStep.getLabel(), uiStep.getComment(), uiStep.getWaitSeconds());
            } else if (uiStep.getWorkflowStepType().equals(WorkflowStepType.RESTART)) {
                step = new T9tWorkflowStepRestart(uiStep.getLabel(), uiStep.getComment());
            }

            steps.add(step);
        }

        if (this.data.getWorkflow() == null) {
            this.data.setWorkflow(new T9tWorkflow(steps));
        } else {
            this.data.getWorkflow().setSteps(steps);
        }
    }

    @Override
    @Command
    public void commandSave() {
        if (uiOnlyWorkflowSteps.isEmpty()) {
            Messagebox.show(session.translate("processDefinition", "missingSteps"), session.translate("com", "badinput"), Messagebox.OK, Messagebox.INFORMATION);
        }  else {
            super.commandSave();
            if (bpmnByte != null) {
                if (data.getObjectRef() == null) {
                    DeployNewProcessRequest request = new DeployNewProcessRequest(bpmnByte);
                    remoteUtil.executeExpectOk(request);
                } else {
                    DeployProcessRequest request = new DeployProcessRequest(new ProcessDefinitionRef(data.getObjectRef()),bpmnByte);
                    remoteUtil.executeExpectOk(request);
                }
            }
        }
    }

    private void buildUiOnlyWorkflowSteps() {
        uiOnlyWorkflowSteps.clear();
        if (this.getData().getWorkflow() == null || this.getData().getWorkflow().getSteps() == null || this.getData().getWorkflow().getSteps().isEmpty()) {
            return;
        }

        for (T9tAbstractWorkflowStep step : this.getData().getWorkflow().getSteps()) {
            UiOnlyWorkflowStep uiStep = new UiOnlyWorkflowStep();
            uiStep.setLabel(step.getLabel());
            uiStep.setComment(step.getComment());

            if (step instanceof T9tWorkflowStepJavaTask) {
                uiStep.setWorkflowStepType(WorkflowStepType.JAVA_TASK);
                uiStep.setStepName(((T9tWorkflowStepJavaTask) step).getStepName());
            } else if (step instanceof T9tWorkflowStepAddParameters) {
                uiStep.setWorkflowStepType(WorkflowStepType.ADD_PARAMETER);
                uiStep.setParameters(((T9tWorkflowStepAddParameters) step).getParameters());
            } else if (step instanceof T9tWorkflowStepGoto) {
                uiStep.setWorkflowStepType(WorkflowStepType.STEP_GOTO);
                uiStep.setToLabel(((T9tWorkflowStepGoto) step).getToLabel());
            } else if (step instanceof T9tWorkflowStepRestart) {
                uiStep.setWorkflowStepType(WorkflowStepType.RESTART);
            } else if (step instanceof T9tWorkflowStepYield) {
                uiStep.setWorkflowStepType(WorkflowStepType.YIELD);
                uiStep.setWaitSeconds(((T9tWorkflowStepYield) step).getWaitSeconds());
            } else if (step instanceof T9tWorkflowStepCondition) {
                uiStep.setWorkflowStepType(WorkflowStepType.CONDITION);
                T9tWorkflowStepCondition condition = (T9tWorkflowStepCondition) step;
                uiStep.setCondition(condition.getCondition());
                uiStep.setThenDo(condition.getThenDo());
                uiStep.setElseDo(condition.getElseDo());
            }

            uiOnlyWorkflowSteps.add(uiStep);
        }
    }


    @Command
    @NotifyChange("uiOnlyWorkflowSteps")
    public void addSteps() {
        if (CrudMode.NONE != this.getCurrentMode()) {
            UiOnlyWorkflowStep step = new UiOnlyWorkflowStep();
            step.setWorkflowStepType(WorkflowStepType.JAVA_TASK);
            uiOnlyWorkflowSteps.add(step);
        }
    }

    @Command
    @NotifyChange("uiOnlyWorkflowSteps")
    public void removeSteps(@BindingParam("step") UiOnlyWorkflowStep step) {
        if (step != null && CrudMode.NONE!=this.getCurrentMode())
            uiOnlyWorkflowSteps.remove(step);
    }

    @Command
    @NotifyChange("uiOnlyWorkflowSteps")
    public void upSteps(@BindingParam("step") UiOnlyWorkflowStep step) {
        if (step != null && CrudMode.NONE!=this.getCurrentMode()) {
            int i = uiOnlyWorkflowSteps.indexOf(step);
            if (i != 0) {
                uiOnlyWorkflowSteps.remove(step);
                uiOnlyWorkflowSteps.add(i - 1, step);
            }
        }
    }

    @Command
    @NotifyChange("uiOnlyWorkflowSteps")
    public void downSteps(@BindingParam("step") UiOnlyWorkflowStep step) {
        if (step != null && CrudMode.NONE!=this.getCurrentMode()) {
            int i = uiOnlyWorkflowSteps.indexOf(step);
            if (i != uiOnlyWorkflowSteps.size()-1) {
                uiOnlyWorkflowSteps.remove(step);
                uiOnlyWorkflowSteps.add(i + 1, step);
            }
        }
    }

    @Command
    public void uploadBpmn(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws IOException {

        byte[] uploaded = ((UploadEvent) ctx.getTriggerEvent()).getMedia().getByteData();
        if (uploaded != null) {
            bpmnByte = new ByteArray(uploaded);
        }
    }

    @Command
    public void downloadBpmn(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws IOException, ReturnCodeException {

        if (data.getObjectRef() != null) {
            GetProcessContentRequest request = new GetProcessContentRequest(new ProcessDefinitionRef(data.getObjectRef()));
            GetProcessContentResponse response = remoteUtil.executeAndHandle(request, GetProcessContentResponse.class);

            if (response.getContent() != null && response.getContent().getBytes().length != 0) {
                String ctype = "text/plain";
                String format = "content.bpmn20.xml";
                Media media = new AMedia(data.getProcessDefinitionId(), format, ctype, response.getContent().asByteArrayInputStream());
                Filedownload.save(media);
            }
        }
    }

    public UiOnlyWorkflowStep getUiOnlyWorkflowStep() {
        return uiOnlyWorkflowStep;
    }

    public void setUiOnlyWorkflowStep(UiOnlyWorkflowStep uiOnlyWorkflowStep) {
        this.uiOnlyWorkflowStep = uiOnlyWorkflowStep;
    }

    @Command
    public void addWorkflowStep() {

        if (CrudMode.NONE == this.getCurrentMode()) {
            return;
        }

        if (validationOnNewWorkflowStep()) {
            uiOnlyWorkflowSteps.add(uiOnlyWorkflowStep);
        }
    }

    private boolean validationOnNewWorkflowStep() {
        if (uiOnlyWorkflowStep.getLabel() == null || uiOnlyWorkflowStep.getLabel().isEmpty()) {
            showMandatoryError("label");
            return false;
        }

        if (uiOnlyWorkflowStep.getWorkflowStepType() == null) {
            showMandatoryError("workflowStepType");
            return false;
        }

        if (uiOnlyWorkflowStep.getWorkflowStepType().equals(WorkflowStepType.ADD_PARAMETER)) {
            if (uiOnlyWorkflowStep.getParameters() == null || uiOnlyWorkflowStep.getParameters().isEmpty()) {
                showMandatoryError("parameter");
                return false;
            }
        } else if (uiOnlyWorkflowStep.getWorkflowStepType().equals(WorkflowStepType.CONDITION)) {
            if (uiOnlyWorkflowStep.getCondition() == null) {
                showMandatoryError("condition");
                return false;
            }
        } else if (uiOnlyWorkflowStep.getWorkflowStepType().equals(WorkflowStepType.JAVA_TASK)) {
            if (uiOnlyWorkflowStep.getStepName() == null || uiOnlyWorkflowStep.getStepName().isEmpty()) {
                showMandatoryError("stepName");
                return false;
            }
        } else if (uiOnlyWorkflowStep.getWorkflowStepType().equals(WorkflowStepType.STEP_GOTO)) {
            if (uiOnlyWorkflowStep.getToLabel() == null || uiOnlyWorkflowStep.getToLabel().isEmpty()) {
                showMandatoryError("toLabel");
                return false;
            }
        } else if (uiOnlyWorkflowStep.getWorkflowStepType().equals(WorkflowStepType.YIELD)) {
            if (uiOnlyWorkflowStep.getWaitSeconds() == null) {
                showMandatoryError("waitSeconds");
                return false;
            }
        }

        return true;
    }

    private void showMandatoryError(String fieldName) {
        Messagebox.show(session.translate("processDefinition", fieldName), session.translate("com", "com.badinput"),
                Messagebox.OK, Messagebox.ERROR);
    }

    public List<UiOnlyWorkflowStep> getUiOnlyWorkflowSteps() {
        return uiOnlyWorkflowSteps;
    }

    public void setUiOnlyWorkflowSteps(List<UiOnlyWorkflowStep> uiOnlyWorkflowSteps) {
        this.uiOnlyWorkflowSteps = uiOnlyWorkflowSteps;
    }

    @Command
    public void editCondition(@BindingParam("lb") Listbox lb) {

        Info info = new Info(JsonComposerPrettyPrint.toJsonString(uiOnlyWorkflowStep.getCondition()));
        ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {
            Map<String, Object> objectInMap = new JsonParser(d.getText(), false).parseObject();
            BonaPortable bp = MapParser.asBonaPortable(objectInMap, MapParser.OUTER_BONAPORTABLE_FOR_JSON);
            if (bp instanceof T9tAbstractWorkflowCondition) {
                uiOnlyWorkflowStep.setCondition((T9tAbstractWorkflowCondition) bp);
            }
        });
    }

    @Command
    public void editThenDo(@BindingParam("lb") Listbox lb) {

        Info info = new Info(JsonComposerPrettyPrint.toJsonString(uiOnlyWorkflowStep.getThenDo()));
        ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {
            List<Object> objectList = new JsonParser(d.getText(), false).parseArray();
            List<T9tAbstractWorkflowStep> steps = new ArrayList<>(objectList.size());
            for (Object obj : objectList) {
                Object parsed = MapParser.asBonaPortable((Map<String, Object>) obj, MapParser.OUTER_BONAPORTABLE_FOR_JSON);
                if (parsed instanceof T9tAbstractWorkflowStep) {
                    steps.add((T9tAbstractWorkflowStep) parsed);
                }
            }

            uiOnlyWorkflowStep.setThenDo(steps);
        });
    }

    @Command
    public void editElseDo(@BindingParam("lb") Listbox lb) {

        Info info = new Info(JsonComposerPrettyPrint.toJsonString(uiOnlyWorkflowStep.getElseDo()));
        ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {
            List<Object> objectList = new JsonParser(d.getText(), false).parseArray();
            List<T9tAbstractWorkflowStep> steps = new ArrayList<>(objectList.size());
            for (Object obj : objectList) {
                Object parsed = MapParser.asBonaPortable((Map<String, Object>) obj, MapParser.OUTER_BONAPORTABLE_FOR_JSON);
                if (parsed instanceof T9tAbstractWorkflowStep) {
                    steps.add((T9tAbstractWorkflowStep) parsed);
                }
            }

            uiOnlyWorkflowStep.setElseDo(steps);
        });
    }

}
