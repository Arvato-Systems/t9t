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
package com.arvatosystems.t9t.component.fields.fixedfilters;

import java.time.LocalDate;

import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("jan2Year2000")
public class Jan2Year2000Filter implements IFixedFilter {
    private static final DayFilter FILTER = new DayFilter("day");
    static {
        FILTER.setEqualsValue(LocalDate.of(2000,1,2));
        FILTER.freeze();
    }

    @Override
    public SearchFilter get() {
        return FILTER;
    }
}
