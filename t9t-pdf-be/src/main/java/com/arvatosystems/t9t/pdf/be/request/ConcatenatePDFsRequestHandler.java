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
package com.arvatosystems.t9t.pdf.be.request;

import java.util.List;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.output.OutputSessionParameters;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.search.SinkCreatedResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IFileUtil;
import com.arvatosystems.t9t.base.services.IOutputSession;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.io.SinkDTO;
import com.arvatosystems.t9t.io.request.SinkSearchRequest;
import com.arvatosystems.t9t.pdf.request.ConcatenatePDFsRequest;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

public class ConcatenatePDFsRequestHandler extends AbstractRequestHandler<ConcatenatePDFsRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcatenatePDFsRequestHandler.class);

    private final IExecutor executor = Jdp.getRequired(IExecutor.class);
    private final IFileUtil fileUtil = Jdp.getRequired(IFileUtil.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final ConcatenatePDFsRequest concatRequest) throws Exception {
        final PDFMergerUtility pdfMergerUtility = new PDFMergerUtility();

        // Lookup PDFs to be merged
        final ReadAllResponse<SinkDTO, FullTrackingWithVersion> sinkSearchResponse = getDataSinks(ctx, concatRequest.getSinkRefsToMerge(), null);
        final SinkDTO[] toMergeSinkDTOs = sortToMergeDataSinks(sinkSearchResponse.getDataList(), concatRequest.getSinkRefsToMerge());

        for (final SinkDTO toMerge : toMergeSinkDTOs) {
            pdfMergerUtility.addSource(fileUtil.getAbsolutePathForTenant(ctx.tenantId, toMerge.getFileOrQueueName()));
        }

        // create output sink
        Long outputSinkRef;
        final OutputSessionParameters sessionParams = new OutputSessionParameters();
        sessionParams.setDataSinkId(concatRequest.getOutputDataSinkId());

        try (IOutputSession os = Jdp.getRequired(IOutputSession.class)) {
            outputSinkRef = os.open(sessionParams);
            pdfMergerUtility.setDestinationStream(os.getOutputStream());
            pdfMergerUtility.mergeDocuments(IOUtils.createMemoryOnlyStreamCache());
        } catch (final Exception e) {
            LOGGER.error("Exception during merging PDFs - Error Message {} ", e.getMessage());
            throw new T9tException(T9tException.GENERAL_EXCEPTION, e.getMessage());
        }

        final SinkCreatedResponse response = new SinkCreatedResponse();
        response.setReturnCode(0);
        response.setSinkRef(outputSinkRef);
        return response;
    }

    private ReadAllResponse<SinkDTO, FullTrackingWithVersion> getDataSinks(final RequestContext ctx, final List<Long> valueList, final Long equalsValue) {
        final SinkSearchRequest sinkSearchRequest = new SinkSearchRequest();
        sinkSearchRequest.setSearchFilter(new LongFilter("objectRef", equalsValue, null, null, valueList));
        return executor.executeSynchronousAndCheckResult(ctx, sinkSearchRequest, ReadAllResponse.class);
    }

    private SinkDTO[] sortToMergeDataSinks(final List<DataWithTrackingS<SinkDTO, FullTrackingWithVersion>> dataList, final List<Long> sinkRefs) {

        // ensure same amount of PDF files are merged as requested
        if (dataList.size() != sinkRefs.size()) {
            throw new T9tException(T9tException.RECORD_DOES_NOT_EXIST,
              "Requested files to merge is " + sinkRefs.size() + " but actual files found is " + dataList.size());
        }

        final SinkDTO[] sortedSinks = new SinkDTO[sinkRefs.size()];
        for (final DataWithTrackingS<SinkDTO, FullTrackingWithVersion> item : dataList) {
            final int idx = sinkRefs.indexOf(item.getData().getObjectRef());
            sortedSinks[idx] = item.getData();
        }

        return sortedSinks;
    }
}
