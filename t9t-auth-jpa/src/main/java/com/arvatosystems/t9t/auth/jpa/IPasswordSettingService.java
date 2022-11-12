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
package com.arvatosystems.t9t.auth.jpa;

import java.time.Instant;
import com.arvatosystems.t9t.auth.jpa.entities.PasswordEntity;
import com.arvatosystems.t9t.auth.jpa.entities.UserEntity;
import com.arvatosystems.t9t.base.services.RequestContext;

public interface IPasswordSettingService {
    /** This method is invoked if an administor changes a password for another user. */
    void setPasswordForUser(RequestContext ctx, UserEntity user, String password);

    /** This method is invoked when user reset the password. Returns the newly created PasswordEntity */
    PasswordEntity setPasswordForUser(Instant now, UserEntity user, String password, Long passwordSetByUserRef);
}
