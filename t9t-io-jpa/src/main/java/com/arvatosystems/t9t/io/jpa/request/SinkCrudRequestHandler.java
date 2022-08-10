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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractCrudSurrogateKeyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.SinkRef;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.io.jpa.entities.SinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.ISinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.ISinkEntityResolver;
import com.arvatosystems.t9t.io.request.SinkCrudRequest;

import de.jpaw.dp.Jdp;

public class SinkCrudRequestHandler extends AbstractCrudSurrogateKeyRequestHandler<SinkRef, SinkDTO, FullTrackingWithVersion, SinkCrudRequest, SinkEntity> {

    private static final String[] FORBIDDEN_FILE_PATH_ELEMENTS = { ":", "\\", "../" };

    private final ISinkEntityResolver sinksResolver = Jdp.getRequired(ISinkEntityResolver.class);
    private final ISinkDTOMapper sinksMapper = Jdp.getRequired(ISinkDTOMapper.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final SinkCrudRequest crudRequest) throws Exception {
        return execute(ctx, sinksMapper, sinksResolver, crudRequest);
    }

    @Override
    protected final void validateUpdate(final SinkEntity current, final SinkDTO intended) {
        if (intended.getCommTargetChannelType().equals(CommunicationTargetChannelType.FILE)) {
            validateFilePathPattern(intended.getFileOrQueueName());
        }
    }

    @Override
    protected final void validateCreate(final SinkDTO intended) {
        if (intended.getCommTargetChannelType().equals(CommunicationTargetChannelType.FILE)) {
            validateFilePathPattern(intended.getFileOrQueueName());
        }
    }

    private void validateFilePathPattern(final String pattern) {
        for (final String forbiddenElement : FORBIDDEN_FILE_PATH_ELEMENTS) {
            if (pattern.contains(forbiddenElement)) {
                throw new T9tException(T9tIOException.FORBIDDEN_FILE_PATH_ELEMENTS);
            }
        }

        if (pattern.startsWith("/")) {
            throw new T9tException(T9tIOException.FORBIDDEN_FILE_PATH_ELEMENTS);
        }
    }
}
