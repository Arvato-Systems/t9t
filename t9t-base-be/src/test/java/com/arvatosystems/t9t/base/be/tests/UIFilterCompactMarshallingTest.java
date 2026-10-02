/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.base.be.tests;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayComposer;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.util.ByteArray;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.ui.UIFilter;
import com.arvatosystems.t9t.base.ui.UIFilterType;

public class UIFilterCompactMarshallingTest {

    @BeforeAll
    static void initializeParsers() {
        MessagingUtil.initializeBonaparteParsers();
    }

    @Test
    public void shouldRoundTripStringPresetValues() throws Exception {
        final UIFilter filter = baseFilter("stringField", UIFilterType.LIKE);
        filter.setCaseInsensitive(Boolean.TRUE);
        filter.setQualifier("contains");
        filter.setEqualsValue("exact");
        filter.setLowerBound("alpha");
        filter.setUpperBound("omega");
        filter.setLikeValue("%needle%");
        filter.setValueList(List.of("A", "B", "C"));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getFieldName(), parsed.getFieldName());
        Assertions.assertEquals(filter.getFilterType(), parsed.getFilterType());
        Assertions.assertEquals(filter.getQualifier(), parsed.getQualifier());
        Assertions.assertEquals(filter.getCaseInsensitive(), parsed.getCaseInsensitive());
        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getLikeValue(), parsed.getLikeValue());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripIntPresetValues() throws Exception {
        final UIFilter filter = baseFilter("intField", UIFilterType.RANGE);
        filter.setEqualsValue(Integer.valueOf(42));
        filter.setLowerBound(Integer.valueOf(-7));
        filter.setUpperBound(Integer.valueOf(123));
        filter.setValueList(List.of(4, 8, 15, 16, 23, 42));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripDoublePresetValues() throws Exception {
        final UIFilter filter = baseFilter("doubleField", UIFilterType.RANGE);
        filter.setEqualsValue(Double.valueOf(42.5d));
        filter.setLowerBound(Double.valueOf(-7.125d));
        filter.setUpperBound(Double.valueOf(123.875d));
        filter.setValueList(List.of(1.25d, 2.5d, 5.0d));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripUuidPresetValues() throws Exception {
        final UIFilter filter = baseFilter("uuidField", UIFilterType.IN);
        final UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        final UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        final UUID uuid3 = UUID.fromString("33333333-3333-3333-3333-333333333333");
        filter.setEqualsValue(uuid1);
        filter.setLowerBound(uuid2);
        filter.setUpperBound(uuid3);
        filter.setValueList(List.of(uuid1, uuid2, uuid3));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripDayPresetValues() throws Exception {
        final UIFilter filter = baseFilter("dayField", UIFilterType.RANGE);
        final LocalDate start = LocalDate.of(2024, 2, 29);
        final LocalDate end = LocalDate.of(2024, 12, 31);
        filter.setEqualsValue(start);
        filter.setLowerBound(start.minusDays(10));
        filter.setUpperBound(end);
        filter.setValueList(List.of(start, end));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripTimePresetValues() throws Exception {
        final UIFilter filter = baseFilter("timeField", UIFilterType.RANGE);
        final LocalTime start = LocalTime.of(8, 15, 30, 123_000_000);
        final LocalTime end = LocalTime.of(17, 45, 59, 987_000_000);
        filter.setEqualsValue(start);
        filter.setLowerBound(start.minusHours(1));
        filter.setUpperBound(end);
        filter.setValueList(List.of(start, end));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripTimestampPresetValues() throws Exception {
        final UIFilter filter = baseFilter("timestampField", UIFilterType.RANGE);
        final LocalDateTime start = LocalDateTime.of(2024, 2, 29, 8, 15, 30, 123_000_000);
        final LocalDateTime end = LocalDateTime.of(2024, 12, 31, 17, 45, 59, 987_000_000);
        filter.setEqualsValue(start);
        filter.setLowerBound(start.minusDays(1));
        filter.setUpperBound(end);
        filter.setValueList(List.of(start, end));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    @Test
    public void shouldRoundTripPartiallyPopulatedTimestampPresetValues() throws Exception {
        final UIFilter filter = baseFilter("timestampField", UIFilterType.UPPER_BOUND);
        final LocalDateTime end = LocalDateTime.of(2024, 12, 31, 17, 45, 59, 987_000_000);
        filter.setUpperBound(end);

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertNull(parsed.getEqualsValue());
        Assertions.assertNull(parsed.getLowerBound());
        Assertions.assertEquals(end, parsed.getUpperBound());
        Assertions.assertNull(parsed.getValueList());
    }

    @Test
    public void shouldRoundTripInstantPresetValues() throws Exception {
        final UIFilter filter = baseFilter("instantField", UIFilterType.RANGE);
        final Instant start = Instant.parse("2024-02-29T08:15:30.123Z");
        final Instant end = Instant.parse("2024-12-31T17:45:59.987Z");
        filter.setEqualsValue(start);
        filter.setLowerBound(start.minusSeconds(3600));
        filter.setUpperBound(end);
        filter.setValueList(List.of(start, end));

        final UIFilter parsed = roundTrip(filter);

        Assertions.assertEquals(filter.getEqualsValue(), parsed.getEqualsValue());
        Assertions.assertEquals(filter.getLowerBound(), parsed.getLowerBound());
        Assertions.assertEquals(filter.getUpperBound(), parsed.getUpperBound());
        Assertions.assertEquals(filter.getValueList(), parsed.getValueList());
    }

    private static UIFilter baseFilter(final String fieldName, final UIFilterType filterType) {
        final UIFilter filter = new UIFilter();
        filter.setFieldName(fieldName);
        filter.setFilterType(filterType);
        return filter;
    }

    private static UIFilter roundTrip(final UIFilter filter) throws Exception {
        final ByteArray serialized = CompactByteArrayComposer.marshalAsByteArray(StaticMeta.OUTER_BONAPORTABLE, filter);
        final BonaPortable parsed = new CompactByteArrayParser(serialized.getBytes(), 0, -1).readObject(StaticMeta.OUTER_BONAPORTABLE, BonaPortable.class);
        return Assertions.assertInstanceOf(UIFilter.class, parsed);
    }
}
