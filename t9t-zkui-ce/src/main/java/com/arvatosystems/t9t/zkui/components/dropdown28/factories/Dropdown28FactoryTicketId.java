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
