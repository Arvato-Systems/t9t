package com.arvatosystems.t9t.zkui.viewmodel.framework;

import com.arvatosystems.t9t.auth.PasswordBlacklistDTO;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("passwordBlacklist")
public class PasswordBlacklistKeyFromDataProvider implements IKeyFromDataProvider<PasswordBlacklistDTO, NoTracking> {

    @Override
    public SearchFilter getFilterForKey(DataWithTracking<PasswordBlacklistDTO, NoTracking> dwt) {

        final UnicodeFilter passwordInBlacklistFilter = new UnicodeFilter("passwordInBlacklist");
        passwordInBlacklistFilter.setEqualsValue(dwt.getData().getPasswordInBlacklist());

        return passwordInBlacklistFilter;
    }
}
