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
package com.arvatosystems.t9t.rep.be.request.restriction.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.StringFilter;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.io.CommunicationTargetChannelType;
import com.arvatosystems.t9t.io.DataSinkCategoryType;
import com.arvatosystems.t9t.rep.ReportConfigDTO;
import com.arvatosystems.t9t.rep.be.request.restriction.IReportSinkSearchFilterRestriction;
import com.arvatosystems.t9t.rep.request.ReportConfigSearchRequest;

/**
 * implementation of {@link IReportSinkSearchFilterRestriction}
 * @author RREN001
 */
@Singleton
public class ReportSinkSearchFilterRestriction implements IReportSinkSearchFilterRestriction {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReportSinkSearchFilterRestriction.class);

    protected final IExecutor messaging = Jdp.getRequired(IExecutor.class);

    private static final List<String> PERMITTED_COMMUNICATION_FORMAT = Arrays.asList(MediaType.PDF.getToken(),
            MediaType.CSV.getToken(), MediaType.XLS.getToken());

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isSearchFilterCheckingSuccess(final FieldFilter searchFilter) {
        boolean currentCheckingResult = true;
        switch (searchFilter.getFieldName()) {
            case COMM_TARGET_CHANNEL_FIELD_NAME:
                currentCheckingResult = currentCheckingResult && doCommTargetChannelTypeFilterChecking(searchFilter);
                break;
            case DATA_SINK_CATEGORY_FIELD_NAME:
                currentCheckingResult = currentCheckingResult && doDataSinkFilterChecking(searchFilter);
                break;
            case COMM_FORMAT_TYPE_FIELD_NAME:
                currentCheckingResult = currentCheckingResult && doCommFormatTypeFilterChecking(searchFilter);
                break;
            case FILE_OR_QUEUE_FIELD_NAME:
                currentCheckingResult = currentCheckingResult && doFileOrQueueNameFilterChecking((StringFilter)searchFilter);
                break;
        }

        return currentCheckingResult;
    }


    /**
     * do filter checking for fileOrQueueName field
     * @param searchFilter
     * @throws T9tException
     *              if the filter has more than one value available
     */
    private boolean doFileOrQueueNameFilterChecking(final StringFilter searchFilter) {
        doStringFilterTypeChecking(searchFilter);

        if ((isStringFilterUsingEqualsValue(searchFilter) && isStringFilterUsingLikeValue(searchFilter))
          || (isStringFilterUsingLikeValue(searchFilter) && isStringFilterUsingValueList(searchFilter))) {
            LOGGER.error("Filter value should be set in either equalValue or likeValue or valueList and not more than one");
            throw new T9tException(T9tException.UNEXPECTED_FILTER_VALUE);
        }

        // if we are using using likeValue, we might need to do some fileName conversion
        if (isStringFilterUsingLikeValue(searchFilter)) {
            searchFilter.setLikeValue(processPermittedFileName(searchFilter.getLikeValue()));
        }
        return true;
    }


    /**
     * do filter checking for commFormatType:
     *      1. filter if it's on the correct filter type
     *      2. filter its content
     * @param searchFilter
     *              the search filter we are validating
     * @throws T9tException
     *              if the searchFilter is not of an StringFilter type
     *              if the searchFilter's value checking fails
     */
    private boolean doCommFormatTypeFilterChecking(final SearchFilter searchFilter) {
        doStringFilterTypeChecking(searchFilter);
        doStringFilterContentChecking((StringFilter)searchFilter, PERMITTED_COMMUNICATION_FORMAT);
        return true;
    }


    /**
     * do filter checking for dataSink.category field
     * @param searchFilter
     *              the searchFilter that we are processing
     * @throws T9tException
     *              if the searchFilter is not of an StringFilter type
     *              if the searchFilter actually contains value that's outside from the permitted for report search
     */
    private boolean doDataSinkFilterChecking(final SearchFilter searchFilter) {
        doStringFilterTypeChecking(searchFilter);
        doStringFilterContentChecking((StringFilter)searchFilter, Arrays.asList(DataSinkCategoryType.REPORT.getToken()));
        return true;
    }


    /**
     * do filtering for communicationTargetChannelType for the report search
     * @param searchFilter
     *              the search filter that we are checking
     * @throws T9tException
     *              if the searchFilter is not of an StringFilter type
     *              if the searchFilter actually contains value that's outside from the permitted for report search
     */
    private boolean doCommTargetChannelTypeFilterChecking(final SearchFilter searchFilter) {
        doStringFilterTypeChecking(searchFilter);
        doStringFilterContentChecking((StringFilter)searchFilter, Arrays.asList(CommunicationTargetChannelType.FILE.getToken()));
        return true;
    }


    /**
     * check if the searchFilter is of an StringFilter type
     * @param searchFilter
     *              the searchFilter we are checking
     * @throws T9tException
     *              if the searchFilter is not of StringFilter type
     */
    protected void doStringFilterTypeChecking(final SearchFilter searchFilter) {
        if (!(searchFilter instanceof StringFilter)) {
            throw new T9tException(T9tException.SEARCH_FILTER_VALIDATION_ERROR);
        }
    }


    /**
     * check if the searchFilter is of an LongFilter type
     * @param searchFilter
     *              the searchFilter we are checking
     * @throws T9tException
     *              if the searchFilter is not of LongFilter type
     */
    protected void doLongFilterTypeChecking(final SearchFilter searchFilter) {
        if (!(searchFilter instanceof LongFilter)) {
            throw new T9tException(T9tException.SEARCH_FILTER_VALIDATION_ERROR);
        }
    }


    /**
     * filtering the content of asciiFilter by the following:
     *      1. filter if it only has one value field being set
     *      2. if the filter is empty, put all permitted value to the filter
     *      3. if the is a value being provided, check if it's permitted
     * @param asciiFilter
     *              the asciiFilter we are checking
     * @param permittedContent
     *              the content that we are expecting
     * @throws T9tException
     *              if the filter has more than one value available
     *              if the value provided is outside of the permitted values
     */
    protected void doStringFilterContentChecking(final StringFilter asciiFilter, final List<String> permittedContent) {
        if ((isStringFilterUsingEqualsValue(asciiFilter) && isStringFilterUsingLikeValue(asciiFilter))
          || (isStringFilterUsingLikeValue(asciiFilter) && isStringFilterUsingValueList(asciiFilter))
          || (isStringFilterUsingEqualsValue(asciiFilter) && isStringFilterUsingValueList(asciiFilter))) {
            LOGGER.error("Filter value should be set in either equalValue or likeValue or valueList and not more than one");
            throw new T9tException(T9tException.UNEXPECTED_FILTER_VALUE);
        } else if ((isStringFilterUsingEqualsValue(asciiFilter) || isStringFilterUsingLikeValue(asciiFilter)
          || !isStringFilterUsingValueList(asciiFilter))) {
            asciiFilter.setValueList(permittedContent);

        } else {
            // for any provided filter check if it's within the permitted communication format
            if ((isStringFilterUsingEqualsValue(asciiFilter) && !permittedContent.contains((asciiFilter).getEqualsValue()))
              || (isStringFilterUsingLikeValue(asciiFilter) && !permittedContent.contains((asciiFilter).getLikeValue()))
              || (isStringFilterUsingValueList(asciiFilter) && !permittedContent.containsAll((asciiFilter).getValueList()))) {
                throw new T9tException(T9tException.RESTRICTED_ACCESS);
            }
        }
    }


    /**
     * filtering the content of longFilter by the following:
     *      1. filter if it only has either range value/valueList field being set
     *      2. if the filter is empty, put all permitted value to the filter
     *      3. if the is a value being provided, check if it's permitted
     * @param longFilter
     *              the longFilter we are checking
     * @param permittedContent
     *              the content that we are expecting
     * @throws T9tException
     *              if the filter has more than one value available
     *              if the value provided is outside of the permitted values
     */
    protected void doLongFilterContentChecking(final LongFilter longFilter, final List<Long> permittedContent) {
        //invalid usage of the filter. We only expect either lowerBound & upperBound is not null or valueList is not null, but not both
        if ((isLongFilterUsingValueList(longFilter) && isLongFilterUsingRange(longFilter))
          // we don't expect a range in our reference
          || isLongFilterRangeEquivalent(longFilter)) {
            LOGGER.error("Filter value should be set in either lowerBound-upperBound or valueList and not more than one");
            throw new T9tException(T9tException.UNEXPECTED_FILTER_VALUE);

            // no filter provided, set to default
        } else if (!isLongFilterUsingValueList(longFilter) && !isLongFilterUsingRange(longFilter)) {
            longFilter.setValueList(permittedContent);

        } else {
            // for any provided filter, check if it is in the PERMITTED_CONFIGURATION_REF
            if ((isLongFilterUsingValueList(longFilter) && !permittedContent.containsAll(longFilter.getValueList()))
              || (isLongFilterUsingRange((longFilter)) && !permittedContent.contains(longFilter.getLowerBound()))) {
                throw new T9tException(T9tException.RESTRICTED_ACCESS);
            }
        }
    }


    /**
     * process permitted file name into SQL-safe (as the wildcards is differ from java style) if any
     * @param inputFileName
     *              the input file name we are processing
     * @return
     *              a SQL-safe fileName
     */
    private String processPermittedFileName(final String inputFileName) {
        if (!inputFileName.contains(T9tConstants.WILDCARD) && !inputFileName.contains("%")) {
            return inputFileName;
        }

        return inputFileName.replaceAll("\\*", "%");
    }


    /**
     * resolve the permitted reportConfigs depending on the user's permission
     * @return
     *              list of permitted ReportConfigDTO for the user
     * @throws T9tException
     *              if any issue with the persistence
     */
    protected List<ReportConfigDTO> resolvePermittedReportConfigsDTO() {
        //first we need to retrieve the ReportConfigId that's eligible for the user
        final ReportConfigSearchRequest reportConfigSearchRequest = new ReportConfigSearchRequest();

        final ReadAllResponse<ReportConfigDTO, FullTrackingWithVersion> response =
                messaging.executeSynchronousAndCheckResult(reportConfigSearchRequest, ReadAllResponse.class);
        final List<DataWithTrackingS<ReportConfigDTO, FullTrackingWithVersion>> permittedReports = response.getDataList();

        final List<ReportConfigDTO> permittedReportConfigs = new ArrayList<>();
        for (final DataWithTrackingS<ReportConfigDTO, FullTrackingWithVersion> permittedReport : permittedReports) {
            permittedReportConfigs.add(permittedReport.getData());
        }
        return permittedReportConfigs;
    }


    /**
     * retrieve list of long reference from list of ReportConfigDTO
     * @param dto
     *              the dto we want to get its objectRef from
     * @return
     *              list of long reference from the dto
     */
    protected <T extends Ref>   List<Long> getLongRefsFromDto(final List<T> dtos) {
        return Lists.transform(dtos, new Function<T, Long>() {
            @Override
            public Long apply(final T dto) {
                return dto.getObjectRef();
            }
        });
    }


    /**
     * check whether the lowerBound field in the LongFilter is equals with its upperBound's value
     * @param longFilter
     *              the filter that we want to process
     * @return
     *              whether or not the lowerBound meaningfully equals with upperBound
     */
    protected boolean isLongFilterRangeEquivalent(final LongFilter longFilter) {
        return longFilter.getLowerBound() != null && longFilter.getUpperBound() != null
          && longFilter.getLowerBound().equals(longFilter.getUpperBound());
    }


    /**
     * check whether the longFilter is using the lowerBound/upperBound field
     * @param longFilter
     *              the filter we want to check
     * @return
     *              true: it use the lowerBound/upperBound; false otherwise
     */
    protected boolean isLongFilterUsingRange(final LongFilter longFilter) {
        return longFilter.getLowerBound() != null || longFilter.getUpperBound() != null;
    }


    /**
     * check whether the longFilter is using the valueList field
     * @param longFilter
     *              the filter we want to check
     * @return
     *              true: it use the valueList; false otherwise
     */
    protected boolean isLongFilterUsingValueList(final LongFilter longFilter) {
        return longFilter.getValueList() != null && !longFilter.getValueList().isEmpty();
    }

    protected boolean isStringFilterUsingEqualsValue(final StringFilter asciiFilter) {
        return asciiFilter.getEqualsValue() != null && !asciiFilter.getEqualsValue().trim().isEmpty();
    }

    protected boolean isStringFilterUsingLikeValue(final StringFilter asciiFilter) {
        return asciiFilter.getLikeValue() != null && !asciiFilter.getLikeValue().trim().isEmpty();
    }

    protected boolean isStringFilterUsingValueList(final StringFilter asciiFilter) {
        return asciiFilter.getValueList() != null && !asciiFilter.getValueList().isEmpty();
    }

    protected boolean isStringFilterEqualsValueSameTo(final StringFilter asciiFilter, final String content) {
        return asciiFilter.getEqualsValue().equals(content);
    }

    //we only accept an exact equals (just like equalValue) because it may match something that it shouldn't if we accept wildcard
    protected boolean isStringFilterLikeValueSameTo(final StringFilter asciiFilter, final String content) {
        return asciiFilter.getLikeValue().equals(content);
    }

    protected boolean isStringFilterValueListIsIn(final StringFilter asciiFilter, final List<String> contents) {
        return contents.containsAll(asciiFilter.getValueList());
    }
}
