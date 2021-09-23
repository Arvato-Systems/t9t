/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.execution

import com.arvatosystems.t9t.base.types.AuthenticationParameters
import com.arvatosystems.t9t.base.types.SessionParameters
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo
import org.eclipse.xtend.lib.annotations.Data

@Data
class AuthInfo {
    SessionParameters sessionParameters
    AuthenticationParameters authenticationParameters
    JwtInfo jwtInfo
    String encodedJwt
}
