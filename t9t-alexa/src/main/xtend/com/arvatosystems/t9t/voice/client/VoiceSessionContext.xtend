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
package com.arvatosystems.t9t.voice.client

import java.util.Map
import java.util.UUID

class VoiceSessionContext {
    var public String providerSessionKey;
    var public UUID connectionApiKey;
    var public boolean shouldTerminateWhenDone;
    var public String userName;
    var public String userInternalId;
    // var public String connectionAuthentication;  // usually "Bearer " + encodedJwt
    var public Map<String,String> nextCallbacks;    // special response mapping
    var public Map<String,String> previousCallbacks;    // special response mapping before - deleted after intent execution
}
