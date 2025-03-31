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

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.jpa.HibernateHints;

import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;

import de.jpaw.dp.Singleton;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

@Singleton
public class QueryHintSetter implements IQueryHintSetter {

    @Override
    public void setManualFlushMode(final EntityManager em) {
        final Session session = em.unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
    }

    @Override
    public void setReadOnly(final EntityManager em, final Object entity, final boolean readOnly) {
        final Session session = em.unwrap(Session.class);
        session.setReadOnly(entity, readOnly);
    }

    @Override
    public void setReadOnlySession(final EntityManager em) {
        final Session session = em.unwrap(Session.class);
        session.setDefaultReadOnly(true);
    }

    @Override
    public void setReadOnly(final Query query) {
        query.setHint(HibernateHints.HINT_READ_ONLY, true);
    }

    @Override
    public void setComment(final Query query, final String text) {
        query.setHint(HibernateHints.HINT_COMMENT, text);
    }
}
