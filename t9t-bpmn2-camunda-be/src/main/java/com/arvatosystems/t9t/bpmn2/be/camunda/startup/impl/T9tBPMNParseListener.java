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
package com.arvatosystems.t9t.bpmn2.be.camunda.startup.impl;

import org.camunda.bpm.engine.impl.bpmn.parser.AbstractBpmnParseListener;
import org.camunda.bpm.engine.impl.core.model.CoreModelElement;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.impl.pvm.process.TransitionImpl;
import org.camunda.bpm.engine.impl.util.xml.Element;

import com.arvatosystems.t9t.bpmn2.be.camunda.listener.ExecuteFinalWorkflowStepListener;
import com.arvatosystems.t9t.bpmn2.be.camunda.listener.ProcessExecutionLogListener;
import com.arvatosystems.t9t.bpmn2.be.camunda.listener.WorkflowStartContextListener;

import de.jpaw.dp.Jdp;

public class T9tBPMNParseListener extends AbstractBpmnParseListener {

    private static void addLogListener(CoreModelElement element, String eventName) {
        if (ProcessExecutionLogListener.isEnabled()) {
            element.addListener(eventName, Jdp.getRequired(ProcessExecutionLogListener.class));
        }
    }

    @Override
    public void parseSequenceFlow(Element sequenceFlowElement, ScopeImpl scopeElement, TransitionImpl transition) {
        addLogListener(transition, WorkflowStartContextListener.EVENTNAME_TAKE);
    }

    @Override
    public void parseCallActivity(Element callActivityElement, ScopeImpl scope, ActivityImpl activity) {
        addLogListener(activity, WorkflowStartContextListener.EVENTNAME_START);
        addLogListener(activity, WorkflowStartContextListener.EVENTNAME_END);
    }

    @Override
    public void parseStartEvent(Element startEventElement, ScopeImpl scope, ActivityImpl startEventActivity) {
        addLogListener(startEventActivity, WorkflowStartContextListener.EVENTNAME_START);

        // Maybe get property here to move BPMN parsing from execution to this point - but in this case we would need to provide the listener as bean to allow scripted call
        startEventActivity.addListener(WorkflowStartContextListener.EVENTNAME_START, Jdp.getRequired(WorkflowStartContextListener.class));

        addLogListener(startEventActivity, WorkflowStartContextListener.EVENTNAME_END);
    }

    @Override
    public void parseEndEvent(Element endEventElement, ScopeImpl scope, ActivityImpl activity) {
        addLogListener(activity, WorkflowStartContextListener.EVENTNAME_START);

        // Maybe get property here to move BPMN parsing from execution to this point - but in this case we would need to provide the listener as bean to allow scripted call
        activity.addListener(WorkflowStartContextListener.EVENTNAME_END, Jdp.getRequired(ExecuteFinalWorkflowStepListener.class));

        addLogListener(activity, WorkflowStartContextListener.EVENTNAME_END);
    }
}
