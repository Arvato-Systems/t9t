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
package com.arvatosystems.t9t.orm.jpa.eclipselink.impl;

import jakarta.persistence.Query;

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;

import com.arvatosystems.t9t.base.jpa.ormspecific.IQueryHintSetter;

import de.jpaw.dp.Singleton;

@Singleton
public class QueryHintSetter implements IQueryHintSetter {

    @Override
    public void setReadOnly(final Query query) {
        query.setHint(QueryHints.READ_ONLY, HintValues.TRUE);
    }

    @Override
    public void setComment(final Query query, final String text) {
    }
}
