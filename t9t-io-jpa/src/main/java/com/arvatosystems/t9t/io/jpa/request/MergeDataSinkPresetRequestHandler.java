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
package com.arvatosystems.t9t.io.jpa.request;

import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.in.services.IInputDataTransformer;
import com.arvatosystems.t9t.io.DataSinkPresets;
import com.arvatosystems.t9t.io.jpa.entities.DataSinkEntity;
import com.arvatosystems.t9t.io.jpa.mapping.IDataSinkDTOMapper;
import com.arvatosystems.t9t.io.jpa.persistence.IDataSinkEntityResolver;
import com.arvatosystems.t9t.io.request.MergeDataSinkPresetRequest;
import com.arvatosystems.t9t.io.request.MergeDataSinkPresetResponse;
import com.arvatosystems.t9t.io.services.IDataSinkDefaultConfigurationProvider;
import com.arvatosystems.t9t.out.services.IPreOutputDataTransformer;

import de.jpaw.dp.Jdp;

public class MergeDataSinkPresetRequestHandler extends AbstractRequestHandler<MergeDataSinkPresetRequest> {
    protected final IDataSinkEntityResolver resolver = Jdp.getRequired(IDataSinkEntityResolver.class);
    protected final IDataSinkDTOMapper mapper = Jdp.getRequired(IDataSinkDTOMapper.class);

    @Override
    public MergeDataSinkPresetResponse execute(RequestContext ctx, MergeDataSinkPresetRequest rq) {
        final MergeDataSinkPresetResponse resp = new MergeDataSinkPresetResponse();
        resp.setWasMerged(false);
        final DataSinkEntity dataSink = resolver.getEntityDataForKey(rq.getDataSinkRef(), false);
        if (dataSink.getPreTransformerName() != null) {
            final boolean isInput = Boolean.TRUE.equals(dataSink.getIsInput());
            final IDataSinkDefaultConfigurationProvider configPresetProvider = isInput ?
              Jdp.getRequired(IInputDataTransformer.class,     dataSink.getPreTransformerName()) :
              Jdp.getRequired(IPreOutputDataTransformer.class, dataSink.getPreTransformerName());
            final DataSinkPresets preset = configPresetProvider.getDefaultConfiguration(isInput);
            if (preset != null) {
                resp.setWasMerged(true);
                dataSink.setBaseClassPqon(preset.getBaseClassPqon());
                dataSink.setJaxbContextPath(preset.getJaxbContextPath());
                dataSink.setXmlDefaultNamespace(preset.getXmlDefaultNamespace());
                dataSink.setXmlRootElementName(preset.getXmlRootElementName());
                dataSink.setXmlRecordName(preset.getXmlRecordName());
                dataSink.setXmlNamespacePrefix(preset.getXmlNamespacePrefix());
                dataSink.setXmlHeaderElements(preset.getXmlHeaderElements());
                dataSink.setXmlFooterElements(preset.getXmlFooterElements());
                dataSink.setWriteTenantId(preset.getWriteTenantId());
            }
        }
        resp.setDwt(mapper.mapToDwt(dataSink));
        return resp;
    }
}
