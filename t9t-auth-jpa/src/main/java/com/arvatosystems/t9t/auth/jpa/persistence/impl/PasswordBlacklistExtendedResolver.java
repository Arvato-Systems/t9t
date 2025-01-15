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
package com.arvatosystems.t9t.auth.jpa.persistence.impl;

import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.dp.Singleton;
import de.jpaw.dp.Specializes;
import java.util.Collections;
import java.util.List;

@Specializes
@Singleton
public class PasswordBlacklistExtendedResolver extends PasswordBlacklistEntityResolver {

    private static final List<SortColumn> DEFAULT_SORT_COLUMN = Collections.singletonList(new SortColumn("passwordInBlacklist", true));

    @Override
    protected List<SortColumn> getDefaultSortColumns() {
        return DEFAULT_SORT_COLUMN;
    }
}
