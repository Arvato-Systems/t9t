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
package com.arvatosystems.t9t.voice.jpa.persistence.impl;

import com.arvatosystems.t9t.voice.VoiceUserInternalKey;
import com.arvatosystems.t9t.voice.VoiceUserKey;
import com.arvatosystems.t9t.voice.VoiceUserRef;
import com.arvatosystems.t9t.voice.jpa.persistence.IVoiceApplicationEntityResolver;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;

@Singleton
@Specializes
public class VoiceUserExtResolver extends VoiceUserEntityResolver {
    protected final IVoiceApplicationEntityResolver applicationResolver = Jdp.getRequired(IVoiceApplicationEntityResolver.class);

    @Override
    protected VoiceUserRef resolveNestedRefs(final VoiceUserRef ref) {
        if (ref instanceof VoiceUserKey key) {
            final VoiceUserInternalKey inKey = new VoiceUserInternalKey();
            inKey.setApplicationRef(applicationResolver.getRef(key.getApplicationRef(), false));
            inKey.setProviderId(key.getProviderId());
            return inKey;
        }
        return super.resolveNestedRefs(ref);
    }
}
