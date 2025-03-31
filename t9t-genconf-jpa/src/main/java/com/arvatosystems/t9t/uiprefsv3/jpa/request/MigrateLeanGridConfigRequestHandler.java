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
package com.arvatosystems.t9t.uiprefsv3.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.uiprefsv3.jpa.entities.LeanGridConfigEntity;
import com.arvatosystems.t9t.uiprefsv3.jpa.persistence.ILeanGridConfigEntityResolver;
import com.arvatosystems.t9t.uiprefsv3.request.MigrateLeanGridConfigRequest;
import de.jpaw.bonaparte.core.JsonComposer;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.dp.Jdp;
import de.jpaw.json.JsonParser;
import java.util.List;

public class MigrateLeanGridConfigRequestHandler extends AbstractRequestHandler<MigrateLeanGridConfigRequest> {

    private final ILeanGridConfigEntityResolver resolver = Jdp.getRequired(ILeanGridConfigEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final MigrateLeanGridConfigRequest request)
        throws Exception {
        List<LeanGridConfigEntity> gridConfigs = resolver.findByAll(false);
        for (LeanGridConfigEntity gridConfig : gridConfigs) {
            String beforeGridConfig = JsonComposer.toJsonString(gridConfig.getGridPrefs());
            String afterGridConfig = beforeGridConfig.replace(request.getFrom(), request.getTo());
            JsonParser jp = new JsonParser(afterGridConfig, false);
            UILeanGridPreferences gridPrefs = (UILeanGridPreferences) MapParser.asBonaPortable(jp.parseObject(),
                StaticMeta.OUTER_BONAPORTABLE);
            gridConfig.setGridPrefs(gridPrefs);
        }
        return ok();
    }
}
