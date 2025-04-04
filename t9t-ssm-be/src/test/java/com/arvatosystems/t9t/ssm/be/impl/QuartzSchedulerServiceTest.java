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
package com.arvatosystems.t9t.ssm.be.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.quartz.Scheduler;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.ITimeZoneProvider;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.services.ICannedRequestResolver;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType;
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceWeekdayTypeEnum;
import com.arvatosystems.t9t.ssm.SchedulerWeekDaysEnumSet;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;

public class QuartzSchedulerServiceTest {
    protected QuartzSchedulerService service;

    protected Scheduler scheduler;
    protected Provider<RequestContext> ctxProvider;
    protected RequestContext requestContext;
    protected ICannedRequestResolver rqResolver;
    protected ITimeZoneProvider timeZoneProvider;

    @BeforeEach
    public void setup() {
        scheduler = Mockito.mock(Scheduler.class);
        ctxProvider = Mockito.mock(Provider.class);
        requestContext = Mockito.mock(RequestContext.class);
        rqResolver = Mockito.mock(ICannedRequestResolver.class);
        timeZoneProvider = Mockito.mock(ITimeZoneProvider.class);

        Jdp.bindInstanceTo(scheduler, Scheduler.class);
        Jdp.bindInstanceTo(ctxProvider, Provider.class);
        Jdp.bindInstanceTo(requestContext, RequestContext.class);
        Jdp.bindInstanceTo(rqResolver, ICannedRequestResolver.class);
        Jdp.bindInstanceTo(timeZoneProvider, ITimeZoneProvider.class);

        service = new QuartzSchedulerService();

        Mockito.doReturn(requestContext).when(ctxProvider).get();
    }

