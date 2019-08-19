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
package com.arvatosystems.t9t.auth.jpa.impl

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO
import com.arvatosystems.t9t.auth.jpa.entities.AuthModuleCfgEntity
import com.arvatosystems.t9t.auth.jpa.persistence.IAuthModuleCfgEntityResolver
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver
import com.arvatosystems.t9t.core.jpa.impl.AbstractModuleConfigResolver
import de.jpaw.dp.Singleton

@Singleton
class AuthModuleCfgDtoResolver extends AbstractModuleConfigResolver<AuthModuleCfgDTO, AuthModuleCfgEntity> implements IAuthModuleCfgDtoResolver {
    static final AuthModuleCfgDTO DEFAULT_MODULE_CFG = new AuthModuleCfgDTO(
        null,       // Json z
        720,        // maxTokenValidityInMinutes:  how many minutes can a JWT token last max.? (default 12 hrs)
        10,         // passwordMinimumLength:      minimum length for a password in characters
        3,          // passwordDifferPreviousN:    from how many previous passwords must this one be different?
        60,         // passwordExpirationInDays:   after how many days does a password expired? (0 to disable expiry, 60 is default)
        86400,      // passwordResetDurationInSec: how long is a reset password valid? (default 1 day)
        3,          // passwordThrottlingAfterX:   after how many incorrect attempts is the access throttled? (default 3)
        60,         // passwordThrottlingDuration: for how many seconds does the account not accept any login attempt after throttling?
        0           // passwordBlockingPeriod:     Period in days saying how long an old password will be blocked before it can be used again. "0" Disables this feature
    );

    new() {
        super(IAuthModuleCfgEntityResolver)
    }

    override AuthModuleCfgDTO getDefaultModuleConfiguration() {
        return DEFAULT_MODULE_CFG;
    }
}
