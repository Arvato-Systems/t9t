/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.remote.connect;

import java.util.UUID;

import jakarta.annotation.Nonnull;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.util.impl.RecordMarshallerCompactBonaparteIdentity;
import de.jpaw.bonaparte.util.impl.RecordMarshallerJson;

/**
 * Streamlined variant of the Connection class.
 */
public class DirectConnection extends AbstractConnection {
    private final HttpPostClient dlg;

    /**
     * Creates a connection without separate login, i.e. the connection is created with the provided credentials.
     *
     * @param url the URL for the connection, including /rpc path.
     * @param authHeader the full authentication header, e.g. "API-Key <uuid>" or "Bearer <...>"
     * @return the connection
     */
    public DirectConnection(@Nonnull final String url, @Nonnull final String authHeader, final boolean useJson) {
        dlg = new HttpPostClient(url, false, false, false, false, useJson ? new RecordMarshallerJson() : new RecordMarshallerCompactBonaparteIdentity());
        dlg.setBaseUrl(url);
        dlg.setAuthentication(authHeader);
    }

    @Override
    public void switchUser(UUID newApiKey) {
        throw new UnsupportedOperationException("DirectConnection does not support user switching");
    }

    @Override
    public void switchUser(String userId, String password) {
        throw new UnsupportedOperationException("DirectConnection does not support user switching");
    }

    @Override
    public BonaPortable doIO(BonaPortable rp) {
        try {
            return dlg.doIO(rp);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
}

    @Override
    public void logout() {
        throw new UnsupportedOperationException("DirectConnection does not support logout");
    }

    @Override
    public void setAuthentication(final String header) {
        dlg.setAuthentication(header);
    }
}
