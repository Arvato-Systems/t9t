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
package com.arvatosystems.t9t.hs.configurate.be.request;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusForEntitiesRequest;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusForEntitiesResponse;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusRequest;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusResponse;
import com.arvatosystems.t9t.hs.configurate.request.EntityIndexStatus;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CheckIndexStatusForEntitiesRequestHandler extends AbstractRequestHandler<CheckIndexStatusForEntitiesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckIndexStatusForEntitiesRequestHandler.class);
    private final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Nonnull
    @Override
    public CheckIndexStatusForEntitiesResponse execute(@Nonnull final RequestContext ctx, @Nonnull final CheckIndexStatusForEntitiesRequest request) throws Exception {
        final List<EntityConfig> entityConfigList = ConfigurationLoader.getEntityConfigCache().getEntities();
        LOGGER.info("Checking index status for {} configured entities", entityConfigList.size());
        final CheckIndexStatusForEntitiesResponse response = new CheckIndexStatusForEntitiesResponse();
        final List<EntityIndexStatus> results = new ArrayList<>(entityConfigList.size());
        response.setResults(results);
        for (final EntityConfig entityConfig : entityConfigList) {
            final EntityIndexStatus entityIndexStatus = new EntityIndexStatus();
            entityIndexStatus.setEntityName(entityConfig.getClassName());
            results.add(entityIndexStatus);
            try {
                final CheckIndexStatusRequest checkIndexStatusRequest = new CheckIndexStatusRequest(entityConfig.getClassName());
                final CheckIndexStatusResponse indexStatusResponse = autonomousExecutor.executeAndCheckResult(ctx, checkIndexStatusRequest,
                    CheckIndexStatusResponse.class);
                entityIndexStatus.setIndexValid(indexStatusResponse.getValidationError() == null);
                entityIndexStatus.setErrorMessage(indexStatusResponse.getValidationError());
            } catch (final Exception e) {
                entityIndexStatus.setErrorMessage(e.getMessage());
                LOGGER.error("Index status check for entity {} failed!", entityConfig.getClassName(), e);
            }
        }
        return response;
    }
}
