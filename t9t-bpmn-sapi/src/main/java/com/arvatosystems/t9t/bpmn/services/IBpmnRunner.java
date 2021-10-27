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
package com.arvatosystems.t9t.bpmn.services;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepRequest;
import com.arvatosystems.t9t.bpmn.request.PerformSingleStepResponse;

public interface IBpmnRunner {
    /** Perform workflow steps. Returns true if the workflow should rerun immediately (intermediate commit), false in any other case. */
    boolean run(RequestContext ctx, Long statusRef);

    /** For debugging. */
    default PerformSingleStepResponse singleStep(final RequestContext ctx, final PerformSingleStepRequest rq) {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, "Not available for this engine");
    }
}
