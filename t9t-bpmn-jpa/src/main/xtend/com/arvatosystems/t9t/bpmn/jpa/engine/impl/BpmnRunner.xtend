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
package com.arvatosystems.t9t.bpmn.jpa.engine.impl

import com.arvatosystems.t9t.base.MessagingUtil
import com.arvatosystems.t9t.base.T9tConstants
import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.bpmn.IBPMObjectFactory
import com.arvatosystems.t9t.bpmn.IWorkflowStep
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowCondition
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep
import com.arvatosystems.t9t.bpmn.T9tBPMException
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionAnd
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionNot
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionOr
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableEquals
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsIn
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsNull
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsTrue
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableStartsOrEndsWith
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepCondition
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepGoto
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepRestart
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepYield
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessExecStatusEntityResolver
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepRequest
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepResponse
import com.arvatosystems.t9t.bpmn.services.IBpmnEngineRunner
import com.arvatosystems.t9t.bpmn.services.IBpmnRunner
import com.arvatosystems.t9t.bpmn.services.IProcessDefinitionCache
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepCache
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Jdp
import de.jpaw.dp.Singleton
import de.jpaw.util.ExceptionUtil
import java.util.HashMap
import java.util.Map
import java.util.Objects
import java.util.concurrent.atomic.AtomicInteger
import org.joda.time.Instant
import org.slf4j.MDC

@Singleton
@AddLogger
class BpmnRunner implements IBpmnRunner {
    @Inject IProcessExecStatusEntityResolver statusResolver
    @Inject IWorkflowStepCache workflowStepCache
    @Inject IProcessDefinitionCache pdCache
    val dbgCtr = new AtomicInteger(886688000)

    def protected getFactory(ProcessDefinitionDTO pd) {
        if (pd.factoryName === null)
            return UnspecifiedFactory.FACTORY
        return workflowStepCache.getBPMObjectFactoryForName(pd.factoryName) as IBPMObjectFactory<Object>
    }

    override run(RequestContext ctx, Long statusRef) {
        //////////////////////////////////////////////////
        // prepare...
        //////////////////////////////////////////////////
        // 1.) get status entity
        val statusEntity = statusResolver.find(statusRef) //, LockModeType.PESSIMISTIC_READ)
        if (statusEntity === null) {
            LOGGER.info("Process status entry {} has disappeared... which implies end of the workflow process")
            return false
        }

        val id = '''serial «dbgCtr.incrementAndGet»: «statusEntity.objectRef» of «statusEntity.processDefinitionId»:«statusEntity.targetObjectRef» step «statusEntity.nextStep»'''
        LOGGER.debug("XYZZY START {}", id)
        ctx.addPostCommitHook([ LOGGER.debug("XYZZY DONE {}", id) ])
        ctx.addPostFailureHook([ LOGGER.error("XYZZY FAILED {}", id) ])

        // 2.) get process configuration
        val pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, statusEntity.processDefinitionId)
        ctx.statusText = ctx.tenantId + ":" + pd.processDefinitionId + "(" + statusRef.toString + ")"

        // 3.) obtain a factory to initialize the object (or use a dummy)
        val factory = pd.factory

