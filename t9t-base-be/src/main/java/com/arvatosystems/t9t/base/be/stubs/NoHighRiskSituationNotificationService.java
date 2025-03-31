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
package com.arvatosystems.t9t.base.be.stubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.services.IHighRiskSituationNotificationService;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Singleton
public class NoHighRiskSituationNotificationService implements IHighRiskSituationNotificationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoHighRiskSituationNotificationService.class);

    @Override
    public void notifyChange(final RequestContext ctx, final String changeType, final String userId, final String userName, final String emailAddress, final String newEmailAddress) {
        LOGGER.info("High risk notification {} for user ID {}", changeType, userId);
    }
}
