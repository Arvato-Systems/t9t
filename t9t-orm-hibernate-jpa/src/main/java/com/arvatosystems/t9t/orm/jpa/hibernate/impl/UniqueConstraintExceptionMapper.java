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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import jakarta.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaCrudTechnicalExceptionMapper;

import de.jpaw.dp.Singleton;


/**
 * Maps broken unique constraint technical exception to <code>T9tException.UNIQUE_CONSTRAINT_VIOLATION</code>.
 */
@Singleton
public class UniqueConstraintExceptionMapper implements IJpaCrudTechnicalExceptionMapper {

    @Override
    public boolean handles(final PersistenceException e) {
        final Throwable cause = e.getCause();
        return e instanceof ConstraintViolationException
            || e instanceof jakarta.persistence.EntityExistsException
            || (cause != null && (cause instanceof ConstraintViolationException || cause instanceof jakarta.persistence.EntityExistsException));
    }

    @Override
    public T9tException mapException(final PersistenceException e) {
        return new T9tException(T9tException.UNIQUE_CONSTRAINT_VIOLATION);
    }
}