        // decide if the execution must set a lock
        val refToLock = if (pd.useExclusiveLock) factory.getRefForLock(statusEntity.targetObjectRef);
        if (refToLock !== null) {
            ctx.lockRef(refToLock, pd.jvmLockTimeoutInMillis ?: T9tConstants.DEFAULT_JVM_LOCK_TIMEOUT)
        }
        if (pd.engine !== null) {
            // use some BPMN 2 engine
            val engineRunner = Jdp.getRequired(IBpmnEngineRunner, pd.engine)
            return engineRunner.run(ctx, statusRef, pd, factory, refToLock, refToLock !== null)
        }
        // execute with the default engine
        return run(ctx, statusEntity, pd, factory, refToLock, refToLock !== null)
    }

    override PerformSingleStepResponse singleStep(RequestContext ctx, PerformSingleStepRequest rq) {
        // get process configuration
        val pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, rq.processDefinitionId)

        // obtain a factory to initialize the object (or use a dummy)
        val factory = pd.factory

        // decide if the execution must set a lock
        val refToLock = if (pd.useExclusiveLock) factory.getRefForLock(rq.targetObjectRef);
        if (refToLock !== null) {
            ctx.lockRef(refToLock, pd.jvmLockTimeoutInMillis ?: T9tConstants.DEFAULT_JVM_LOCK_TIMEOUT)
        }
        // execute with the default engine. Fake a status entity
        val workflowObject = factory.read(rq.targetObjectRef, refToLock, refToLock !== null)
        val statusEntity = statusResolver.newEntityInstance
        val resp = new PerformSingleStepResponse
        val parameters = if (rq.parameters !== null) new HashMap(rq.parameters) else new HashMap
        resp.workflowReturnCode = rq.workflowStep.execute(ctx, pd, statusEntity, workflowObject, parameters)
        resp.parameters = parameters
        return resp
    }

    def protected boolean run(RequestContext ctx, ProcessExecStatusEntity statusEntity, ProcessDefinitionDTO pd, IBPMObjectFactory<?> factory, Long lockObjectRef, boolean jvmLockAcquired) {
        MDC.put(T9tConstants.MDC_BPMN_PROCESS, pd.name?:Objects.toString(pd.objectRef));
        MDC.put(T9tConstants.MDC_BPMN_PROCESS_INSTANCE, Objects.toString(statusEntity.objectRef));

        try {
            val workflowObject = factory.read(statusEntity.targetObjectRef, lockObjectRef, jvmLockAcquired)
            // only now the lock has been obtained
            statusResolver.entityManager.refresh(statusEntity)  // inside the lock, read again before we make any changes
            val parameters = statusEntity.currentParameters ?: new HashMap<String, Object>()  // do not work with the entity data, every getter / setter will convert!
            statusEntity.yieldUntil = ctx.executionStart;  // default entry for next execution

            //////////////////////////////////////////////////
            // find where to (re)start...
            //////////////////////////////////////////////////
            var int nextStepToExecute = pd.findStep(statusEntity.nextStep)
            LOGGER.debug("(Re)starting workflow {}: {} for ref {} at step {} ({})",
                ctx.tenantId, pd.processDefinitionId, statusEntity.targetObjectRef,
                nextStepToExecute, statusEntity.nextStep ?: ""
            )

            statusEntity.returnCode   = null    // reset issue marker
            statusEntity.errorDetails = null

            while (true) {
                // execute a step (or skip it)
                val nextStep = pd.workflow.steps.get(nextStepToExecute)
                MDC.put(T9tConstants.MDC_BPMN_STEP, nextStep.label);

                try {
                    LOGGER.debug("Starting workflow step {}: {} for ref {} at step {} ({})",
                        ctx.tenantId, pd.processDefinitionId, statusEntity.targetObjectRef,
                        nextStepToExecute, statusEntity.nextStep ?: ""
                    )
                    val WorkflowReturnCode wfReturnCode = nextStep.execute(ctx, pd, statusEntity, workflowObject, parameters)
                    LOGGER.debug("{}.{} ({}) returned {} on object {}", //, parameters are {}",
                        pd.processDefinitionId, nextStep.label, nextStep.ret$PQON, wfReturnCode, statusEntity.targetObjectRef
                    )

                    // evaluate workflow return code
                    if (wfReturnCode === WorkflowReturnCode.COMMIT_RESTART || wfReturnCode === WorkflowReturnCode.PROCEED_NEXT || wfReturnCode === WorkflowReturnCode.YIELD_NEXT) {
                        nextStepToExecute += 1
                        if (nextStepToExecute >= pd.workflow.steps.size) {
                            // implicit end
                            // remove the status entity
                            LOGGER.info("Workflow {} COMPLETED by running past end for ref {} (original return code was {})", pd.processDefinitionId, statusEntity.targetObjectRef, wfReturnCode)
                            statusResolver.entityManager.remove(statusEntity)
                            return false
                        }
                        statusEntity.nextStep = pd.workflow.steps.get(nextStepToExecute).label
                    }
                    switch (wfReturnCode) {
                        case GOTO: {
                            statusEntity.currentParameters = if (parameters.empty) null else parameters
                            return false
                        }
                        case DONE: {
                            // remove the status entity
                            LOGGER.info("Workflow {} COMPLETED with DONE for ref {}", pd.processDefinitionId, statusEntity.targetObjectRef)
                            statusResolver.entityManager.remove(statusEntity)
                            return false
                        }
                        case YIELD: {
                            // write back parameters and return, next time we restart the same step!
                            statusEntity.currentParameters = if (parameters.empty) null else parameters
                            return false
                        }
                        case COMMIT_RESTART: {
                            // common code executed before...
                            return true
                        }
                        case PROCEED_NEXT: {
                            // common code executed before...
                            // fall through (keep running)
                        }
                        case YIELD_NEXT: {
                            // common code executed before...
                            // write back parameters and return, next time we restart the next step!
                            statusEntity.currentParameters = if (parameters.empty) null else parameters
                            return false
                        }
                        case ERROR: {
                            // nothing to do?
                        }
                    }
                } catch (Exception e) {
                    // the JPA transaction is probably broken, so converting this into an Error return won't help much, but at least we can log the error
                    LOGGER.error("Unexpected exception in workflow step {}: {}", nextStep.ret$PQON, ExceptionUtil.causeChain(e));
                    if (e instanceof NullPointerException)
                        LOGGER.error("NPE Stack trace is ", e)
                    throw e;
                } finally {
                    MDC.remove(T9tConstants.MDC_BPMN_STEP);
                }
            }
        } finally {
            MDC.remove(T9tConstants.MDC_BPMN_PROCESS);
            MDC.remove(T9tConstants.MDC_BPMN_PROCESS_INSTANCE);
        }
    }

    def int findStep(ProcessDefinitionDTO pd, String label) {
        if (label === null || pd.alwaysRestartAtStep1)
            return 0
        val steps = pd.workflow.steps
        for (var int i = 0; i < steps.size; i += 1)
            if (steps.get(i).label == label)
                return i
        LOGGER.error("Invalid label name {} referenced for workflow {}", label, pd.processDefinitionId)
        throw new T9tException(T9tBPMException.BPM_LABEL_NOT_FOUND, pd.processDefinitionId + ": " + label)
    }

    def protected WorkflowReturnCode dealWithError(ProcessExecStatusEntity statusEntity, Map<String, Object> parameters) {
        val retCode = parameters.get("returnCode")
        statusEntity.returnCode = if (retCode === null || !(retCode instanceof Integer))
            T9tBPMException.BPM_NO_ERROR
        else
            retCode as Integer;
        statusEntity.errorDetails = MessagingUtil.truncField(parameters.get("errorDetails"), ProcessExecutionStatusDTO.meta$$errorDetails.length)
        parameters.remove("returnCode")
        parameters.remove("errorDetails")
        return WorkflowReturnCode.YIELD
    }

    def protected WorkflowReturnCode dealWithDelay(ProcessExecStatusEntity statusEntity, Map<String, Object> parameters, WorkflowReturnCode code) {
        val tilWhen = parameters.get("yieldUntil")
        if (tilWhen !== null && tilWhen instanceof Instant) {
            statusEntity.yieldUntil = tilWhen as Instant
        }
        if (tilWhen !== null && Number.isAssignableFrom(tilWhen.class)) {
            // an Instant which has been serialized as JSON and later deserialized will appear as a numeric value, representing the number of seconds since the Epoch
            statusEntity.yieldUntil = new Instant((tilWhen as Number).longValue)
        }
        return code;
    }


    //
    // WORKFLOW STEP TYPE EXECUTIONS
    //

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepJavaTask step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        val javaWfStep = workflowStepCache.getWorkflowStepForName(step.stepName) as IWorkflowStep<Object>
        val runnable = javaWfStep.mayRun(workflowObject, parameters)
        if (runnable === null) {
            // coding issue
            LOGGER.error("step {}.{}.mayRun returned code null", pd.processDefinitionId, step.stepName)
            statusEntity.returnCode = T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL
            statusEntity.errorDetails = step.stepName
            return WorkflowReturnCode.YIELD
        }
        LOGGER.trace("Executing java task {} in workflow {} (mayRun returned {})", step.stepName, pd.processDefinitionId, runnable)
        switch (runnable) {
            case RUN: {
                val execCode = javaWfStep.execute(workflowObject, parameters)
                if (execCode === null) {
                    // coding issue
                    LOGGER.error("step {}.{}.execute returned code null", pd.processDefinitionId, step.stepName)
                    statusEntity.returnCode = T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL
                    statusEntity.errorDetails = step.stepName
                    return WorkflowReturnCode.YIELD
                }
                switch (execCode) {
                case ERROR:
                    return dealWithError(statusEntity, parameters)
                case YIELD:
                    return dealWithDelay(statusEntity, parameters, execCode)
                case YIELD_NEXT:
                    return dealWithDelay(statusEntity, parameters, execCode)
                default:
                    return execCode
                }
            }
            case SKIP: {
                return WorkflowReturnCode.PROCEED_NEXT
            }
            case ERROR: {
                return dealWithError(statusEntity, parameters)
            }
            case YIELD: {
                return WorkflowReturnCode.YIELD
            }
        }

        return WorkflowReturnCode.PROCEED_NEXT
    }

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepCondition step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        // need the factory for variable name lookup
        val result = evaluateCondition(step.condition, pd.factory, workflowObject, parameters)
        val stepsToPerform = if (result) step.thenDo else step.elseDo
        var WorkflowReturnCode returnCode = WorkflowReturnCode.PROCEED_NEXT
        var boolean gotCommit = false
        if (stepsToPerform !== null) {
            for (theStep: stepsToPerform) {
                val theCode = theStep.execute(ctx, pd, statusEntity, workflowObject, parameters)
                switch (theCode) {
                    case YIELD:
                        return theCode
                    case YIELD_NEXT:
                        return theCode
                    case COMMIT_RESTART:
                        gotCommit = true
                    case ERROR:
                        return dealWithError(statusEntity, parameters)
                    case DONE:
                        return theCode
                    default:
                        {}
                }
            }
        }
        if (gotCommit && returnCode == WorkflowReturnCode.PROCEED_NEXT)
            return WorkflowReturnCode.COMMIT_RESTART  // override with a commit
        return returnCode
    }

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepAddParameters step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        for (e : step.parameters.entrySet)
            parameters.put(e.key, e.value)
        return WorkflowReturnCode.PROCEED_NEXT
    }

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepRestart step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        statusEntity.nextStep = null
        statusEntity.currentParameters = if (parameters.empty) null else parameters
        return WorkflowReturnCode.GOTO
    }

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepGoto step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
       pd.findStep(step.toLabel)  // throws exception if invalid
        statusEntity.nextStep = step.toLabel
        statusEntity.currentParameters = if (parameters.empty) null else parameters
        return WorkflowReturnCode.GOTO
    }

    def dispatch WorkflowReturnCode execute(T9tWorkflowStepYield step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        statusEntity.yieldUntil = ctx.executionStart.plus(1000L * step.waitSeconds)
        return WorkflowReturnCode.YIELD_NEXT   // must be YIELD_NEXT, not YIELD, because YIELD would result in an endless loop.
    }

    def dispatch WorkflowReturnCode execute(T9tAbstractWorkflowStep step,
        RequestContext ctx, ProcessDefinitionDTO pd, ProcessExecStatusEntity statusEntity,
        Object workflowObject, Map<String, Object> parameters
    ) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Workflow step type " + step.class.canonicalName)
    }


    //
    // WORKFLOW CONDITIONS
    //

    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionAnd it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        for (cond: conditions) {
            if (!evaluateCondition(cond, factory, workflowObject, parameters))
                return false
        }
        return true
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionOr it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        for (cond: conditions) {
            if (evaluateCondition(cond, factory, workflowObject, parameters))
                return true
        }
        return false
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionNot condition, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        return !evaluateCondition(condition.condition, factory, workflowObject, parameters)
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionVariableIsNull it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        val variable = if (fromMap) parameters.get(variableName) else factory.getVariable(variableName, workflowObject)
        return variable === null
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionVariableIsTrue it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        val variable = if (fromMap) parameters.get(variableName) else factory.getVariable(variableName, workflowObject)
        return Boolean.TRUE == variable
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionVariableEquals it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        val variable = if (fromMap) parameters.get(variableName) else factory.getVariable(variableName, workflowObject)
        return value == variable
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionVariableStartsOrEndsWith it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        val variable = if (fromMap) parameters.get(variableName) else factory.getVariable(variableName, workflowObject)
        if (variable === null)
            return false
        if (ends)
            return variable.toString.endsWith(pattern)
        else
            return variable.toString.startsWith(pattern)
    }
    def dispatch protected boolean evaluateCondition(T9tWorkflowConditionVariableIsIn it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        val variable = if (fromMap) parameters.get(variableName) else factory.getVariable(variableName, workflowObject)
        return variable !== null && values.contains(variable)
    }
    def dispatch protected boolean evaluateCondition(T9tAbstractWorkflowCondition it, IBPMObjectFactory<Object> factory, Object workflowObject, Map<String, Object> parameters) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Workflow condition type " + class.canonicalName)
    }
}
