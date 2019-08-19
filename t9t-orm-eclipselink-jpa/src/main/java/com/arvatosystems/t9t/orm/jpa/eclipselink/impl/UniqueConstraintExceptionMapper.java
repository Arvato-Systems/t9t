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
package com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

import java.sql.SQLIntegrityConstraintViolationException;

import javax.persistence.PersistenceException;
import javax.persistence.RollbackException;

import org.eclipse.persistence.exceptions.DatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.jpa.ormspecific.IJpaCrudTechnicalExceptionMapper;

import de.jpaw.dp.Singleton;


/**
 * Maps broken unique constraint technical exception to com.arvatosystems.fortytwo.base.exceptions.T9tException.UNIQUE_CONSTRAINT_VIOLATION.
 *
 * @author dzie003
 *
 */
@Singleton
public class UniqueConstraintExceptionMapper implements IJpaCrudTechnicalExceptionMapper {
    private static final Logger LOGGER = LoggerFactory.getLogger(UniqueConstraintExceptionMapper.class);

    @Override
    public boolean handles(PersistenceException e) {
        boolean isRollbackException = (e instanceof RollbackException);
        boolean isDatabaseException = false;
        boolean isConstraintViolationException = false;

        // hack for postgres:
        if (e.getCause() != null && "DatabaseException".equals(e.getCause().getClass().getSimpleName())) {
            Throwable cause = e.getCause().getCause();
            if (cause != null && "PSQLException".equals(cause.getClass().getSimpleName())) {
                // exactly the same message as below is logged by Eclipselink directly already anyway!
//                LOGGER.warn("PSQL exception caught: {}", e.getCause().getMessage());
                if (cause.getMessage().startsWith("ERROR: duplicate key value violates "))
                    return true;
            }
        }

        if (isRollbackException && (e.getCause() != null)) {
            isDatabaseException = (e.getCause() instanceof DatabaseException);

            if (isDatabaseException && (e.getCause().getCause() != null)) {
                isConstraintViolationException = (e.getCause().getCause() instanceof SQLIntegrityConstraintViolationException);
            }
        }

        return isConstraintViolationException;
    }

    @Override
    public T9tException mapException(PersistenceException e) {
        return new T9tException(T9tException.UNIQUE_CONSTRAINT_VIOLATION);
    }

}
