/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.jpa.impl;

import com.arvatosystems.t9t.base.search.SearchCriteria;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;

public abstract class AbstractMonitoringSearchRequestHandler<REQUEST extends SearchCriteria> extends AbstractSearchRequestHandler<REQUEST> {

    protected final ApplicationConfiguration applConfig = ConfigProvider.getConfiguration().getApplicationConfiguration();
    protected final boolean useShadowDatabase = applConfig != null && Boolean.TRUE.equals(applConfig.getUseShadowDatabaseForMonitoringQueries());

    /**
     * Returns the hint to use the shadow database (if present).
     * By default, all combined searches use the shadow database unless they have to export data (which needs write access).
     * Therefore the methods delegates to the test for a data export as used by the generic search request handler.
     */
    @Override
    public boolean useShadowDatabase(final REQUEST rq) {
        return useShadowDatabase && isReadOnly(rq);
    }
}
