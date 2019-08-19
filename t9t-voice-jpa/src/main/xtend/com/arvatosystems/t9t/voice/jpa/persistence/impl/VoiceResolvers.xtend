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
package com.arvatosystems.t9t.voice.jpa.persistence.impl

import com.arvatosystems.t9t.annotations.jpa.AllCanAccessGlobalTenant
import com.arvatosystems.t9t.annotations.jpa.AutoResolver42
import com.arvatosystems.t9t.annotations.jpa.GlobalTenantCanAccessAll
import com.arvatosystems.t9t.voice.VoiceApplicationRef
import com.arvatosystems.t9t.voice.VoiceResponseRef
import com.arvatosystems.t9t.voice.VoiceUserRef
import com.arvatosystems.t9t.voice.jpa.entities.VoiceApplicationEntity
import com.arvatosystems.t9t.voice.jpa.entities.VoiceModuleCfgEntity
import com.arvatosystems.t9t.voice.jpa.entities.VoiceResponseEntity
import com.arvatosystems.t9t.voice.jpa.entities.VoiceUserEntity

/*
 * Generates resolver classes for all entities in the doc module. The generator class itself is not used.
 */
@AutoResolver42
class VoiceResolvers {

    @AllCanAccessGlobalTenant
    def VoiceModuleCfgEntity      getVoiceModuleCfgEntity       (Long key,       boolean onlyActive) {}
    @GlobalTenantCanAccessAll
    def VoiceApplicationEntity    getVoiceApplicationEntity     (VoiceApplicationRef ref,     boolean onlyActive) {}
    @GlobalTenantCanAccessAll
    def VoiceUserEntity           getVoiceUserEntity            (VoiceUserRef ref,            boolean onlyActive) {}
    def VoiceResponseEntity       getVoiceResponseEntity        (VoiceResponseRef ref,        boolean onlyActive) {}
}
