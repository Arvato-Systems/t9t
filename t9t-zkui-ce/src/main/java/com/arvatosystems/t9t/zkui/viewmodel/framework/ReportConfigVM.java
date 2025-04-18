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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IT9tMessagingDAO;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.ReportConfigRef;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.api.media.MediaXType;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;

@Init(superclass = true)
public class ReportConfigVM extends CrudSurrogateKeyVM<ReportConfigRef, ReportConfigDTO, FullTrackingWithVersion> {
    private static final Logger LOGGER       = LoggerFactory.getLogger(ReportConfigVM.class);
    private static final String FILE_EXTENSION_JASPER_REPORT = ".jrxml";
    private static final String DATA_SINK_ID = "reportSrc";
    protected final IT9tMessagingDAO messagingDAO = Jdp.getRequired(IT9tMessagingDAO.class);

    @NotifyChange("data.jasperReportTemplateName")
    @Command
    public void uploadReport(@ContextParam(ContextType.BIND_CONTEXT) final BindContext ctx) throws ReturnCodeException, IOException {
        LOGGER.debug("uploadReport");
        final MediaData md = messagingDAO.getUploadedData((UploadEvent)ctx.getTriggerEvent());
        final ByteArray r = md.getRawData();
        LOGGER.debug("uploadedReport of {} bytes of type {}", r == null ? md.getText().length() : r.length(), md.getMediaType());
        final String filename = md.getZ().get("fileName").toString();

        // File extension got from the upload is "bin", thus manually check the file extension from the file name.
        if (filename == null || !filename.endsWith(FILE_EXTENSION_JASPER_REPORT)) {
            Messagebox.show(session.translate("ReportConfigVM", "jasperReportOnly"),
                    session.translate("ReportConfigVM", "com.badinput"), Messagebox.OK, Messagebox.ERROR);
            LOGGER.error("ERROR: Invalid file name: {} ", filename);
            return;
        }
        data.setJasperReportTemplateName(filename);

        final Map<String, Object> additionalParameters = new HashMap<String, Object>();
        additionalParameters.put("localFilename", filename);

        final OutputSessionParameters outputSessionParameters = new OutputSessionParameters();
        outputSessionParameters.setDataSinkId(DATA_SINK_ID);
        outputSessionParameters.setCommunicationFormatType(md.getMediaType());
        outputSessionParameters.setCommunicationFormatType(MediaXType.of(MediaType.RAW));  // must be raw, nothing else accepted
        outputSessionParameters.setAdditionalParameters(additionalParameters);

        try {
            //SinkCreatedResponse response =
            messagingDAO.fileUploadRequest(outputSessionParameters,
                    r != null ? r : ByteArray.fromString(md.getText(), StandardCharsets.UTF_8));
        } catch (ReturnCodeException e) {
            LOGGER.error("ERROR: {} / {} ", e, ExceptionUtil.causeChain(e));
        }
    }
}
