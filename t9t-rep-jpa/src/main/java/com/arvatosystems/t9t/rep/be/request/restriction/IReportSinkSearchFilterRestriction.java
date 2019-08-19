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
package com.arvatosystems.t9t.rep.be.request.restriction;

import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.bonaparte.core.ObjectValidationException;
import de.jpaw.bonaparte.pojos.api.FieldFilter;


/**
 * SearchFilter restriction for report specific SinkSearch
 * @author RREN001
 */
public interface IReportSinkSearchFilterRestriction {
    public static final String COMM_TARGET_CHANNEL_FIELD_NAME = "commTargetChannelType";
    public static final String DATA_SINK_CATEGORY_FIELD_NAME = "dataSink.category";
    public static final String COMM_FORMAT_TYPE_FIELD_NAME = "commFormatType";
    public static final String FILE_OR_QUEUE_FIELD_NAME = "fileOrQueueName";

    /**
     * do searchFilter checking depending on the fieldName
     * at the moment only handling filter checking for: commTargetChannelType, dataSink.category, commFormatType, configurationRef, fileOrQueueName
     * @param searchFilter
     *              the searchFilter we are checking
     * @throws ObjectValidationException
     *              if the searchFilter is not of the expected type
     * @throws T9tException
     *              if the searchFilter actually contains value that's outside from the permitted for report search
     */
    boolean isSearchFilterCheckingSuccess(FieldFilter searchFilter);
}
