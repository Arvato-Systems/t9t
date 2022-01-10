/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.IBPMObjectFactory;
import com.arvatosystems.t9t.bpmn.IWorkflowStep;
import com.arvatosystems.t9t.bpmn.ProcessDefinitionDTO;
import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowCondition;
import com.arvatosystems.t9t.bpmn.T9tAbstractWorkflowStep;
import com.arvatosystems.t9t.bpmn.T9tBPMException;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionAnd;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionNot;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionOr;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableEquals;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsIn;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsNull;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableIsTrue;
import com.arvatosystems.t9t.bpmn.T9tWorkflowConditionVariableStartsOrEndsWith;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepAddParameters;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepCondition;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepGoto;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepJavaTask;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepRestart;
import com.arvatosystems.t9t.bpmn.T9tWorkflowStepYield;
import com.arvatosystems.t9t.bpmn.WorkflowReturnCode;
import com.arvatosystems.t9t.bpmn.WorkflowRunnableCode;
import com.arvatosystems.t9t.bpmn.jpa.entities.ProcessExecStatusEntity;
import com.arvatosystems.t9t.bpmn.jpa.persistence.IProcessExecStatusEntityResolver;
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepRequest;
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepResponse;
import com.arvatosystems.t9t.bpmn.services.IBpmnEngineRunner;
import com.arvatosystems.t9t.bpmn.services.IBpmnRunner;
import com.arvatosystems.t9t.bpmn.services.IProcessDefinitionCache;
import com.arvatosystems.t9t.bpmn.services.IWorkflowStepCache;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

