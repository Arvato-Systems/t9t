/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.out.be.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.ISplittingOutputSessionProvider;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;

@Singleton
public class SplittingOutputSessionProvider implements ISplittingOutputSessionProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(SplittingOutputSessionProvider.class);
    protected final Provider<IOutputSession> outputSessionProvider = Jdp.getProvider(IOutputSession.class);

    @Override
    public IOutputSession get(final Integer maxRecords) {
        LOGGER.debug("starting OutputSession with maxRecords = {}", maxRecords == null ? "NULL" : maxRecords);
        if (maxRecords == null || maxRecords.intValue() == 0) {
            return outputSessionProvider.get(); // regular session
        }
        return new SplittingOutputSession(maxRecords);
    }
}
