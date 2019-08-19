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
package com.arvatosystems.t9t.genconf.be.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.genconf.ConfigDTO;
import com.arvatosystems.t9t.genconf.ConfigRef;
import com.arvatosystems.t9t.genconf.request.ConfigCrudRequest;
import com.arvatosystems.t9t.genconf.services.IConfigResolver;

import de.jpaw.dp.Jdp;

public class ConfigCrudRequestHandler extends AbstractCrudSurrogateKeyBERequestHandler<ConfigRef, ConfigDTO, FullTrackingWithVersion, ConfigCrudRequest> {

    // @Inject
    protected final IConfigResolver resolver = Jdp.getRequired(IConfigResolver.class);

    private void resetComponentValues(ConfigDTO data) {
        if (data == null || data.getConfigTypeEnum() == null) {
            return;
        }
        switch (data.getConfigTypeEnum()) {
        case INTEGRAL:
        case REFERENCE:
            data.setStringProperty(null);
            data.setObjectProperty(null);
            data.setBooleanProperty(null);
            data.setDecimalProperty(null);
            break;
        case TEXT:
            data.setObjectProperty(null);
            data.setBooleanProperty(null);
            data.setDecimalProperty(null);
            data.setIntegerProperty(null);
            break;
        case OBJECT:
            data.setStringProperty(null);
            data.setBooleanProperty(null);
            data.setDecimalProperty(null);
            data.setIntegerProperty(null);
            break;
        case BOOLEAN:
            data.setStringProperty(null);
            data.setObjectProperty(null);
            data.setDecimalProperty(null);
            data.setIntegerProperty(null);
            break;
        case FRACTIONAL:
            data.setStringProperty(null);
            data.setObjectProperty(null);
            data.setBooleanProperty(null);
            data.setIntegerProperty(null);
            break;
        }
    }

    @Override
    public ServiceResponse execute(RequestContext ctx, ConfigCrudRequest request) throws Exception {
        resetComponentValues(request.getData());  // normalize data fields, if too many have been provided
        return execute(ctx, request, resolver);
    }
}
