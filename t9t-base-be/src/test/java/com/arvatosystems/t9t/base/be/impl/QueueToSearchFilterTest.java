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

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.arvatosystems.t9t.base.services.ISearchTools;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.dp.Jdp;

public class QueueToSearchFilterTest {
    SearchFilterUtil util;
    private ISearchTools searchTools = Mockito.mock(ISearchTools.class);

    @Before
    public void setupMocks() {
        Jdp.bindInstanceTo(searchTools, ISearchTools.class);
    }

    @Test
    public void shouldTranslateToSearchFilter() {
        util = new SearchFilterUtil();

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        filterQueue.add(new AndFilter());
        filterQueue.add(new AsciiFilter("A"));
        filterQueue.add(new NotFilter());
        filterQueue.add(new AsciiFilter("C"));


        SearchFilter filter = util.generateSearchFilterFromQueue(filterQueue, null);
        Assert.assertTrue(filter instanceof AndFilter);
        Assert.assertTrue(((AndFilter)filter).getFilter1() instanceof AsciiFilter);
        Assert.assertTrue(((AndFilter)filter).getFilter2() instanceof NotFilter);
    }

    @Test
    public void shouldTranslateToSearchFilter02() {
        util = new SearchFilterUtil();

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        filterQueue.add(new NotFilter());
        filterQueue.add(new AndFilter());
        filterQueue.add(new OrFilter());
        filterQueue.add(new AsciiFilter("A"));
        filterQueue.add(new AsciiFilter("B"));
        filterQueue.add(new AsciiFilter("C"));


        SearchFilter filter = util.generateSearchFilterFromQueue(filterQueue, null);
        Assert.assertTrue(filter instanceof NotFilter);
        Assert.assertTrue(((NotFilter)filter).getFilter() instanceof AndFilter);

        AndFilter andFilter = (AndFilter)((NotFilter)filter).getFilter();
        Assert.assertTrue(andFilter.getFilter1() instanceof OrFilter);
        Assert.assertTrue(andFilter.getFilter2() instanceof AsciiFilter);
        Assert.assertEquals("C", ((AsciiFilter)andFilter.getFilter2()).getFieldName());
    }


}
