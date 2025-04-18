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
package com.arvatosystems.t9t.bpmn2.be.camunda.delegate;

import static com.arvatosystems.t9t.bpmn2.be.camunda.utils.ExpressionUtils.getValueAsBonaPortable;
import static java.util.Objects.requireNonNull;

import jakarta.persistence.OptimisticLockException;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.event.EventParameters;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.bpmn2.be.camunda.utils.MDCHelper;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

/**
 * Java delegate to publish a T9T event.
 *
 * @author TWEL006
 */
@Singleton
public class PublishEventDelegate implements JavaDelegate {

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        try (AutoCloseable mdc = MDCHelper.put(execution)) {
            final EventParameters eventParameters = requireNonNull(getValueAsBonaPortable(execution, "eventJson", null),
              "Variable 'eventJson' must not be empty");

            executor.publishEvent(eventParameters);
        } catch (ApplicationException e) {

            if (e.getErrorCode() == T9tException.NOT_CURRENT_RECORD_OPTIMISTIC_LOCKING) {
                // Wrap into OptimisticLockException to get appropriate support by BPMN engine
                throw new OptimisticLockException(e);
            } else {
                throw e;
            }
        }
    }

}
