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
package com.arvatosystems.t9t.auth.hooks;

import com.arvatosystems.t9t.base.auth.AuthenticationResponse;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.types.AuthenticationParameters;
import com.arvatosystems.t9t.base.types.SessionParameters;

/**
 * Hook to provide authentication by some other AuthenticationParameters (not ApiKeyAuthentication or PasswordAuthentication).
 * The default implementation throws some exception.
 */
public interface IOtherAuthentication {
    /** The preprocess method allows to convert AuthenticationParameters of one type into some of another type, for example just a different user selection type. */
    default AuthenticationParameters preprocess(RequestContext ctx, SessionParameters sp, AuthenticationParameters ap) {
        return ap;
    }

    /** The auth method allows to implement completely new types of authentication. */
    default AuthenticationResponse auth(RequestContext ctx, AuthenticationParameters ap, String locale, String zoneinfo) {
        throw new UnsupportedOperationException("Unsupported authentication parameters: " + ap.getClass().getCanonicalName());
    }
}
