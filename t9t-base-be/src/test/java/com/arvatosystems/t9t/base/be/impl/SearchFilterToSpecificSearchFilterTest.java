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
package com.arvatosystems.t9t.base.be.impl;

import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.arvatosystems.t9t.base.services.ISearchFilterUtil;
import com.arvatosystems.t9t.base.services.ISearchTools;
import com.google.common.collect.Sets;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.IntFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Jdp;

public class SearchFilterToSpecificSearchFilterTest {

    ISearchFilterUtil searchFilterUtil;
    private ISearchTools searchTools = Mockito.mock(ISearchTools.class);

    @Before
    public void setup() {
        Jdp.bindInstanceTo(searchTools, ISearchTools.class);
    }

    private SearchFilter createSearchFilter() {
        AndFilter searchFilter = new AndFilter();
        searchFilter.setFilter1(new AsciiFilter("A"));
        searchFilter.setFilter2(new OrFilter());

        ((OrFilter)searchFilter.getFilter2()).setFilter1(new AsciiFilter("B"));

        NotFilter notFilter = new NotFilter();
        ((OrFilter)searchFilter.getFilter2()).setFilter2(notFilter);

        notFilter.setFilter(new AndFilter());
        ((AndFilter)notFilter.getFilter()).setFilter1(new LongFilter("C"));
        ((AndFilter)notFilter.getFilter()).setFilter2(new IntFilter("D"));

        return searchFilter;
    }

    @Test
    public void shouldReduceFilterToWantedFields() {
        searchFilterUtil = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class)))
                .thenReturn(Sets.newHashSet("C", "D"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any())).thenReturn(Sets.newHashSet("B", "C", "D"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(true);

        SearchFilter copySearchFilter = searchFilterUtil.selectFiltersBasedOnFieldName(createSearchFilter(), Sets.newHashSet("B", "D"));

        Assert.assertTrue(copySearchFilter instanceof OrFilter);
        OrFilter orFilter = (OrFilter)copySearchFilter;
        Assert.assertTrue(orFilter.getFilter1() instanceof FieldFilter);
        Assert.assertEquals("B", ((FieldFilter)orFilter.getFilter1()).getFieldName());
        Assert.assertTrue(orFilter.getFilter2() instanceof NotFilter);
        NotFilter notFilter = (NotFilter)orFilter.getFilter2();
        Assert.assertTrue(notFilter.getFilter() instanceof FieldFilter);
        Assert.assertEquals("D", ((FieldFilter)notFilter.getFilter()).getFieldName());
    }


}
