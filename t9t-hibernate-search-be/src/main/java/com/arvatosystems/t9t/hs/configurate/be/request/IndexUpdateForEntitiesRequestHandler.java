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

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IAutonomousExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.hs.configurate.be.core.util.ConfigurationLoader;
import com.arvatosystems.t9t.hs.configurate.model.EntityConfig;
import com.arvatosystems.t9t.hs.configurate.request.IndexUpdateForEntitiesRequest;
import com.arvatosystems.t9t.hs.configurate.request.IndexUpdateRequest;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IndexUpdateForEntitiesRequestHandler extends AbstractRequestHandler<IndexUpdateForEntitiesRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexUpdateForEntitiesRequestHandler.class);
    private final IAutonomousExecutor autonomousExecutor = Jdp.getRequired(IAutonomousExecutor.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull final RequestContext ctx, @Nonnull final IndexUpdateForEntitiesRequest request) throws Exception {
        final List<EntityConfig> entityConfigList = ConfigurationLoader.getEntityConfigCache().getEntities();
        LOGGER.info("Updating index for {} configured entities", entityConfigList.size());
        for (final EntityConfig entityConfig : entityConfigList) {
            try {
                final IndexUpdateRequest indexUpdateRequest = new IndexUpdateRequest(entityConfig.getClassName());
                autonomousExecutor.execute(ctx, indexUpdateRequest);
            } catch (final Exception e) {
                LOGGER.error("Index update for entity {} failed!", entityConfig.getClassName(), e);
            }
        }
        return ok();
    }
}
