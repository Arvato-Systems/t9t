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
package com.arvatosystems.t9t.orm.jpa.hibernate.impl;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.jpa.QueryHints;

import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;

import de.jpaw.dp.Singleton;

@Singleton
public class QueryHintSetter implements IQueryHintSetter {

    @Override
    public
    void setManualFlushMode(EntityManager em) {
        Session session = em.unwrap(Session.class);
        session.setHibernateFlushMode(FlushMode.MANUAL);
    }

    @Override
    public void setReadOnly(EntityManager em, Object entity, boolean readOnly) {
        Session session = em.unwrap(Session.class);
        session.setReadOnly(entity, readOnly);
    }

    @Override
    public void setReadOnlySession(EntityManager em) {
        Session session = em.unwrap(Session.class);
        session.setDefaultReadOnly(true);
    }

    @Override
    public void setReadOnly(Query query) {
        query.setHint(QueryHints.HINT_READONLY, true);
    }

    @Override
    public void setComment(Query query, String text) {
        query.setHint(QueryHints.HINT_COMMENT, text);
    }
}
