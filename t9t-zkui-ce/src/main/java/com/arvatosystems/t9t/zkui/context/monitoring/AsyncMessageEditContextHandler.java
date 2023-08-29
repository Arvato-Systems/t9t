/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.context.monitoring;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.base.output.ExportStatusEnum;
import com.arvatosystems.t9t.io.AsyncMessageDTO;
import com.arvatosystems.t9t.io.request.AsyncMessageCrudRequest;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.components.basic.ModalWindows;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.bonaparte.core.MapParser;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.json.JsonParser;

@Singleton
@Named("asyncMessage.ctx.editMessage")
public class AsyncMessageEditContextHandler implements IGridContextMenu<AsyncMessageDTO> {
    protected final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncMessageEditContextHandler.class);

    @Override
    public boolean isEnabled(final DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        return dwt.getData().getStatus() != ExportStatusEnum.RESPONSE_OK;
    }

    @Override
    public void selected(final Grid28 lb, final DataWithTracking<AsyncMessageDTO, TrackingBase> dwt) {
        final AsyncMessageDTO dto = dwt.getData();
        final BonaPortable payload = dto.getPayload();
        LOGGER.debug("EDIT invoked on message ref {}, payload type {}", dto.getObjectRef(), payload.ret$PQON());

        final Info info = new Info();
        info.setText(JsonComposerPrettyPrint.toJsonString(payload));
        ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {
            final Map<String, Object> jsonAsMap = (new JsonParser(d.getText(), false)).parseObject();
            final BonaPortable edited = MapParser.asBonaPortable(jsonAsMap, StaticMeta.OUTER_BONAPORTABLE_FOR_JSON);
            if (edited.getClass().equals(payload.getClass())) {
                LOGGER.debug("UPDATING async message of ref {}, payload type {}", dto.getObjectRef(), payload.ret$PQON());
                final AsyncMessageCrudRequest crud = new AsyncMessageCrudRequest();
                crud.setCrud(OperationType.UPDATE);
                crud.setKey(dto.getObjectRef());
                dto.setPayload(edited);
                crud.setData(dto);
                remoteUtils.executeExpectOk((RequestParameters)crud);
            } else {
                LOGGER.error("Editing changed the class from {} to {}", payload.getClass().getCanonicalName(), edited.getClass().getCanonicalName());
                Messagebox.show("Class changed", "Error", Messagebox.CANCEL, Messagebox.EXCLAMATION);
            }
        });
    }
}
