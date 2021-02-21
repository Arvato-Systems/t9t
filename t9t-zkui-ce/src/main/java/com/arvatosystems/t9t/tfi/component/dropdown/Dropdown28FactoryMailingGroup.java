package com.arvatosystems.t9t.tfi.component.dropdown;

import com.arvatosystems.t9t.base.search.LeanSearchRequest;
import com.arvatosystems.t9t.doc.MailingGroupDTO;
import com.arvatosystems.t9t.doc.MailingGroupKey;
import com.arvatosystems.t9t.doc.MailingGroupRef;
import com.arvatosystems.t9t.doc.request.LeanMailingGroupSearchRequest;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("mailGroupId")
@Singleton
public class Dropdown28FactoryMailingGroup implements IDropdown28DbFactory<MailingGroupRef> {
    @Override
    public String getDropdownId() {
        return "mailGroupId";
    }

    @Override
    public LeanSearchRequest getSearchRequest() {
        return new LeanMailingGroupSearchRequest();
    }

    @Override
    public MailingGroupRef createRef(Long ref) {
        return new MailingGroupRef(ref);
    }

    @Override
    public MailingGroupKey createKey(String id) {
        return new MailingGroupKey(id);
    }

    @Override
    public Dropdown28Db<MailingGroupRef> createInstance() {
        return new Dropdown28Db<MailingGroupRef>(this);
    }

    @Override
    public String getIdFromKey(MailingGroupRef key) {
        if (key instanceof MailingGroupKey)
            return ((MailingGroupKey)key).getMailingGroupId();
        if (key instanceof MailingGroupDTO)
            return ((MailingGroupDTO)key).getMailingGroupId();
        return null;
    }
}
