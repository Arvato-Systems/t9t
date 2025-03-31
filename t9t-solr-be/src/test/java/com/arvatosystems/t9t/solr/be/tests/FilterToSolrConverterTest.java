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
package com.arvatosystems.t9t.solr.be.tests;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.search.XenumFilter;
import com.arvatosystems.t9t.base.services.IEnumResolver;
import com.arvatosystems.t9t.solr.be.impl.FilterToSolrConverter;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.DayFilter;
import de.jpaw.bonaparte.pojos.api.DecimalFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.NullFilter;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.dp.Jdp;

public class FilterToSolrConverterTest {

    private FilterToSolrConverter converter;
    private IEnumResolver resolver;

    @BeforeEach
    public void setup() {
        resolver = Mockito.mock(IEnumResolver.class);
        Jdp.bindInstanceTo(resolver, IEnumResolver.class);

        Mockito.when(resolver.getTokenByPqonAndInstance(Mockito.any(), Mockito.eq("A"))).thenReturn("ANANAS");
        Mockito.when(resolver.getTokenByPqonAndInstance(Mockito.any(), Mockito.eq("B"))).thenReturn("BANANA");
        Mockito.when(resolver.getTokenByPqonAndInstance(Mockito.any(), Mockito.eq("C"))).thenReturn("CITRON");

        Mockito.when(resolver.getTokenByXEnumPqonAndInstance(Mockito.any(), Mockito.eq("D"))).thenReturn("DURIAN");
        Mockito.when(resolver.getTokenByXEnumPqonAndInstance(Mockito.any(), Mockito.eq("E"))).thenReturn("EGGPLANT");
        Mockito.when(resolver.getTokenByXEnumPqonAndInstance(Mockito.any(), Mockito.eq("F"))).thenReturn("FIG");

        converter = new FilterToSolrConverter();
    }

    @Test
    public void toSolrByAsciiFilterTest() throws Exception {
        AsciiFilter filter = new AsciiFilter("textField");

        List<String> valueList = new ArrayList<String>();
        valueList.add("A");
        valueList.add("B");
        valueList.add("C");
        filter.setValueList(valueList);

        Assertions.assertEquals("(A OR B OR C)", converter.toSolr(filter));
    }

    @Test
    public void toSolrDecimalFilterTest() throws Exception {
        DecimalFilter filter = new DecimalFilter("decField");

        List<BigDecimal> valueList = new ArrayList<BigDecimal>();
        valueList.add(new BigDecimal(5.0).setScale(2, RoundingMode.HALF_EVEN));
        valueList.add(new BigDecimal(15.001).setScale(3, RoundingMode.HALF_EVEN));
        valueList.add(new BigDecimal(2.2002).setScale(6, RoundingMode.HALF_EVEN));
        filter.setValueList(valueList);

        Assertions.assertEquals("(5.00 OR 15.001 OR 2.200200)", converter.toSolr(filter));
    }

    @Test
    public void toSolrDecimalFilterRangeTest() throws Exception {
        DecimalFilter filter = new DecimalFilter("decField");
        filter.setLowerBound(new BigDecimal(5.0).setScale(2, RoundingMode.HALF_EVEN));
        filter.setUpperBound(new BigDecimal(2.2002).setScale(6, RoundingMode.HALF_EVEN));

        Assertions.assertEquals("[5.00 TO 2.200200]", converter.toSolr(filter));
    }

    @Test
    public void toSolrConditionTest() throws Exception {
        AsciiFilter filter = new AsciiFilter("asciiField");

        List<String> valueList = new ArrayList<String>();
        valueList.add("A");
        valueList.add("B");
        valueList.add("C");
        filter.setValueList(valueList);

        AndFilter andFilter = new AndFilter(filter, filter);
        Assertions.assertEquals("(+asciiField:(A OR B OR C) AND +asciiField:(A OR B OR C))", converter.toSolrCondition(andFilter));

        OrFilter orFilter = new OrFilter(andFilter, filter);
        Assertions.assertEquals("((+asciiField:(A OR B OR C) AND +asciiField:(A OR B OR C)) OR +asciiField:(A OR B OR C))",
          converter.toSolrCondition(orFilter));

        NotFilter notFilter = new NotFilter(filter);
        Assertions.assertEquals("NOT (+asciiField:(A OR B OR C))", converter.toSolrCondition(notFilter));

        NullFilter nullFilter = new NullFilter("nullField");
        Assertions.assertEquals("-nullField:[* TO *]", converter.toSolrCondition(nullFilter));

        NotFilter notNullFilter = new NotFilter(nullFilter);
        Assertions.assertEquals("nullField:[* TO *]", converter.toSolrCondition(notNullFilter));

        NotFilter notNotFilter = new NotFilter(new NotFilter((filter)));
        Assertions.assertEquals("+asciiField:(A OR B OR C)", converter.toSolrCondition(notNotFilter), "Double NOT should be eaten");

    }

    @Test
    public void toSolrByDayFilterTest() throws Exception {
        DayFilter filter = new DayFilter();

        List<LocalDate> valueList = new ArrayList<LocalDate>();
        valueList.add(LocalDate.of(1985, 10, 19));
        valueList.add(LocalDate.of(1988, 7, 20));
        valueList.add(LocalDate.of(2003, 3, 22));
        filter.setValueList(valueList);

        Assertions.assertEquals("(1985-10-19T00\\\\:00\\\\:00Z OR 1988-07-20T00\\\\:00\\\\:00Z OR 2003-03-22T00\\\\:00\\\\:00Z)",
                converter.toSolr(filter));
    }

    @Test
    public void toSolrByDayFilterRangeTest() throws Exception {
        DayFilter filter = new DayFilter();

        List<LocalDate> valueList = new ArrayList<LocalDate>();
        valueList.add(LocalDate.of(2003, 3, 22));
        filter.setLowerBound(LocalDate.of(1985, 10, 19));
        filter.setUpperBound(LocalDate.of(1988, 7, 20));

        Assertions.assertEquals("[1985-10-19T00\\\\:00\\\\:00Z TO 1988-07-20T00\\\\:00\\\\:00Z]",
                converter.toSolr(filter));
    }

    @Test
    public void toSolrByEnumFilterTest() throws Exception {

        EnumFilter filter = new EnumFilter();
        filter.setEnumPqon("ENUM");

        List<String> valueList = new ArrayList<String>();
        valueList.add("A");
        valueList.add("B");
        valueList.add("C");
        filter.setNameList(valueList);

        Assertions.assertEquals("(ANANAS OR BANANA OR CITRON)", converter.toSolr(filter));
    }

    @Test
    public void toSolrByEnumFilterTokenListTest() throws Exception {

        EnumFilter filter = new EnumFilter();
        filter.setEnumPqon("ENUM");

        List<String> valueList = new ArrayList<String>();
        valueList.add("A");
        valueList.add("B");
        valueList.add("C");
        filter.setTokenList(valueList);

        Assertions.assertEquals("(A OR B OR C)", converter.toSolr(filter));
    }

    @Test
    public void toSolrByXenumFilterTest() throws Exception {

        XenumFilter filter = new XenumFilter();
        filter.setXenumPqon("XENUM");

        List<String> valueList = new ArrayList<String>();
        valueList.add("F");
        valueList.add("E");
        valueList.add("D");
        filter.setNameList(valueList);

        Assertions.assertEquals("(FIG OR EGGPLANT OR DURIAN)", converter.toSolr(filter));
    }
}
