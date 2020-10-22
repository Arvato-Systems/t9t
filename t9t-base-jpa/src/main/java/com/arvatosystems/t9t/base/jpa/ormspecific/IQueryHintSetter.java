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
package com.arvatosystems.t9t.base.jpa.ormspecific;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;

/**
 * API to set OR mapper specific query hints or session modes.
 * If a hint is not supported by a specific OR mapper, it should just be ignored.
 */
public interface IQueryHintSetter {
    /** Set flush mode to manual, which may offer advantages over COMMIT if native SQL is used. */
    default void setManualFlushMode(EntityManager em) {}

    /** Set all queries in a session by default to read-only (true) or read/write (false). */
    default void setReadOnly(EntityManager em, Object entity, boolean readOnly) {}

    /** Set all queries in a session by default to read-only. */
    default void setReadOnlySession(EntityManager em) {}

    /** Set a query to read-only. This saves space because a second copy per entity is not required, and also no dirty-checking required. */
    default void setReadOnly(CriteriaQuery<?> q) {}

    /** Provide a query comment. This usually appears in the logs and can be used to identify the source. */
    default void setComment(CriteriaQuery<?> q, String text) {}

    /** Set a query to read-only. This saves space because a second copy per entity is not required, and also no dirty-checking required. */
    void setReadOnly(Query q);

    /** Provide a query comment. This usually appears in the logs and can be used to identify the source. */
    void setComment(Query q, String text);
}
