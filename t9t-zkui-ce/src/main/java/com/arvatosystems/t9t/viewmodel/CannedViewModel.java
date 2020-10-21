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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.components.crud.CrudSurrogateKeyVM;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.services.IT9TMessagingDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;

import de.jpaw.dp.Jdp;

// viewModel only required for the button command. This could be done via context menu as well!

@Init(superclass=true)
public class CannedViewModel extends CrudSurrogateKeyVM<CannedRequestRef, CannedRequestDTO, FullTrackingWithVersion> {
    private static final Logger LOGGER = LoggerFactory.getLogger(CannedViewModel.class);

    protected final IT9TMessagingDAO t9tRequestDAO = Jdp.getRequired(IT9TMessagingDAO.class);

    @Command
    public final void executeCannedRequest() throws ReturnCodeException {
        if (data == null || data.getObjectRef() == null)
            return;
        LOGGER.debug("executeCannedRequest with objectRef {}", data.getObjectRef());
        t9tRequestDAO.executeCannedRequest(new CannedRequestRef(data.getObjectRef()));
    }
}
