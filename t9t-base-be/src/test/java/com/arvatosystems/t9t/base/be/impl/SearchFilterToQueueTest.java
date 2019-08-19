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

import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

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

public class SearchFilterToQueueTest {
    SearchFilterUtil util;

    private ISearchTools searchTools = Mockito.mock(ISearchTools.class);

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

    private SearchFilter createSearchFilter02() {
        NotFilter notFilter = new NotFilter();
        AndFilter andFilter = new AndFilter();
        notFilter.setFilter(andFilter);

        andFilter.setFilter2(new AsciiFilter("A"));
        andFilter.setFilter1(new OrFilter());

        ((OrFilter)andFilter.getFilter1()).setFilter1(new AsciiFilter("B"));
        ((OrFilter)andFilter.getFilter1()).setFilter2(new LongFilter("C"));


        return notFilter;
    }

    @Before
    public void setupMocks() {
        Jdp.bindInstanceTo(searchTools, ISearchTools.class);
    }

    @Test
    public void shouldCreateQueueWithA() {
        util = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(SearchFilter.class), Mockito.any())).thenReturn(Sets.newHashSet("A", "B", "C", "D"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(false);

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter(), Collections.singleton("B"), filterQueue);

        Assert.assertEquals(1, filterQueue.size());

        SearchFilter filter1 = filterQueue.poll();
        Assert.assertTrue(filter1 instanceof FieldFilter);
        Assert.assertEquals("B", ((FieldFilter)filter1).getFieldName());
    }

    @Test
    public void shouldCreateQueueWithAndAB() {
        util = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("A", "B", "C", "D"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("B", "C", "D"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(false);

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter(), Sets.newHashSet("A", "B"), filterQueue);

        Assert.assertEquals(3, filterQueue.size());

        //1 expected AndFilter
        Assert.assertTrue(filterQueue.poll() instanceof AndFilter);

        //2 and 3 expected FieldFilters
        SearchFilter filter2 = filterQueue.poll();
        Assert.assertTrue(filter2 instanceof FieldFilter);
        Assert.assertEquals("A", ((FieldFilter)filter2).getFieldName());

        SearchFilter filter3 = filterQueue.poll();
        Assert.assertTrue(filter3 instanceof FieldFilter);
        Assert.assertEquals("B", ((FieldFilter)filter3).getFieldName());
    }

    @Test
    public void shouldCreateQueueWithABC() {
        util = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class)))
                .thenReturn(Sets.newHashSet("A", "B", "C", "D"), Sets.newHashSet("C", "D"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("B", "C", "D"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(true);

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter(), Sets.newHashSet("A", "B", "C"), filterQueue);

        Assert.assertEquals(6, filterQueue.size());
    }

    @Test
    public void shouldCreateQueueWithBC() {
        util = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class)))
                .thenReturn(Sets.newHashSet("C", "D"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("B", "C"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(true);

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter(), Sets.newHashSet("B", "C"), filterQueue);

        Assert.assertEquals(4, filterQueue.size());
        Assert.assertTrue(filterQueue.poll() instanceof OrFilter);
        Assert.assertTrue(filterQueue.poll() instanceof FieldFilter);
        Assert.assertTrue(filterQueue.poll() instanceof NotFilter);
        Assert.assertTrue(filterQueue.poll() instanceof FieldFilter);
    }

    @Test
    public void shouldCreateQueueWithAD() {
        util = new SearchFilterUtil();

        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class)))
                .thenReturn(Sets.newHashSet("A", "B", "C", "D"), Sets.newHashSet("C", "D"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("B", "C", "D"));
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(true);

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter(), Sets.newHashSet("A", "D"), filterQueue);

        Assert.assertEquals(4, filterQueue.size());
        Assert.assertTrue(filterQueue.poll() instanceof AndFilter);
        Assert.assertTrue(filterQueue.poll() instanceof FieldFilter); // A
        Assert.assertTrue(filterQueue.poll() instanceof NotFilter);
        Assert.assertTrue(filterQueue.poll() instanceof FieldFilter); // D
    }

    @Test
    public void shouldCreateQueue() {
        util = new SearchFilterUtil();
        Mockito.when(searchTools.containsFieldPathElements(Mockito.any(NotFilter.class), Mockito.any())).thenReturn(true);
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(AndFilter.class), Mockito.any(Set.class)))
            .thenReturn(Sets.newHashSet("A", "B", "C"));
        Mockito.when(searchTools.getAllSearchFilterFieldName(Mockito.any(OrFilter.class), Mockito.any(Set.class))).thenReturn(Sets.newHashSet("B", "C"));

        Queue<SearchFilter> filterQueue = new LinkedList<>();
        util.populateQueue(createSearchFilter02(), Sets.newHashSet("A", "B", "C"), filterQueue);

        Assert.assertEquals(6, filterQueue.size());
    }

}
