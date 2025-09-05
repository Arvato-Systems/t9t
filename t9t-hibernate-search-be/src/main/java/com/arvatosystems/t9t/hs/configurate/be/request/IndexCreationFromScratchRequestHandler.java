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
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.HibernateSearchConfiguration;
import com.arvatosystems.t9t.hs.configurate.be.core.service.IConfigurationService;
import com.arvatosystems.t9t.hs.configurate.request.IndexCreationFromScratchRequest;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for creating/recreating hibernate search indexes
 */
public class IndexCreationFromScratchRequestHandler extends AbstractRequestHandler<IndexCreationFromScratchRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexCreationFromScratchRequestHandler.class);
    private final HibernateSearchConfiguration sc = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();
    private final IConfigurationService service = Jdp.getRequired(IConfigurationService.class);

    @Nonnull
    @Override
    public ServiceResponse execute(@Nonnull RequestContext ctx, @Nonnull IndexCreationFromScratchRequest request) throws Exception {

        LOGGER.info("Creating index for entity: {} and strategy: {}", request.getEntityName(), sc.getSearchType());

        try {
            // Get entity class by name
            Class<?> entityClass = Class.forName(request.getEntityName());

            // Recreate the index
            service.createIndexesFromScratch(entityClass);

            LOGGER.info("Successfully created index for entity: {}", request.getEntityName());
            return ok();

        } catch (ClassNotFoundException e) {
            LOGGER.error("Entity class not found: {}", request.getEntityName(), e);
            throw new RuntimeException("Entity class not found: " + request.getEntityName(), e);
        } catch (Exception e) {
            LOGGER.error("Error creating index for entity {}: {}", request.getEntityName(), e.getMessage(), e);
            throw new RuntimeException("Failed to create index for entity: " + request.getEntityName(), e);
        }
    }
}
