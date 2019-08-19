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
package com.arvatosystems.t9t.base.be.stubs;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.ITextSearch;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ApplicationException;

/**
 * Stub implementation of the ITextSearch interface, just here to avoid cyclic dependencies. It is expected that a real implementation prioritizes over this
 * with the @Specializes annotation, or as a separate implementation.
 *
 */

@Fallback
@Any
@Singleton
public class NoTextSearch implements ITextSearch {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoTextSearch.class);

    public NoTextSearch() {
        LOGGER.warn("NoTextSearch execution stub selected - SOLR searches will not work");
    }

    @Override
    public List<Long> search(RequestContext ctx, SearchCriteria sc, String documentName, String resultFieldName) {
        LOGGER.error("No full text search engine has been configured");
        throw new ApplicationException(T9tException.NOT_YET_IMPLEMENTED, documentName);
    }
}
