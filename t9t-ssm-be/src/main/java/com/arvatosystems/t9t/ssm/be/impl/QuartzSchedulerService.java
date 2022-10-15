/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.CannedRequestDTO;
import com.arvatosystems.t9t.core.CannedRequestRef;
import com.arvatosystems.t9t.core.services.ICannedRequestResolver;
import com.arvatosystems.t9t.ssm.SchedulerConcurrencyType;
import com.arvatosystems.t9t.ssm.SchedulerSetupDTO;
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceType;
import com.arvatosystems.t9t.ssm.SchedulerSetupRecurrenceWeekdayTypeEnum;
import com.arvatosystems.t9t.ssm.T9tSsmException;
import com.arvatosystems.t9t.ssm.services.ISchedulerService;

import de.jpaw.bonaparte.core.StaticMeta;
import de.jpaw.bonaparte.core.StringBuilderComposer;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

@Singleton
public class QuartzSchedulerService implements ISchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(QuartzSchedulerService.class);

    public static final String DM_SETUP_REF  = "setupRef";      // the reference to the stored request setup, for auditing purposes
    public static final String DM_TENANT_ID  = "tenantId";     // the reference to the tenant (implicitly defined via API-Key, but required upfront)
    public static final String DM_API_KEY    = "apiKey";        // the API key which defines the tenant and user ID to run the request under
    public static final String DM_LANGUAGE   = "language";      // the desired language
    public static final String DM_REQUEST    = "request";       // a reference to the serialized request stored centrally
    public static final String DM_CONC_TYPE  = "concType";      // the concurrency type
    public static final String DM_CONC_TYPE2 = "concTypeStale"; // the concurrency type for old instances
    public static final String DM_TIME_LIMIT = "timeLimit";     // after how many minutes a process is regarded as "old"
    public static final String DM_RUN_ON_NODE = "runOnNode";    // on which node to run this task, or null for any node
    public static final Integer RUN_ON_ALL_NODES = Integer.valueOf(-1);
    public static final Integer NO_TIME_LIMIT    = Integer.valueOf(0);

    protected final ICannedRequestResolver rqResolver = Jdp.getRequired(ICannedRequestResolver.class);
    protected final Scheduler scheduler = Jdp.getRequired(Scheduler.class);

    private static final List<SchedulerSetupRecurrenceType> DAILY_OR_LESS_FREQ = Arrays.asList(SchedulerSetupRecurrenceType.DAILY,
            SchedulerSetupRecurrenceType.WEEKLY, SchedulerSetupRecurrenceType.MONTHLY, SchedulerSetupRecurrenceType.YEARLY);

    private static final String CRON_REGEX_PATTERN
      = "(\\*|\\d{0,2}([-,]\\d{0,2})*(/\\d{0,2})?)\\s(\\*|\\d{0,2}([-,]\\d{0,2})*"
      + "(/\\d{0,2})?)\\s(\\*|\\d{0,2}([-,]\\d{0,2})*"
      + "(/\\d{0,2})?)\\s(\\*|\\d{0,2}([-,]\\d{0,2})*"
      + "(/\\d{0,2})?|\\?)\\s(\\*|\\d{0,2}([-,]\\d{0,2})*"
      + "(/\\d{0,2}))\\s(\\*|\\d{0,2}([-,]\\d{0,2})*"
      + "(/\\d{0,2})?|\\?)(\\s(\\*|\\d{4}([-,]\\d{4})*))?";

    /** returns the token (null safe) */
    private String getToken(final SchedulerConcurrencyType t) {
        return t == null ? null : t.getToken();
    }

    @Override
    public void createScheduledJob(final RequestContext ctx, final SchedulerSetupDTO setup) {
        final CannedRequestRef rqRef = setup.getRequest();
        final CannedRequestDTO requestDTO = rqRef instanceof CannedRequestDTO ? (CannedRequestDTO)rqRef : rqResolver.getDTO(rqRef);

        // convert the request invocation into a String
        final String serializedRequest = StringBuilderComposer.marshal(StaticMeta.OUTER_BONAPORTABLE, requestDTO.getRequest());

        try {
            LOGGER.info("Creating scheduled job {}.{}, triggerDescription={}.", ctx.tenantId, setup.getSchedulerId(), setup);

            final JobDetail jobDetail = JobBuilder.newJob(PerformScheduledJob.class).withIdentity(setup.getSchedulerId(), ctx.tenantId).build();
            final JobDataMap m = jobDetail.getJobDataMap();
            m.put(DM_TENANT_ID,  ctx.tenantId);
            m.put(DM_SETUP_REF,  setup.getObjectRef());
            m.put(DM_API_KEY,    setup.getApiKey().toString());
            m.put(DM_LANGUAGE,   setup.getLanguageCode());
            m.put(DM_REQUEST,    serializedRequest);
            m.put(DM_CONC_TYPE,  getToken(setup.getConcurrencyType()));
            // m.put(DM_CONC_TYPE2, getToken(setup.getConcurrencyTypeStale())); // not needed, if old jobs exist, a request handler will be invoked.
            m.put(DM_TIME_LIMIT, setup.getTimeLimit() == null ? NO_TIME_LIMIT    : setup.getTimeLimit());
            m.put(DM_TIME_LIMIT, setup.getRunOnNode() == null ? RUN_ON_ALL_NODES : setup.getRunOnNode());
            final Trigger trigger = getTrigger(ctx, setup);
            scheduler.scheduleJob(jobDetail, trigger);

            LOGGER.info("Scheduled job {}.{}. Next execution will be at: {}", ctx.tenantId, setup.getSchedulerId(), trigger.getFireTimeAfter(null));
        } catch (final Exception underlyingException) {
            final String message = "Failed to create job with description=" + setup.toString();
            LOGGER.error(message + " Caught exception: ", underlyingException);
            throw new T9tException(T9tSsmException.SCHEDULER_CREATE_JOB_EXCEPTION, message, underlyingException);
        }
    }

    @Override
    public void recreateScheduledJob(final RequestContext ctx, final SchedulerSetupDTO setup) {
        removeScheduledJob(ctx, setup.getSchedulerId());
        createScheduledJob(ctx, setup);
    }

    @Override
    public void updateScheduledJob(final RequestContext ctx, final SchedulerSetupDTO setup) {
        try {
            LOGGER.info("Updating scheduled job {}.{} to triggerDescription={}.", ctx.tenantId, setup.getSchedulerId(), setup);

            final Trigger trigger = getTrigger(ctx, setup);
            final Date date = scheduler.rescheduleJob(trigger.getKey(), trigger);
            if (date == null) {
                LOGGER.error("Tried to reschedule job {}.{}, but job was not found! Trying to create a new job instead.",
                  ctx.tenantId, setup.getSchedulerId());
                createScheduledJob(ctx, setup);
            } else {
                LOGGER.info("Rescheduled job {}.{}. Next execution will be at: {}", ctx.tenantId, setup.getSchedulerId(), trigger.getFireTimeAfter(null));
            }
        } catch (SchedulerException | ParseException underlyingException) {
            final String message = "Failed to update scheduled job with description=" + setup.toString();
            LOGGER.error(message + " Caught exception: ", underlyingException);
            throw new T9tException(T9tSsmException.SCHEDULER_UPDATE_JOB_EXCEPTION, message, underlyingException);
        }
    }


    @Override
    public void removeScheduledJob(final RequestContext ctx, final String schedulerId) {
        try {
            LOGGER.info("Removing scheduled job {}.{}.", ctx.tenantId, schedulerId);
            final JobKey jobKey = new JobKey(schedulerId, ctx.tenantId);
            final boolean result = scheduler.deleteJob(jobKey);
            if (!result) {
                LOGGER.error("Tried to remove scheduled job {}.{}, but job was not found!", ctx.tenantId, schedulerId);
            } else {
                LOGGER.info("Deleted job {}.{}.", ctx.tenantId, schedulerId);
            }
        } catch (final Exception underlyingException) {
            final String message = ctx.tenantId + "." + schedulerId;
            LOGGER.error("Failed to delete scheduled job {}", message, underlyingException);
            throw new T9tException(T9tSsmException.SCHEDULER_UPDATE_JOB_EXCEPTION, message, underlyingException);
        }
    }


    protected Trigger getTrigger(final RequestContext ctx, final SchedulerSetupDTO setup) throws ParseException {
        // Do the common work here (identity + start + end)
        final TriggerBuilder<Trigger> builder = TriggerBuilder.newTrigger().withIdentity(setup.getSchedulerId(), ctx.tenantId);
        if (setup.getValidFrom() != null) {
            final Date startAt = java.sql.Timestamp.valueOf(setup.getValidFrom());
            final Date now = new Date();
            if (startAt.compareTo(now) > 0) {
                builder.startAt(startAt);
            } else {
                LOGGER.warn("Passed startTime={} was in the past. Parameter will be ignored.", startAt);
            }
        }
        addSchedule(builder, setup);
        if (setup.getValidTo() != null) {
            builder.endAt(java.sql.Timestamp.valueOf(setup.getValidTo()));
        }
        return builder.build();
    }
    /**
     * This method creates a trigger description object and fills it with start date, end date provided by passed scheduler setup DTO.
     *
     * @param setup
     *            see method description for details.
     */
    // override this in order to add additional trigger types.
    protected void addSchedule(final TriggerBuilder<Trigger> builder, final SchedulerSetupDTO setup) throws ParseException {
        // Handle the different trigger cases

        if (setup.getRecurrencyType() == SchedulerSetupRecurrenceType.FAST) {
            builder.withSchedule(
                    SimpleScheduleBuilder.simpleSchedule()
                    .withRepeatCount(setup.getRepeatCount() == null ? 1 : setup.getRepeatCount())
                    .withIntervalInMilliseconds(setup.getIntervalMilliseconds() == null ? 1000 : setup.getIntervalMilliseconds())
            );
        } else {
            builder.withSchedule(
                    CronScheduleBuilder.cronScheduleNonvalidatedExpression(setup.getCronExpression())
            );
        }
    }

    private StringBuilder getHourlyOrMoreOftenHour(final SchedulerSetupDTO setup) {
        final StringBuilder hour = new StringBuilder();

        if (setup.getStartHour() == null && setup.getEndHour() == null) {
            return hour.append("*");
        }

        if (setup.getStartHour() != null) {
            validateByDateBuilder("HOUR", setup.getStartHour().getHour());
            hour.append(setup.getStartHour().getHour());
        } else {
            hour.append("0");
        }

        if (setup.getEndHour() != null) {
            validateByDateBuilder("HOUR", setup.getEndHour().getHour());
            hour.append("-").append(setup.getEndHour().getHour());
        } else {
            hour.append("-").append("23");
        }

        return hour;
    }

    // override this in order to add additional trigger types.
    @Override
    public String determineCronExpression(final SchedulerSetupDTO setup) {
        int hr  = 0;
        int min = 0;
        int sec = 0;

        if (DAILY_OR_LESS_FREQ.contains(setup.getRecurrencyType())) {
            if (setup.getExecutionTime() != null) {
                hr = setup.getExecutionTime().getHour();
                min = setup.getExecutionTime().getMinute();
                sec = setup.getExecutionTime().getSecond();

                validateByDateBuilder("HOUR", hr);
                validateByDateBuilder("MINUTE", min);
                validateByDateBuilder("SECOND", sec);
            }
        }

        switch (setup.getRecurrencyType()) {
        case SECONDLY:
            if (setup.getIntervalMinutes() != null && setup.getIntervalMinutes() == 1 && setup.getIntervalOffset() != null && setup.getIntervalOffset() > 0) {
                throw new T9tException(T9tSsmException.SCHEDULE_SETUP_INTERVAL_VALIDATION_ERR,
                        "For SECONDLY recurrencyType, we don't work on smaller unit than second.");

            } else if (setup.getIntervalMinutes() != null && setup.getIntervalOffset() != null && setup.getIntervalOffset() > setup.getIntervalMinutes()) {
                throw new T9tException(T9tSsmException.SCHEDULE_SETUP_INTERVAL_VALIDATION_ERR,
                        "intervalOffset should be < intervalMinutes.");
            }

            final String secondly = String.format("%s * %s ? * *",
                    getIntervalString(setup.getIntervalMinutes(), setup.getIntervalOffset()),
                    getHourlyOrMoreOftenHour(setup).toString());
            LOGGER.debug("Determined cron expression='{}' for type=SECONDLY", secondly);
            return secondly;


        case MINUTELY:
            if (setup.getIntervalMilliseconds() != null || (setup.getSetOfWeekdays() != null && !setup.getSetOfWeekdays().isEmpty())
                    || setup.getExecutionTime() != null) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                        "For MINUTELY recurrencyType, only intervalMinutes, intervalOffset, startHour and endHour is relevant.");
            }

            String secondOffset = "0";
            String minute = "*";
            if (setup.getIntervalMinutes() != null && setup.getIntervalMinutes() == 1 && setup.getIntervalOffset() != null) {
                validateByDateBuilder("SECOND", setup.getIntervalOffset());
                secondOffset = String.valueOf(setup.getIntervalOffset());
            } else if (setup.getIntervalMinutes() != null && setup.getIntervalMinutes() > 1) {
                validateByDateBuilder("MINUTE", setup.getIntervalMinutes());
                minute = String.format("%s/%s", setup.getIntervalOffset() == null ? 0 : setup.getIntervalOffset(), setup.getIntervalMinutes());
            }

            final String minutely = String.format("%s %s %s ? * *", secondOffset, minute, getHourlyOrMoreOftenHour(setup).toString());
            LOGGER.debug("Determined cron expression='{}' for type=MINUTELY", minutely);
            return minutely;

        case HOURLY:
            if (setup.getExecutionTime() != null && setup.getRepeatCount() != null && setup.getIntervalMilliseconds() != null) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                 "For HOURLY recurrencyType, only startTime(optional), endTime(optional), intervalMinutes(interval) and intervalOffset(offset) should be set.");
            }

            StringBuilder hour = getHourlyOrMoreOftenHour(setup);

            // intervalMinutes == 1 -> intervalOffset = smaller recurrency offset,
            // interval > 1 -> interval = current recurrency interval
            if (setup.getIntervalMinutes() != null && setup.getIntervalMinutes() > 1) {
                validateByDateBuilder("HOUR", setup.getIntervalMinutes());
                if (hour.toString().equals("*") && setup.getIntervalOffset() != null && setup.getIntervalOffset() < setup.getIntervalMinutes()) {
                    hour = new StringBuilder();
                    hour.append(setup.getIntervalOffset().toString());
                } else if (hour.toString().equals("*")) {
                    hour = new StringBuilder("0");
                }
                hour.append("/").append(setup.getIntervalMinutes());

            } else if (setup.getIntervalMinutes() != null && setup.getIntervalMinutes() == 1 && setup.getIntervalOffset() != null) {
                min = setup.getIntervalOffset();
            }

            final String hourly = String.format("0 %s %s ? * *", min, hour.toString());
            LOGGER.debug("Determined cron expression='{}' for type=HOURLY, passed hour={}, interval={}", hourly, hour.toString(), setup.getIntervalOffset());
            return hourly;

        case DAILY:
            if (setup.getEndHour() != null || setup.getStartHour() != null || setup.getIntervalMilliseconds() != null
              || (setup.getSetOfWeekdays() != null && !setup.getSetOfWeekdays().isEmpty())) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                        "For DAILY reccurencyType, only executionTime should be set.");
            }

            if (setup.getExecutionTime() == null) {
                throw new T9tException(T9tSsmException.REQUIRED_SCHEDULER_PARAM_MISSING,
                        "For DAILY reccurencyType, executionTime is required.");
            }

            String dayStr = "*";
            if (setup.getIntervalMinutes() != null)
                dayStr = "1/" + setup.getIntervalMinutes();

            final String daily = String.format("%s %s %s %s * ?", sec, min, hr, dayStr);
            LOGGER.debug("Determined cron expression='{}' for type=DAILY, executionTime={}", daily, setup.getExecutionTime());
            return daily;

        case WEEKLY: {
            if (setup.getRepeatCount() != null || setup.getIntervalMilliseconds() != null || setup.getEndHour() != null || setup.getStartHour() != null) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                        "For WEEKLY recurrencyType, only execution time and either of setOfWeekdays or interval are relevant.");
            }

            if ((setup.getSetOfWeekdays() != null && !setup.getSetOfWeekdays().isEmpty())
              && (setup.getIntervalMinutes() != null || setup.getIntervalOffset() != null)) {
                throw new T9tException(T9tSsmException.SCHEDULE_SETUP_PARAM_VALIDATION_ERR,
                        "For reccurencyType WEEKLY, if setOfWeekdays is set, intervalMinutes and intervalOffset shouldn't be set.");
            }

            if (setup.getIntervalMinutes() != null) {
                //redirect to DAILY
                setup.setRecurrencyType(SchedulerSetupRecurrenceType.DAILY);
                return determineCronExpression(setup);
            }

            final Integer[] days = new Integer[setup.getSetOfWeekdays().size()];
            int i = 0;
            for (final SchedulerSetupRecurrenceWeekdayTypeEnum day : setup.getSetOfWeekdays()) {
                if (day == SchedulerSetupRecurrenceWeekdayTypeEnum.SUNDAY)
                    days[i++] = 1;
                else
                    days[i++] = day.ordinal() + 2;
            }
            final CronTrigger mutableTrigger = (CronTrigger) CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek(hr, min, days).build();
            final String result = mutableTrigger.getCronExpression();

            LOGGER.debug("Determined cron expression='{}' for type=WEEKLY, hourOfDay={}, minuteOfHour={}, dayOfWeek={}.", result, hr, min, days);
            return result;
        }

        case MONTHLY: {
            if (setup.getIntervalMilliseconds() != null && setup.getStartHour() != null && setup.getEndHour() != null) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                        "For MONTHLY recurrencyType, only executionTime is relevant.");
            }
            if (setup.getExecutionTime() == null) {
                throw new T9tException(T9tSsmException.REQUIRED_SCHEDULER_PARAM_MISSING,
                        "For MONTHLY recurrencyType, executionTime can't be null.");
            }

            if (setup.getValidFrom() == null) {
                throw new T9tException(T9tSsmException.SCHEDULE_VALID_FROM_NOT_PROVIDED,
                        "Valid From Date is missing when setup Monthly Schedule.");
            }

            final String dayOfMonth = String.valueOf(setup.getValidFrom().getDayOfMonth());

            String month = "*";
            if (setup.getIntervalMinutes() != null)
                month = String.format("1/%s", setup.getIntervalMinutes());

            final String monthly = String.format("%d %d %d %s %s ?", sec, min, hr, dayOfMonth, month);
            LOGGER.debug("Determined cron expression='{}' for type=MONTHLY, hourOfDay={}, minuteOfHour={}, dayOfMonth={}.", monthly, hr, min, month);
            return monthly;
        }

        case YEARLY: {
            if (setup.getIntervalMilliseconds() != null && setup.getStartHour() != null && setup.getEndHour() != null) {
                throw new T9tException(T9tSsmException.IRRELEVANT_SCHEDULER_PARAM_ERR,
                        "For MONTHLY recurrencyType, only executionTime is relevant.");
            }
            if (setup.getExecutionTime() == null) {
                throw new T9tException(T9tSsmException.REQUIRED_SCHEDULER_PARAM_MISSING,
                        "For MONTHLY recurrencyType, executionTime can't be null.");
            }
            if (setup.getValidFrom() == null) {
                throw new T9tException(T9tSsmException.SCHEDULE_VALID_FROM_NOT_PROVIDED,
                        "Valid From Date is missing when setup Yearly Schedule.");
            }

            final LocalDateTime startDate = setup.getValidFrom();
            final int monthOfYear = startDate.getMonthValue();
            final int dayOfMonth = startDate.getDayOfMonth();

            String year = "*";
            if (setup.getValidTo() != null) {
                year = String.format("%d-%d", startDate.getYear(), setup.getValidTo().getYear());
            }

            validateByDateBuilder("MONTH", monthOfYear);
            validateByDateBuilder("DAYOFMONTH", dayOfMonth);
            final String result = String.format("%d %d %d %d %d ? %s", sec, min, hr, dayOfMonth, monthOfYear, year);
            LOGGER.debug("Determined cron expression='{}' for type=YEARLY, hourOfDay={}, minuteOfHour={}, dayOfMonth={}, monthOfYear={}.",
              result, hr, min, dayOfMonth, monthOfYear);
            return result;
        }

        case CRON_NATIVE: {
            if (setup.getEndHour() != null || setup.getStartHour() != null || setup.getExecutionTime() != null
                    || setup.getIntervalMilliseconds() != null || setup.getIntervalMinutes() != null
                    || setup.getIntervalOffset() != null || (setup.getSetOfWeekdays() != null && !setup.getSetOfWeekdays().isEmpty())
                    || setup.getRepeatCount() != null) {
                LOGGER.error("for CRON native, only cronExpression is relevant");
                throw new T9tException(T9tSsmException.SCHEDULE_CRON_EXPRESSION_MISSING);
            }

            if (setup.getCronExpression() == null || !setup.getCronExpression().matches(CRON_REGEX_PATTERN)) {
                throw new T9tException(T9tSsmException.SCHEDULE_CRON_REGEX_PATTERN_MISMATCH);
            }

            return setup.getCronExpression();
        }

        default:
            // Make sure we don't miss any value here
            final String message = "Unknown Enum value for SchedulerSetupRecurrenceTypeEnum. Found " + setup.getRecurrencyType();
            throw new T9tException(T9tException.NOT_YET_IMPLEMENTED, message);
        }
    }


    private String getIntervalString(final Integer interval, final Integer intervalOffset) {
        if (interval == null || interval == 0 || interval == 1) {
            return "*";
        } else {
            return String.format("%s/%s", intervalOffset == null ? "0" : intervalOffset, interval);
        }
    }

    /**
     * Method to validate by using DateBuilder, return T9tSsmException instead of IllegalArguemtnException
     * to be used on the UI.
     *
     * @param type
     * @param value
     */
    private void validateByDateBuilder(final String type, final int value) {
        try {
            switch (type) {
            case "SECOND":
                DateBuilder.validateSecond(value);
                break;
            case "MINUTE":
                DateBuilder.validateMinute(value);
                break;
            case "HOUR":
                DateBuilder.validateHour(value);
                break;
            case "DAYOFMONTH":
                DateBuilder.validateDayOfMonth(value);
                break;
            case "MONTH":
                DateBuilder.validateMonth(value);
                break;
            default:
                throw new T9tException(T9tException.GENERAL_EXCEPTION_CENTRAL);
            }
        } catch (final IllegalArgumentException e) {
            throw new T9tException(T9tSsmException.SCHEDULE_SETUP_INTERVAL_VALIDATION_ERR, e.getMessage());
        }
    }
}
