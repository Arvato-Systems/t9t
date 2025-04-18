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
package com.arvatosystems.t9t.base.be.stubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoRemoteDefaultUrlRetriever implements IRemoteDefaultUrlRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoRemoteDefaultUrlRetriever.class);

    @Override
    public String getDefaultRemoteUrl() {
        LOGGER.error("No implementation for a default remote URL available (IRemoteDefaultUrlRetriever)");
        throw new T9tException(T9tException.ILE_MISSING_DEPENDENCY, "No implementation for IRemoteDefaultUrlRetriever available");
    }
}
