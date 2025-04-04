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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.request.GetInternalServicesRequest;
import com.arvatosystems.t9t.core.request.GetInternalServicesResponse;
import com.arvatosystems.t9t.zkui.components.basic.ModalWindows;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IT9tMessagingDAO;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;

import de.jpaw.bonaparte.core.JsonComposerPrettyPrint;
import de.jpaw.dp.Jdp;

// viewModel only required for the button command. This could be done via context menu as well!

public class CannedViewModel extends CrudSurrogateKeyVM<CannedRequestRef, CannedRequestDTO, FullTrackingWithVersion> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CannedViewModel.class);

    protected final IT9tMessagingDAO t9tRequestDAO = Jdp.getRequired(IT9tMessagingDAO.class);
    protected final IT9tRemoteUtils t9tRemoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);

    private final List<String> internalServices = new ArrayList<>();

    @Init(superclass = true)
    public void init() {
        final GetInternalServicesResponse internalServicesResp = t9tRemoteUtils.executeExpectOk(new GetInternalServicesRequest(),
            GetInternalServicesResponse.class);
        internalServices.addAll(internalServicesResp.getInternalServiceKeys());
    }

    @Command
    public final void executeCannedRequest(@BindingParam("service") String service) throws ReturnCodeException {
        if (data == null || data.getObjectRef() == null)
            return;
        LOGGER.debug("executeCannedRequest with objectRef {}", data.getObjectRef());
        final ServiceResponse response = t9tRequestDAO.executeCannedRequest(new CannedRequestRef(data.getObjectRef()), service);
        final Info info = new Info();
        if (response != null) {
            info.setText(JsonComposerPrettyPrint.toJsonString(response));
        }
        ModalWindows.runModal("/screens/scheduler/cannedResponse28.zul", null, info, false, d -> { });
    }

    public List<String> getInternalServices() {
        return internalServices;
    }
}
