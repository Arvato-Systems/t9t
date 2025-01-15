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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import com.arvatosystems.t9t.auth.PasswordBlacklistDTO;
import com.arvatosystems.t9t.auth.PasswordBlacklistKey;
import com.arvatosystems.t9t.auth.request.ClearPasswordBlacklistRequest;
import com.arvatosystems.t9t.auth.request.PasswordBlacklistCrudRequest;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.crud.CrudStringKeyRequest;
import com.arvatosystems.t9t.base.crud.CrudStringKeyResponse;
import com.arvatosystems.t9t.zkui.services.IT9tMessagingDAO;
import com.arvatosystems.t9t.zkui.viewmodel.AbstractCrudVM;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Messagebox;

@Init(superclass = true)
public class PasswordBlacklistVM extends
AbstractCrudVM<PasswordBlacklistKey, PasswordBlacklistDTO, NoTracking, CrudStringKeyRequest<PasswordBlacklistDTO, NoTracking>, CrudStringKeyResponse<PasswordBlacklistDTO, NoTracking>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordBlacklistVM.class);

    protected final IT9tMessagingDAO messagingDAO = Jdp.getRequired(IT9tMessagingDAO.class);

    @Override
    protected void clearKey() {
        data.setPasswordInBlacklist(null);
        data.setPasswordCreation(null);
    }

    @Override
    protected CrudStringKeyRequest<PasswordBlacklistDTO, NoTracking> createCrudWithKey() {
        CrudStringKeyRequest<PasswordBlacklistDTO, NoTracking> crudRq = new PasswordBlacklistCrudRequest();
        crudRq.setKey(data.getPasswordInBlacklist());
        return crudRq;
    }

    @Override
    protected void clearData() {
        super.clearData();
        data = crudViewModel.dtoClass.newInstance();
    }

    @Command
    public void commandClear() {
        showClearConfirmationDialog(new EventListener<>() {

            @Override
            public void onEvent(final Event event) throws Exception {
                if (event.getName().equals(Messagebox.ON_YES)) {
                    final ClearPasswordBlacklistRequest rq = new ClearPasswordBlacklistRequest();
                    remoteUtil.executeExpectOk(rq, ServiceResponse.class);
                    clearData();
                    setRefresher(Boolean.FALSE);
                }
            }
        });

    }

    @Command
    public void uploadPasswordBlacklist(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws IOException {
        // accept a file selection by the user
        final MediaData md = messagingDAO.getUploadedData((UploadEvent) ctx.getTriggerEvent());

        // validate that we got text data, of either CSV or plain text
        final String data = md.getText();
        if (data == null) {
            Messagebox.show(session.translate("batchUpload", "needText"), session.translate("batchUpload", "com.badinput"), Messagebox.OK, Messagebox.ERROR);
            LOGGER.error("ERROR: Binary data uploaded");
            return;
        }
        if (md.getMediaType().getBaseEnum() != MediaType.TEXT && md.getMediaType().getBaseEnum() != MediaType.CSV) {
            Messagebox.show(session.translate("batchUpload", "csvOrTxtOnly"), session.translate("batchUpload", "com.badinput"), Messagebox.OK,
                Messagebox.ERROR);
            LOGGER.error("ERROR: Media type {} not accepted here", md.getMediaType());
            return;
        }
        final String[] rows = data.split(System.lineSeparator());
        final List<String> passwords = new ArrayList<>(rows.length);
        for (final String row : rows) {
            final String trimmed = row.trim();
            if (!T9tUtil.isBlank(trimmed)) {
                passwords.add(trimmed);
            }
        }
        Instant creationTime = Instant.now();
        for (final String password : passwords) {
            final CrudStringKeyRequest<PasswordBlacklistDTO, NoTracking> crudRq = (CrudStringKeyRequest<PasswordBlacklistDTO, NoTracking>) crudViewModel.crudClass
                .newInstance();

            PasswordBlacklistDTO dto = new PasswordBlacklistDTO();
            dto.setPasswordInBlacklist(password);
            dto.setPasswordCreation(creationTime);

            crudRq.setCrud(OperationType.MERGE);
            crudRq.setData(dto);
            crudRq.setKey(password);
            runCrud(crudRq, Boolean.FALSE);
        }
        LOGGER.debug("PasswordBlacklist upload done");
        Messagebox.show(session.translate("batchUpload", "passwordBlacklistUploadSuccess"));
    }

    protected void showClearConfirmationDialog(final EventListener<Event> eventListener) {
        Messagebox.show(session.translate(COMMON, "clearConfirmationMessage"), session.translate(COMMON, "clearConfirmation"),
            Messagebox.YES | Messagebox.CANCEL, Messagebox.EXCLAMATION, eventListener);
    }
}
