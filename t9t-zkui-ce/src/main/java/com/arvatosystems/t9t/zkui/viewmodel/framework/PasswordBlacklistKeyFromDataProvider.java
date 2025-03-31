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
