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

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.OperationType;

/**
 * The base class for most common request handlers. Provides common utilities such as a default "read-only" response to the isReadOnly method, and the
 * entityManagerProvider.
 *
 * @param <REQUEST>
 *            The type of request the request handler is in charge of
 */
public abstract class AbstractRequestHandler<REQUEST extends RequestParameters> implements IRequestHandler<REQUEST> {

    @Override
    public boolean isReadOnly(final REQUEST params) {
        return false;
    }

    /** Return true if the request parameters should not be stored (due to PW components or such). */
    @Override
    public boolean forbidRequestParameterStoring(final REQUEST request) {
        return false;
    }

    /** Return true if the request result should not be stored (due to sensitive data or such). */
    @Override
    public boolean forbidResultStoring(final REQUEST request) {
        return false;
    }

    @Override
    public OperationType getAdditionalRequiredPermission(final REQUEST request) {
        return OperationType.EXECUTE;
    }

    protected final ServiceResponse error(final int code, final String details) {
        return MessagingUtil.createServiceResponse(code, details);
    }

    protected final ServiceResponse ok(final int code) {
        return MessagingUtil.createOk(code);
    }

    protected final ServiceResponse ok() {
        return MessagingUtil.createOk(0);
    }

    /**
     * Method performs class validation making sure that the given request instance matches the expected request class.
     *
     * @param <REQUEST>
     *            The type of request the request handler is in charge of
     * @param requestParameters
     *            The parameters given by the incoming request
     * @param requestClass
     *            The request class to expected by the request handler
     * @return The request instance casted to the given request class
     * @throws T9tException
     *             if the incoming request instance is not matching the expected request class
     */
    @SuppressWarnings("unchecked")
    protected static <REQUEST> REQUEST staticCheckedCast(final BonaPortable requestParameters, final Class<? extends REQUEST> requestClass) {
        checkType(requestParameters, (Class<? extends BonaPortable>) requestClass);

        // Cast and return the inner request instance
        return requestClass.cast(requestParameters);
    }

    /**
     * Checks whether or not the given object instance is of given class.
     *
     * @param actualInstance
     *            The instance to be checked
     * @param expectedClass
     *            The expected class the instance shall belong to
     * @throws T9tException
     *             if the given instance is not of given class
     */
    protected static void checkType(final BonaPortable actualInstance, final Class<? extends BonaPortable> expectedClass) {
        if (!(expectedClass.isInstance(actualInstance))) {
            throw new T9tException(T9tException.INVALID_REQUEST_PARAMETER_TYPE, "expected a " + expectedClass.getSimpleName() + " type, got "
                    + actualInstance.ret$PQON());
        }
    }

    /**
     * Returns completely modifiable request parameters. Modeled as a method to avoid duplicate deep copies as long as a deep copy is already
     * done by <code>RequestProcessor</code>.
     */
    protected REQUEST getMutableRequestParameters(final REQUEST rp, final boolean deepCopy, final boolean includeCollections) {
        return rp;  // rp.ret$MutableClone(deepCopy, includeCollections)
    }
}
