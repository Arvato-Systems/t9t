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

import com.arvatosystems.t9t.voice.VoiceUserInternalKey
import com.arvatosystems.t9t.voice.VoiceUserKey
import com.arvatosystems.t9t.voice.VoiceUserRef
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.dp.Specializes

@Singleton
@Specializes
class VoiceUserExtResolver extends VoiceUserEntityResolver {

    @Inject IVoiceApplicationEntityResolver applicationResolver;

    override protected VoiceUserRef resolveNestedRefs(VoiceUserRef ref) {
        if (ref instanceof VoiceUserKey) {
            return new VoiceUserInternalKey => [
                applicationRef = applicationResolver.getRef(ref.applicationRef, false)
                providerId     = ref.providerId
            ]
        }
        return super.resolveNestedRefs(ref);
    }
}
