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
package com.arvatosystems.t9t.zkui.components.dropdown28.factories;

import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.updates.UpdateStatusDTO;
import com.arvatosystems.t9t.updates.UpdateStatusRef;
import com.arvatosystems.t9t.updates.UpdateStatusTicketKey;
import com.arvatosystems.t9t.updates.request.LeanUpdateStatusSearchRequest;
import com.arvatosystems.t9t.zkui.components.dropdown28.db.Dropdown28Db;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("ticketId")
@Singleton
public class Dropdown28FactoryTicketId implements IDropdown28DbFactory<UpdateStatusRef> {

    @Override
    public String getDropdownId() {
        return "ticketId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanUpdateStatusSearchRequest();
    }

    @Override
    public UpdateStatusRef createRef(Long ref) {
        return new UpdateStatusRef(ref);
    }

    @Override
    public UpdateStatusTicketKey createKey(String id) {
        return new UpdateStatusTicketKey(id);
    }

    @Override
    public Dropdown28Db<UpdateStatusRef> createInstance() {
        return new Dropdown28Db<UpdateStatusRef>(this);
    }

    @Override
    public String getIdFromKey(UpdateStatusRef key) {
        if (key instanceof UpdateStatusTicketKey ticketKey) {
            return ticketKey.getTicketId();
        }
        if (key instanceof UpdateStatusDTO dto) {
            return dto.getTicketId();
        }
        return null;
    }
}
