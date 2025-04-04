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
package com.arvatosystems.t9t.base.services;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;

import de.jpaw.bonaparte.pojos.api.OperationType;
import jakarta.annotation.Nonnull;

/**
 * The contract to be implemented by any class that shall serve as a request handler for incoming service requests.
 *
 * @param <REQUEST> The type of request the request handler is in charge of
 */
public interface IRequestHandler<REQUEST extends RequestParameters> {

    /**
     * Returns whether or not the given request is related to a read-only (repeatable) activity.
     *
     * @param request
     *            The request holding the parameters for the service call (only required in case the read-only property is a function of the parameters, such as
     *            for CRUD services).
     * @return TRUE if invoking this request will not alter the database else FALSE, i.e. if modifications are written to database or file system
     */
    boolean isReadOnly(@Nonnull REQUEST request);

    /**
     * Returns whether a separate database (which possibly has some latency) should be used for this requests read operations.
     * In theory, it is possible for requests which modify data to specify this, in this case however they must perform any write operation
     * via JDBC through the secondary database.
     *
     * @param request
     *            The request holding the parameters for the service call (only required in case the read-only property is a function of the parameters, such as
     *            for CRUD services).
     * @return TRUE if invoking this request is ok with using slightly older data.
     */
    default boolean useShadowDatabase(@Nonnull final REQUEST request) {
        return false;
    }

    /** Return true if the request parameters should not be stored (due to PW components or such). */
    boolean forbidRequestParameterStoring(@Nonnull REQUEST request);

    /** Return true if the request result should not be stored (due to sensitive data or such). */
    boolean forbidResultStoring(@Nonnull REQUEST request);

    /** Obtain any required additional permission (to execute) for the specific request. Still, further checks may be done within execute(). */
    OperationType getAdditionalRequiredPermission(@Nonnull REQUEST request);

    /**
     * Performs the overall request handling of the given request. The execution is normally transaction-agnostic, because transaction handling is performed by
     * the invoker.
     *
     * @param request
     *            The request that shall be handled
     * @return The response to be returned to the invoker.
     * @throws Exception
     *             in case of anything goes wrong
     */
    @Nonnull
    ServiceResponse execute(@Nonnull RequestContext ctx, @Nonnull REQUEST request) throws Exception;
}
