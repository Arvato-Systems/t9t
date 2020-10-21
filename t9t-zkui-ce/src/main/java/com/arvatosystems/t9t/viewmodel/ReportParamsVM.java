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
package com.arvatosystems.t9t.viewmodel;

import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zhtml.Messagebox;

import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.components.crud.CrudSurrogateKeyVM;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.ReportParamsRef;
import com.arvatosystems.t9t.services.T9TMessagingDAO;

import de.jpaw.dp.Jdp;

@Init(superclass = true)
public class ReportParamsVM extends CrudSurrogateKeyVM<ReportParamsRef, ReportParamsDTO, FullTrackingWithVersion> {

    private final T9TMessagingDAO messagingDAO = Jdp.getRequired(T9TMessagingDAO.class);

    @Override
    @Command
    public void commandSave() {

        boolean proceed = true;

        // ReportConfigDTO config = getReportConfig(data.getReportConfigRef());

        if (data.getIntervalCategory() == null) {
            Messagebox.show(session.translate("reportParams", "invalid.by.intervalCategory"));
            return;
        }

        switch (data.getIntervalCategory()) {
        case BY_TIME: {
            if (data.getInterval() == null) {
                Messagebox.show(session.translate("reportParams", "invalid.by.time"));
                proceed = false;
            }
            break;
        }
        case BY_DURATION: {
            if (data.getIntervalDays() == null || data.getIntervalSeconds() == null) {
                Messagebox.show(session.translate("reportParams", "invalid.by.duration"));
                proceed = false;
            }
            break;
        }
        case BY_RANGE: {
            if (data.getFromDate() == null || data.getToDate() == null) {
                Messagebox.show(session.translate("reportParams", "invalid.by.range"));
                proceed = false;
            }
            break;
        }
        }

        if (proceed) {
            super.commandSave();
        }
    }

    @Command
    public void runReport() throws ReturnCodeException {
        Long sinkRef = messagingDAO.runReportRequest(this.getData());
        if (sinkRef != null) {
            messagingDAO.downloadFileAndSave(sinkRef);
        }
    }
}
