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
package com.arvatosystems.t9t.base.jpa.ormspecific;

import javax.persistence.PersistenceException;

import com.arvatosystems.t9t.base.T9tException;

/**
 * Defines interface for mappers mapping technical exceptions which can occur during crud operation on entity into business exceptions defined in
 * com.arvatosystems.t9t.core.T9tException class.
 *
 * @author dzie003
 *
 */
public interface IJpaCrudTechnicalExceptionMapper {

    /**
     * Checks if mapper is able to handle given technical exception.
     *
     * @param technicalException
     *            technical exception thrown during entity crud operation
     * @return <code>true</code> if mapper can map given exception into business exception, <code>false</code> otherwise.
     */
    public boolean handles(PersistenceException technicalException);

    /**
     * Maps given technical exception into business exception.
     *
     * @param technicalException
     *            technical exception thrown during entity crud operation
     * @return corresponding business exception
     */
    public T9tException mapException(PersistenceException technicalException);

}