@Singleton
public class BpmnRunner implements IBpmnRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(BpmnRunner.class);

    private final IProcessExecStatusEntityResolver statusResolver = Jdp.getRequired(IProcessExecStatusEntityResolver.class);
    private final IWorkflowStepCache workflowStepCache = Jdp.getRequired(IWorkflowStepCache.class);
    private final IProcessDefinitionCache pdCache = Jdp.getRequired(IProcessDefinitionCache.class);
    private final AtomicInteger dbgCtr = new AtomicInteger(886688000);

    @Override
    public boolean run(final RequestContext ctx, final Long statusRef) {
        //////////////////////////////////////////////////
        // prepare...
        //////////////////////////////////////////////////
        // 1.) get status entity
        ctx.lockRef(statusRef); // acquire & lock the statusRef

        final ProcessExecStatusEntity statusEntity = statusResolver.find(statusRef); //, LockModeType.PESSIMISTIC_READ)
        if (statusEntity == null) {
            LOGGER.info("Process status entry {} has disappeared... which implies end of the workflow process");
            return false;
        }

        final String id = "serial " + dbgCtr.incrementAndGet() + ": " + statusEntity.getObjectRef() + " of " + statusEntity.getProcessDefinitionId() + ":"
                + statusEntity.getTargetObjectRef() + " step " + statusEntity.getNextStep();
        LOGGER.debug("XYZZY START {}", id);
        ctx.addPostCommitHook((final RequestContext previousRequestContext, final RequestParameters rq, final ServiceResponse rs) -> {
            LOGGER.debug("XYZZY DONE {}", id);
        });
        ctx.addPostFailureHook((final RequestContext previousRequestContext, final RequestParameters rq, final ServiceResponse rs) -> {
            LOGGER.error("XYZZY FAILED {}", id);
        });

        // 2.) get process configuration
        final ProcessDefinitionDTO pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, statusEntity.getProcessDefinitionId());
        ctx.statusText = ctx.tenantId + ":" + pd.getProcessDefinitionId() + "(" + statusRef.toString() + ")";

        // 3.) obtain a factory to initialize the object (or use a dummy)
        final IBPMObjectFactory<Object> factory = getFactory(pd);

        // decide if the execution must set a lock
        final Long refToLock = pd.getUseExclusiveLock() ? factory.getRefForLock(statusEntity.getTargetObjectRef()) : null;
        if (refToLock != null) {
            ctx.lockRef(refToLock, pd.getJvmLockTimeoutInMillis() == null ? T9tConstants.DEFAULT_JVM_LOCK_TIMEOUT : pd.getJvmLockTimeoutInMillis());
        }
        if (pd.getEngine() != null) {
            // use some BPMN 2 engine
            final IBpmnEngineRunner engineRunner = Jdp.getRequired(IBpmnEngineRunner.class, pd.getEngine());
            return engineRunner.run(ctx, statusRef, pd, factory, refToLock, refToLock != null);
        }
        return run(ctx, statusEntity, pd, factory, refToLock, refToLock != null);
    }

    @Override
    public PerformSingleStepResponse singleStep(final RequestContext ctx, final PerformSingleStepRequest rq) {
        // get process configuration
        final ProcessDefinitionDTO pd = pdCache.getCachedProcessDefinitionDTO(ctx.tenantId, rq.getProcessDefinitionId());

        // obtain a factory to initialize the object (or use a dummy)
        final IBPMObjectFactory<Object> factory = getFactory(pd);

        // decide if the execution must set a lock
        final Long refToLock = pd.getUseExclusiveLock() ? factory.getRefForLock(rq.getTargetObjectRef()) : null;
        if (refToLock != null) {
            ctx.lockRef(refToLock, pd.getJvmLockTimeoutInMillis() == null ? T9tConstants.DEFAULT_JVM_LOCK_TIMEOUT : pd.getJvmLockTimeoutInMillis());
        }
        // execute with the default engine. Fake a status entity
        final Object workflowObject = factory.read(rq.getTargetObjectRef(), refToLock, refToLock != null);
        final ProcessExecStatusEntity statusEntity = statusResolver.newEntityInstance();
        final PerformSingleStepResponse resp = new PerformSingleStepResponse();
        final Map<String, Object> parameters = rq.getParameters() != null ? new HashMap<>(rq.getParameters()) : new HashMap<>();
        resp.setWorkflowReturnCode(execute(rq.getWorkflowStep(), ctx, pd, statusEntity, workflowObject, parameters));
        resp.setParameters(parameters);
        return resp;
    }

    @SuppressWarnings("unchecked")
    protected IBPMObjectFactory<Object> getFactory(final ProcessDefinitionDTO pd) {
        final String factoryName = pd.getFactoryName();
        if (factoryName == null) {
            return UnspecifiedFactory.FACTORY;
        }
        return (IBPMObjectFactory<Object>) workflowStepCache.getBPMObjectFactoryForName(factoryName);
    }

    protected boolean run(final RequestContext ctx, final ProcessExecStatusEntity statusEntity, final ProcessDefinitionDTO pd,
            final IBPMObjectFactory<?> factory, final Long lockObjectRef, final boolean jvmLockAcquired) {
        MDC.put(T9tConstants.MDC_BPMN_PROCESS, pd.getName() == null ? Objects.toString(pd.getObjectRef()) : pd.getName());
        MDC.put(T9tConstants.MDC_BPMN_PROCESS_INSTANCE, Objects.toString(statusEntity.getObjectRef()));

        try {
            final Object workflowObject = factory.read(statusEntity.getTargetObjectRef(), lockObjectRef, jvmLockAcquired);
            // only now the lock has been obtained
            if (refresh(statusEntity) == null) {
                LOGGER.info("Process status entry {} has disappeared... which implies end of the workflow process");
                return false;
            } // inside the lock, read again before we make any changes

            // do not work with the entity data, every getter / setter will convert!
            final Map<String, Object> parameters = statusEntity.getCurrentParameters() == null ? new HashMap<>() : statusEntity.getCurrentParameters();
            statusEntity.setYieldUntil(ctx.executionStart); // default entry for next execution

            //////////////////////////////////////////////////
            // find where to (re)start...
            //////////////////////////////////////////////////
            int nextStepToExecute = findStep(pd, statusEntity.getNextStep());
            LOGGER.debug("(Re)starting workflow {}: {} for ref {} at step {} ({})", ctx.tenantId, pd.getProcessDefinitionId(),
                    statusEntity.getTargetObjectRef(), nextStepToExecute, statusEntity.getNextStep() == null ? "" : statusEntity.getNextStep());

            statusEntity.setReturnCode(null); // reset issue marker
            statusEntity.setErrorDetails(null);

            while (true) {
                // execute a step (or skip it)
                final T9tAbstractWorkflowStep nextStep = pd.getWorkflow().getSteps().get(nextStepToExecute);
                MDC.put(T9tConstants.MDC_BPMN_STEP, nextStep.getLabel());

                try {
                    LOGGER.debug("Starting workflow step {}: {} for ref {} at step {} ({})", ctx.tenantId, pd.getProcessDefinitionId(),
                            statusEntity.getTargetObjectRef(), nextStepToExecute, statusEntity.getNextStep() == null ? "" : statusEntity.getNextStep());
                    final WorkflowReturnCode wfReturnCode = execute(nextStep, ctx, pd, statusEntity, workflowObject, parameters);
                    LOGGER.debug("{}.{} ({}) returned {} on object {}", pd.getProcessDefinitionId(), nextStep.getLabel(), nextStep.ret$PQON(), wfReturnCode,
                            statusEntity.getTargetObjectRef());

                    // evaluate workflow return code
                    if (wfReturnCode == WorkflowReturnCode.COMMIT_RESTART || wfReturnCode == WorkflowReturnCode.PROCEED_NEXT
                            || wfReturnCode == WorkflowReturnCode.YIELD_NEXT) {
                        nextStepToExecute += 1;
                        if (nextStepToExecute >= pd.getWorkflow().getSteps().size()) {
                            // implicit end
                            // remove the status entity
                            LOGGER.info("Workflow {} COMPLETED by running past end for ref {} (original return code was {})", pd.getProcessDefinitionId(),
                                    statusEntity.getTargetObjectRef(), wfReturnCode);
                            statusResolver.getEntityManager().remove(statusEntity);
                            return false;
                        }
                        statusEntity.setNextStep(pd.getWorkflow().getSteps().get(nextStepToExecute).getLabel());
                    }
                    switch (wfReturnCode) {
                    case GOTO:
                        statusEntity.setCurrentParameters(parameters.isEmpty() ? null : parameters);
                        return false;
                    case DONE:
                        // remove the status entity
                        LOGGER.info("Workflow {} COMPLETED with DONE for ref {}", pd.getProcessDefinitionId(), statusEntity.getTargetObjectRef());
                        statusResolver.getEntityManager().remove(statusEntity);
                        return false;
                    case YIELD:
                        // write back parameters and return, next time we restart the same step!
                        statusEntity.setCurrentParameters(parameters.isEmpty() ? null : parameters);
                        return false;
                    case COMMIT_RESTART:
                        // common code executed before...
                        return true;
                    case PROCEED_NEXT:
                        // common code executed before...
                        // fall through (keep running)
                        break;
                    case YIELD_NEXT:
                        // common code executed before...
                        // write back parameters and return, next time we restart the next step!
                        statusEntity.setCurrentParameters(parameters.isEmpty() ? null : parameters);
                        return false;
                    case ERROR:
                        // nothing to do?
                        break;
                    }
                } catch (final Exception e) {
                    // the JPA transaction is probably broken, so converting this into an Error return won't help much, but at least we can log the error
                    LOGGER.error("Unexpected exception in workflow step {}: {}", nextStep.ret$PQON(), ExceptionUtil.causeChain(e));
                    if (e instanceof NullPointerException) {
                        LOGGER.error("NPE Stack trace is ", e);
                    }
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

    protected ProcessExecStatusEntity refresh(final ProcessExecStatusEntity statusEntity) {
        try {
            statusResolver.getEntityManager().refresh(statusEntity);
        } catch (final EntityNotFoundException enfe) {
            LOGGER.debug("Status probably been removed.");
            return null;
        }
        return statusEntity;
    }

    public int findStep(final ProcessDefinitionDTO pd, final String label) {
        if (label == null || pd.getAlwaysRestartAtStep1()) {
            return 0;
        }
        final List<T9tAbstractWorkflowStep> steps = pd.getWorkflow().getSteps();
        for (int i = 0; i < steps.size(); i++) {
            if (steps.get(i).getLabel().equals(label)) {
                return i;
            }
        }
        LOGGER.error("Invalid label name {} referenced for workflow {}", label, pd.getProcessDefinitionId());
        throw new T9tException(T9tBPMException.BPM_LABEL_NOT_FOUND, pd.getProcessDefinitionId() + ": " + label);
    }

    protected WorkflowReturnCode dealWithError(final ProcessExecStatusEntity statusEntity, final Map<String, Object> parameters) {
        final Object retCode = parameters.get(IWorkflowStep.PROCESS_VARIABLE_RETURN_CODE);
        if (retCode == null || !(retCode instanceof Integer)) {
            statusEntity.setReturnCode(T9tBPMException.BPM_NO_ERROR);
        } else {
            statusEntity.setReturnCode((Integer) retCode);
        }
        statusEntity.setErrorDetails(MessagingUtil.truncField(parameters.get(IWorkflowStep.PROCESS_VARIABLE_ERROR_DETAILS),
                ProcessExecutionStatusDTO.meta$$errorDetails.getLength()));
        parameters.remove(IWorkflowStep.PROCESS_VARIABLE_RETURN_CODE);
        parameters.remove(IWorkflowStep.PROCESS_VARIABLE_ERROR_DETAILS);
        return WorkflowReturnCode.YIELD;
    }

    protected WorkflowReturnCode dealWithDelay(final ProcessExecStatusEntity statusEntity, final Map<String, Object> parameters,
            final WorkflowReturnCode code) {
        final Object tilWhen = parameters.get(IWorkflowStep.PROCESS_VARIABLE_YIELD_UNTIL);
        if (tilWhen != null && tilWhen instanceof Instant) {
            statusEntity.setYieldUntil((Instant) tilWhen);
        }
        if (tilWhen != null && Number.class.isAssignableFrom(tilWhen.getClass())) {
            // an Instant which has been serialized as JSON and later deserialized will appear as a numeric value,
            // representing the number of seconds since the Epoch
            statusEntity.setYieldUntil(Instant.ofEpochMilli(((Number) tilWhen).longValue()));
        }
        return code;
    }

    //
    // WORKFLOW STEP TYPE EXECUTIONS
    //

    public WorkflowReturnCode execute(final T9tAbstractWorkflowStep step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        if (step instanceof T9tWorkflowStepAddParameters) {
            return executeAddParameters((T9tWorkflowStepAddParameters) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step instanceof T9tWorkflowStepCondition) {
            return executeCondition((T9tWorkflowStepCondition) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step instanceof T9tWorkflowStepGoto) {
            return executeGoto((T9tWorkflowStepGoto) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step instanceof T9tWorkflowStepJavaTask) {
            return executeJavaTask((T9tWorkflowStepJavaTask) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step instanceof T9tWorkflowStepRestart) {
            return executeRestart((T9tWorkflowStepRestart) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step instanceof T9tWorkflowStepYield) {
            return executeYield((T9tWorkflowStepYield) step, ctx, pd, statusEntity, workflowObject, parameters);
        } else if (step != null) {
            return executeDefault(step, ctx, pd, statusEntity, workflowObject, parameters);
        } else {
            throw new IllegalArgumentException(
                    "Unhandled parameter types: " + Arrays.<Object>asList(step, ctx, pd, statusEntity, workflowObject, parameters).toString());
        }
    }

    @SuppressWarnings("unchecked")
    protected WorkflowReturnCode executeJavaTask(final T9tWorkflowStepJavaTask step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        final IWorkflowStep<Object> javaWfStep = (IWorkflowStep<Object>) workflowStepCache.getWorkflowStepForName(step.getStepName());
        final WorkflowRunnableCode runnable = javaWfStep.mayRun(workflowObject, parameters);
        if (runnable == null) {
         // coding issue
          LOGGER.error("step {}.{}.mayRun returned code null", pd.getProcessDefinitionId(), step.getStepName());
          statusEntity.setReturnCode(T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL);
          statusEntity.setErrorDetails(step.getStepName());
          return WorkflowReturnCode.YIELD;
        }
        LOGGER.trace("Executing java task {} in workflow {} (mayRun returned {})", step.getStepName(), pd.getProcessDefinitionId(), runnable);

        switch (runnable) {
        case RUN: {
            final WorkflowReturnCode execCode = javaWfStep.execute(workflowObject, parameters);
            if (execCode == null) {
                // coding issue
                LOGGER.error("step {}.{}.execute returned code null", pd.getProcessDefinitionId(), step.getStepName());
                statusEntity.setReturnCode(T9tBPMException.BPM_EXECUTE_JAVA_TASK_RETURNED_NULL);
                statusEntity.setErrorDetails(step.getStepName());
                return WorkflowReturnCode.YIELD;
            }
            switch (execCode) {
            case ERROR:
                return dealWithError(statusEntity, parameters);
            case YIELD:
                return dealWithDelay(statusEntity, parameters, execCode);
            case YIELD_NEXT:
                return dealWithDelay(statusEntity, parameters, execCode);
            default:
                return execCode;
            }
        }
        case SKIP: {
            return WorkflowReturnCode.PROCEED_NEXT;
        }
        case ERROR: {
            return dealWithError(statusEntity, parameters);
        }
        case YIELD: {
            return WorkflowReturnCode.YIELD;
        }
        }

        return WorkflowReturnCode.PROCEED_NEXT;
    }

    protected WorkflowReturnCode executeCondition(final T9tWorkflowStepCondition step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        // need the factory for variable name lookup
        final boolean result = evaluateCondition(step.getCondition(), getFactory(pd), workflowObject, parameters);
        final List<T9tAbstractWorkflowStep> stepsToPerform = result ? step.getThenDo() : step.getElseDo();
        final WorkflowReturnCode returnCode = WorkflowReturnCode.PROCEED_NEXT;
        boolean gotCommit = false;
        if (stepsToPerform != null) {
            for (final T9tAbstractWorkflowStep theStep : stepsToPerform) {
                final WorkflowReturnCode theCode = execute(theStep, ctx, pd, statusEntity, workflowObject, parameters);
                if (theCode != null) {
                    switch (theCode) {
                    case YIELD:
                        return theCode;
                    case YIELD_NEXT:
                        return theCode;
                    case COMMIT_RESTART:
                        gotCommit = true;
                        break;
                    case ERROR:
                        return dealWithError(statusEntity, parameters);
                    case DONE:
                        return theCode;
                    default:
                        break;
                    }
                }
            }
        }
        if (gotCommit && WorkflowReturnCode.PROCEED_NEXT == returnCode) {
            return WorkflowReturnCode.COMMIT_RESTART; // override with a commit
        }
        return returnCode;
    }

    protected WorkflowReturnCode executeAddParameters(final T9tWorkflowStepAddParameters step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        for (final Map.Entry<String, Object> e : step.getParameters().entrySet()) {
            parameters.put(e.getKey(), e.getValue());
        }
        return WorkflowReturnCode.PROCEED_NEXT;
    }

    protected WorkflowReturnCode executeRestart(final T9tWorkflowStepRestart step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        statusEntity.setNextStep(null);
        statusEntity.setCurrentParameters(parameters.isEmpty() ? null : parameters);
        return WorkflowReturnCode.GOTO;
    }

    protected WorkflowReturnCode executeGoto(final T9tWorkflowStepGoto step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        findStep(pd, step.getToLabel()); // throws exception if invalid
        statusEntity.setNextStep(step.getToLabel());
        statusEntity.setCurrentParameters(parameters.isEmpty() ? null : parameters);
        return WorkflowReturnCode.GOTO;
    }

    protected WorkflowReturnCode executeYield(final T9tWorkflowStepYield step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        statusEntity.setYieldUntil(ctx.executionStart.plusSeconds(step.getWaitSeconds()));
        return WorkflowReturnCode.YIELD_NEXT; // must be YIELD_NEXT, not YIELD, because YIELD would result in an endless loop.
    }

    protected WorkflowReturnCode executeDefault(final T9tAbstractWorkflowStep step, final RequestContext ctx, final ProcessDefinitionDTO pd,
            final ProcessExecStatusEntity statusEntity, final Object workflowObject, final Map<String, Object> parameters) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Workflow step type " + step.getClass().getCanonicalName());
    }

    //
    // WORKFLOW CONDITIONS
    //

    protected boolean evaluateCondition(final T9tAbstractWorkflowCondition condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        if (condition instanceof T9tWorkflowConditionVariableEquals) {
            return evaluateEquals((T9tWorkflowConditionVariableEquals) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionVariableIsIn) {
            return evaluateIsIn((T9tWorkflowConditionVariableIsIn) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionVariableIsNull) {
            return evaluateIsNull((T9tWorkflowConditionVariableIsNull) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionVariableIsTrue) {
            return evaluateIsTrue((T9tWorkflowConditionVariableIsTrue) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionVariableStartsOrEndsWith) {
            return evaluateStartsOrEndsWith((T9tWorkflowConditionVariableStartsOrEndsWith) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionAnd) {
            return evaluateAnd((T9tWorkflowConditionAnd) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionNot) {
            return evaluateNot((T9tWorkflowConditionNot) condition, factory, workflowObject, parameters);
        } else if (condition instanceof T9tWorkflowConditionOr) {
            return evaluateOr((T9tWorkflowConditionOr) condition, factory, workflowObject, parameters);
        } else if (condition != null) {
            return evaluateDefault(condition, factory, workflowObject, parameters);
        } else {
            throw new IllegalArgumentException(
                    "Unhandled parameter types: " + Arrays.<Object>asList(condition, factory, workflowObject, parameters).toString());
        }
    }

    protected boolean evaluateAnd(final T9tWorkflowConditionAnd condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        for (final T9tAbstractWorkflowCondition cond : condition.getConditions()) {
            if (!evaluateCondition(cond, factory, workflowObject, parameters)) {
                return false;
            }
        }
        return true;
    }

    protected boolean evaluateOr(final T9tWorkflowConditionOr condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        for (final T9tAbstractWorkflowCondition cond : condition.getConditions()) {
            if (evaluateCondition(cond, factory, workflowObject, parameters)) {
                return true;
            }
        }
        return false;
    }

    protected boolean evaluateNot(final T9tWorkflowConditionNot condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        return !evaluateCondition(condition.getCondition(), factory, workflowObject, parameters);
    }

    protected boolean evaluateIsNull(final T9tWorkflowConditionVariableIsNull condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        final Object variable = condition.getFromMap() ? parameters.get(condition.getVariableName())
                : factory.getVariable(condition.getVariableName(), workflowObject);
        return variable == null;
    }

    protected boolean evaluateIsTrue(final T9tWorkflowConditionVariableIsTrue condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        final Object variable = condition.getFromMap() ? parameters.get(condition.getVariableName())
                : factory.getVariable(condition.getVariableName(), workflowObject);
        return Boolean.TRUE.equals(variable);
    }

    protected boolean evaluateEquals(final T9tWorkflowConditionVariableEquals condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        final Object variable = condition.getFromMap() ? parameters.get(condition.getVariableName())
                : factory.getVariable(condition.getVariableName(), workflowObject);
        return condition.getValue().equals(variable);
    }

    protected boolean evaluateStartsOrEndsWith(final T9tWorkflowConditionVariableStartsOrEndsWith condition, final IBPMObjectFactory<Object> factory,
            final Object workflowObject, final Map<String, Object> parameters) {
        final Object variable = condition.getFromMap() ? parameters.get(condition.getVariableName())
                : factory.getVariable(condition.getVariableName(), workflowObject);
        if (variable == null) {
            return false;
        }
        if (condition.getEnds()) {
            return variable.toString().endsWith(condition.getPattern());
        } else {
            return variable.toString().startsWith(condition.getPattern());
        }
    }

    protected boolean evaluateIsIn(final T9tWorkflowConditionVariableIsIn condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        final Object variable = condition.getFromMap() ? parameters.get(condition.getVariableName())
                : factory.getVariable(condition.getVariableName(), workflowObject);
        return variable != null && condition.getValues().contains(variable);
    }

    protected boolean evaluateDefault(final T9tAbstractWorkflowCondition condition, final IBPMObjectFactory<Object> factory, final Object workflowObject,
            final Map<String, Object> parameters) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Workflow condition type " + getClass().getCanonicalName());
    }
}