    @Test
    public void testSecondlyWithoutParam() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.SECONDLY);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* * * ? * *", cronExpression);
    }

    @Test
    public void testSecondlyWithInterval() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.SECONDLY);
        setup.setIntervalMinutes(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0/3 * * ? * *", cronExpression);
    }

    @Test
    public void testSecondlyWithIntervalAndOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.SECONDLY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("3/10 * * ? * *", cronExpression);
    }

    @Test
    public void testSecondlyWithIntervalAndOffsetAndHourSet() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.SECONDLY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);
        setup.setStartHour(LocalTime.of(12, 12));
        setup.setEndHour(LocalTime.of(15, 12));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("3/10 * 12-15 ? * *", cronExpression);
    }

    @Test
    public void testMinutelyWithNoParam() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MINUTELY);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 * * ? * *", cronExpression);
    }

    @Test
    public void testMinutelyWithInterval() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MINUTELY);
        setup.setIntervalMinutes(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 0/3 * ? * *", cronExpression);
    }

    @Test
    public void testMinutelyWithIntervalAndOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MINUTELY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 3/10 * ? * *", cronExpression);
    }

    @Test
    public void testMinutelyWithIntervalAndOffsetAndHour() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MINUTELY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);
        setup.setStartHour(LocalTime.of(12, 12));
        setup.setEndHour(LocalTime.of(15, 12));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 3/10 12-15 ? * *", cronExpression);
    }

    @Test
    public void testMinutelySmallerUnitOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MINUTELY);
        setup.setIntervalMinutes(1);
        setup.setIntervalOffset(3);
        setup.setStartHour(LocalTime.of(12, 12));
        setup.setEndHour(LocalTime.of(15, 12));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("3 * 12-15 ? * *", cronExpression);
    }

    @Test
    public void testHourly() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 0 * ? * *", cronExpression);
    }

    @Test
    public void testHourlyWithInterval() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);
        setup.setIntervalMinutes(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 0 0/3 ? * *", cronExpression);
    }

    @Test
    public void testHourlyWithIntervalAndWrongOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);
        setup.setIntervalMinutes(3);
        setup.setIntervalOffset(10);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 0 0/3 ? * *", cronExpression);
    }

    @Test
    public void testHourlyWithIntervalAndOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 0 3/10 ? * *", cronExpression);
    }

    @Test
    public void testHourlyWithSmallerUnitOffset() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);
        setup.setIntervalMinutes(1);
        setup.setIntervalOffset(10);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 10 * ? * *", cronExpression);
    }

    @Test
    public void testDailyWithOnlyRequiredParameter() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.DAILY);
        setup.setExecutionTime(LocalTime.of(12, 30));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 * * ?", cronExpression);
    }

    @Test
    public void testDailyWithStartHourAndEndHour() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.DAILY);
            setup.setStartHour(LocalTime.now());
            setup.setEndHour(LocalTime.now());
            setup.setExecutionTime(LocalTime.of(12, 30));

            service.determineCronExpression(setup);
        });
    }

    @Test
    public void testMonthly() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.MONTHLY);

            service.determineCronExpression(setup);
        });
    }

    @Test
    public void testMonthlyWithRequiredParam() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MONTHLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setValidFrom(LocalDateTime.of(2018, 1, 10, 0, 0));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 10 * ?", cronExpression);
    }

    @Test
    public void testMonthlyWithIntervalAndExecutionTime() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.MONTHLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setIntervalMinutes(10);
        setup.setValidFrom(LocalDateTime.of(2018, 1, 10, 0, 0));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 10 1/10 ?", cronExpression);
    }

    @Test
    public void testYearly() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.YEARLY);
            setup.setValidFrom(LocalDateTime.of(2018, 1, 10, 0, 0));

            service.determineCronExpression(setup);
        });
    }

    @Test
    public void testYearlyWithRequiredParam() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.YEARLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setValidFrom(LocalDateTime.of(2018, 1, 10, 0, 0));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 10 1 ? *", cronExpression);
    }

    @Test
    public void testYearlyWithValidTo() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.YEARLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setValidFrom(LocalDateTime.of(2018, 1, 10, 0, 0));
        setup.setValidTo(LocalDateTime.of(2030, 1, 10, 0, 0));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 10 1 ? 2018-2030", cronExpression);
    }

    @Test
    public void testWeeklyWithSetOfWeekdaysParams() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.WEEKLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setSetOfWeekdays(SchedulerWeekDaysEnumSet.ofTokens(SchedulerSetupRecurrenceWeekdayTypeEnum.MONDAY,
          SchedulerSetupRecurrenceWeekdayTypeEnum.WEDNESDAY));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 ? * 2,4", cronExpression);
    }

    @Test
    public void testWeeklyWithBothSetOfWeekdaysAndIntervalParams() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.WEEKLY);
            setup.setExecutionTime(LocalTime.of(12, 30));
            setup.setSetOfWeekdays(SchedulerWeekDaysEnumSet.ofTokens(SchedulerSetupRecurrenceWeekdayTypeEnum.MONDAY));
            setup.setIntervalMinutes(10);

            service.determineCronExpression(setup);
        });
    }

    @Test
    public void testWeeklyWithIntervalParams() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.WEEKLY);
        setup.setExecutionTime(LocalTime.of(12, 30));
        setup.setIntervalMinutes(3);

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 1/3 * ?", cronExpression);
    }

    @Test
    public void testDailyWithInterval() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.DAILY);
        setup.setIntervalMinutes(10);
        setup.setIntervalOffset(3);
        setup.setExecutionTime(LocalTime.of(12, 30));

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("0 30 12 1/10 * ?", cronExpression);
    }

    @Test
    public void testNativeCronAllAsterisk() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
        setup.setCronExpression("* * * ? * * *");

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* * * ? * * *", cronExpression);
    }


    @Test
    public void testNativeCRONExpression() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
        setup.setCronExpression("* 0-59/3 10/3 ? * *");

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* 0-59/3 10/3 ? * *", cronExpression);
    }

    @Test
    public void testNativeCRONExpression01() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
        setup.setCronExpression("* 0-59/3 10/3 * * ? 2017");

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* 0-59/3 10/3 * * ? 2017", cronExpression);
    }

    @Test
    public void testNativeCRONExpression02() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
        setup.setCronExpression("* 0-59/3 10/3 * * ? 2017-2020");

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* 0-59/3 10/3 * * ? 2017-2020", cronExpression);
    }

    @Test
    public void testNativeCRONExpression03() {
        SchedulerSetupDTO setup = new SchedulerSetupDTO();
        setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
        setup.setCronExpression("* 0-59/3 10/3 * * ? 2017,2018-2020");

        String cronExpression = service.determineCronExpression(setup);
        Assertions.assertEquals("* 0-59/3 10/3 * * ? 2017,2018-2020", cronExpression);
    }

    @Test
    public void testNativeCRONGibberishValue() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.CRON_NATIVE);
            setup.setCronExpression("some wrong value 9 * * ?");

            service.determineCronExpression(setup);
        });
    }

    @Test
    public void testExceptionIntervalRange() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.HOURLY);
            setup.setIntervalMinutes(300);
            setup.setIntervalOffset(1000);

            String cronExpression = service.determineCronExpression(setup);
            Assertions.assertEquals("0 0 10/3 ? * *", cronExpression);
        });
    }

    @Test
    public void testExceptionSettingSmallerOffsetOnSecondly() {
        Assertions.assertThrows(T9tException.class, () -> {
            SchedulerSetupDTO setup = new SchedulerSetupDTO();
            setup.setRecurrencyType(SchedulerSetupRecurrenceType.SECONDLY);
            setup.setIntervalMinutes(1);
            setup.setIntervalOffset(10);
            service.determineCronExpression(setup);
        });
    }
}
