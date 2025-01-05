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
package com.arvatosystems.t9t.server.services;

import java.io.Closeable;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.server.ExecutionSummary;
import com.arvatosystems.t9t.server.InternalHeaderParameters;

import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.pojos.api.auth.UserLogLevelType;
import de.jpaw.util.ApplicationException;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * API used by the central request dispatcher to log requests conditionally (based on settings of logLevel in the JWT).
 */
public interface IRequestLogger extends Closeable {

    /** Opens the request logger. First call in regular lifecycle. */
    void open();

    /** Computes the effective log level, based on the return code (error vs successful) and settings of the JWT. */
    default UserLogLevelType calculateLogLevel(final JwtInfo jwtInfo, final int returnCode) {
        if (ApplicationException.isOk(returnCode)) {
            return jwtInfo.getLogLevel() == null ? UserLogLevelType.MESSAGE_ENTRY : jwtInfo.getLogLevel();
        } else {
            if (jwtInfo.getLogLevelErrors() != null) {
                return jwtInfo.getLogLevelErrors();
            } else if (jwtInfo.getLogLevel() != null) {
                return jwtInfo.getLogLevel();
            } else {
                return UserLogLevelType.REQUESTS;
            }
        }
    }

    /**
     * Logs a single request.
     */
    void logRequest(@Nonnull InternalHeaderParameters hdr, @Nonnull ExecutionSummary summary,
      @Nullable RequestParameters params, @Nullable ServiceResponse response, int retriesDone);

    /** Flushes unwritten buffers. */
    void flush();

    /** Closes the logger. Shuts down the message writer; last call in regular lifecycle. */
    @Override
    void close();
}
