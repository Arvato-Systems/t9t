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

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.HibernateSearchConfiguration;
import com.arvatosystems.t9t.hs.T9tHibernateSearchException;
import com.arvatosystems.t9t.hs.configurate.be.core.service.IConfigurationService;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusRequest;
import com.arvatosystems.t9t.hs.configurate.request.CheckIndexStatusResponse;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for creating/recreating hibernate search indexes
 */
public class CheckIndexStatusRequestHandler extends AbstractRequestHandler<CheckIndexStatusRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckIndexStatusRequestHandler.class);
    private final HibernateSearchConfiguration sc = ConfigProvider.getConfiguration().getHibernateSearchConfiguration();
    private final IConfigurationService service = Jdp.getRequired(IConfigurationService.class);

    @Nonnull
    @Override
    public CheckIndexStatusResponse execute(@Nonnull final RequestContext ctx, @Nonnull final CheckIndexStatusRequest request) throws Exception {

        if (sc == null) {
            throw new T9tException(T9tHibernateSearchException.HIBERNATE_SEARCH_CONFIG_NOT_FOUND, "Missing hibernate search configuration");
        }
        LOGGER.info("Check index for entity: {} and strategy: {}", request.getEntityName(), sc.getSearchType());

        // Get entity class by name
        final Class<?> entityClass = Class.forName(request.getEntityName());

        final String validationError = service.checkIndexStatus(entityClass);
        LOGGER.info("Index validation for entity {} is {}. {}", request.getEntityName(), (validationError == null ? "success" : "failed"),
            (validationError == null ? "" : validationError));
        final CheckIndexStatusResponse response = new CheckIndexStatusResponse();
        final String error = validationError == null ? null : validationError.substring(0,
            Math.min(validationError.length(), CheckIndexStatusResponse.meta$$validationError.getLength()));
        response.setValidationError(error);
        return response;
    }
}
