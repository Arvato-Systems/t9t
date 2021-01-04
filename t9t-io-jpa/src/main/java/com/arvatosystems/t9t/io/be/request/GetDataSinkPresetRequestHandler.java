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
package com.arvatosystems.t9t.io.be.request;

import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.io.request.GetDataSinkPresetRequest;
import com.arvatosystems.t9t.io.request.GetDataSinkPresetsResponse;
import com.arvatosystems.t9t.io.services.IDataSinkDefaultConfigurationProvider;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;

import de.jpaw.dp.Jdp;

public class GetDataSinkPresetRequestHandler extends AbstractReadOnlyRequestHandler<GetDataSinkPresetRequest> {

    @Override
    public GetDataSinkPresetsResponse execute(RequestContext ctx, GetDataSinkPresetRequest rq) {
        final boolean isInput = Boolean.TRUE.equals(rq.getIsInput());
        final IDataSinkDefaultConfigurationProvider configPresetProvider = isInput ?
                Jdp.getRequired(IInputDataTransformer.class,     rq.getQualifier()) :
                Jdp.getRequired(IPreOutputDataTransformer.class, rq.getQualifier());
        final GetDataSinkPresetsResponse resp = new GetDataSinkPresetsResponse();
        resp.setPreset(configPresetProvider.getDefaultConfiguration(isInput));
        return resp;
    }
}
