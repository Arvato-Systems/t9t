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

import com.arvatosystems.t9t.voice.VoiceResponseInternalKey
import com.arvatosystems.t9t.voice.VoiceResponseKey
import com.arvatosystems.t9t.voice.VoiceResponseRef
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver
import de.jpaw.dp.Inject
import de.jpaw.dp.Singleton
import de.jpaw.dp.Specializes

@Singleton
@Specializes
class VoiceResponseExtResolver extends VoiceResponseEntityResolver {

    @Inject IVoiceApplicationEntityResolver applicationResolver;

    override protected VoiceResponseRef resolveNestedRefs(VoiceResponseRef ref) {
        if (ref instanceof VoiceResponseKey) {
            return new VoiceResponseInternalKey => [
                applicationRef = applicationResolver.getRef(ref.applicationRef, false)
                languageCode   = ref.languageCode
                key            = ref.key
            ]
        }
        return super.resolveNestedRefs(ref);
    }
}
