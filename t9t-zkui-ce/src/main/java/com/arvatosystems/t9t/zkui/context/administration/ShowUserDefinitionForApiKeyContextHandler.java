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
package com.arvatosystems.t9t.zkui.context.administration;

import com.arvatosystems.t9t.auth.ApiKeyDTO;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.util.JumpTool;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("apikey.ctx.showUserDefinition")
public class ShowUserDefinitionForApiKeyContextHandler implements IGridContextMenu<ApiKeyDTO> {

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<ApiKeyDTO, TrackingBase> dwt) {
        final ApiKeyDTO dto = dwt.getData();
        JumpTool.jump("screens/user_admin/user28.zul", "objectRef", dto.getUserRef().getObjectRef(), "screens/user_admin/apikey28.zul");
    }
}
