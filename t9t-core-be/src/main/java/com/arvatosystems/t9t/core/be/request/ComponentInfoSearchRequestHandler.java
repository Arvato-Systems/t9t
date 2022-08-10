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
package com.arvatosystems.t9t.core.be.request;

import com.arvatosystems.t9t.base.request.ComponentInfoDTO;
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoRequest;
import com.arvatosystems.t9t.base.request.RetrieveComponentInfoResponse;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.IExporterTool;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.core.request.ComponentInfoSearchRequest;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.dp.Jdp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

public class ComponentInfoSearchRequestHandler extends AbstractSearchRequestHandler<ComponentInfoSearchRequest> {
    private static final String FIELD_NAME_GROUP_ID = "groupId";
    private static final String FIELD_NAME_ARTIFACT_ID = "artifactId";
    private static final String FIELD_NAME_VERSION_STRING = "versionString";
    private static final String FIELD_NAME_COMMIT_ID = "commitId";

    private static final String WILDCARD = "%";
    private static final String DEFAULT_COMMIT_ID = "x";

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    @SuppressWarnings("unchecked")
    protected final IExporterTool<ComponentInfoDTO, NoTracking> exporter = Jdp.getRequired(IExporterTool.class);

    @Override
    public ReadAllResponse<ComponentInfoDTO, NoTracking> execute(final RequestContext ctx, final ComponentInfoSearchRequest rq) throws Exception {
        // map generic search filters to the specific parameters
        final RetrieveComponentInfoRequest cmd = new RetrieveComponentInfoRequest();
        final List<ComponentInfoDTO> components = executor.executeSynchronousAndCheckResult(ctx, cmd, RetrieveComponentInfoResponse.class).getComponents();

        final List<ComponentInfoDTO> filteredList = rq.getSearchFilter() == null ? components : applyFilters(rq.getSearchFilter(), components);
        final List<ComponentInfoDTO> sortedList = rq.getSortColumns() == null || rq.getSortColumns().isEmpty() ? filteredList
                : applySort(rq.getSortColumns().get(0), filteredList);
        final List<ComponentInfoDTO> limitedList = exporter.cut(sortedList, rq.getOffset(), rq.getLimit());

        final List<DataWithTrackingS<ComponentInfoDTO, NoTracking>> dataList = new ArrayList<>(limitedList.size());
        for (ComponentInfoDTO componentInfoDTO: limitedList) {
            final DataWithTrackingS<ComponentInfoDTO, NoTracking> dwt = new DataWithTrackingS<ComponentInfoDTO, NoTracking>();
            dwt.setData(componentInfoDTO);
            dwt.setTenantId(ctx.tenantId);
            dataList.add(dwt);
        }
        return exporter.returnOrExport(dataList, rq.getSearchOutputTarget());
    }

    protected List<ComponentInfoDTO> applyFilters(final SearchFilter searchFilter, final List<ComponentInfoDTO> input) {
        if (searchFilter instanceof AndFilter) {
            final AndFilter andFilter = (AndFilter) searchFilter;
            return applyFilters(andFilter.getFilter1(), applyFilters(andFilter.getFilter2(), input));
        } else if (searchFilter instanceof UnicodeFilter) {
            final UnicodeFilter unicodeFilter = (UnicodeFilter) searchFilter;
            if (unicodeFilter.getEqualsValue() != null) {
                if (FIELD_NAME_GROUP_ID.equals(unicodeFilter.getFieldName())) {
                    final List<ComponentInfoDTO> filteredList = new ArrayList<>(input.size());
                    for (final ComponentInfoDTO componentInfo: input) {
                        if (unicodeFilter.getEqualsValue().equals(componentInfo.getGroupId())) {
                            filteredList.add(componentInfo);
                        }
                    }
                    return filteredList;
                } else if (FIELD_NAME_ARTIFACT_ID.equals(unicodeFilter.getFieldName())) {
                    final List<ComponentInfoDTO> filteredList = new ArrayList<>(input.size());
                    for (final ComponentInfoDTO componentInfo: input) {
                        if (unicodeFilter.getEqualsValue().equals(componentInfo.getArtifactId())) {
                            filteredList.add(componentInfo);
                        }
                    }
                    return filteredList;
                }
            } else if (unicodeFilter.getLikeValue() != null) {
                final String likeValue = unicodeFilter.getLikeValue();
                final String like = likeValue.endsWith(WILDCARD) ? likeValue.substring(0, likeValue.length() - 1) : likeValue;
                if (FIELD_NAME_GROUP_ID.equals(unicodeFilter.getFieldName())) {
                    final List<ComponentInfoDTO> filteredList = new ArrayList<>(input.size());
                    for (final ComponentInfoDTO componentInfo: input) {
                        if (componentInfo.getGroupId().startsWith(like)) {
                            filteredList.add(componentInfo);
                        }
                    }
                    return filteredList;
                } else if (FIELD_NAME_ARTIFACT_ID.equals(unicodeFilter.getFieldName())) {
                    final List<ComponentInfoDTO> filteredList = new ArrayList<>(input.size());
                    for (final ComponentInfoDTO componentInfo: input) {
                        if (componentInfo.getArtifactId().startsWith(like)) {
                            filteredList.add(componentInfo);
                        }
                    }
                    return filteredList;
                }
            }
        }

        return null;
    }

    protected List<ComponentInfoDTO> reverse(final List<ComponentInfoDTO> input, final boolean reverse) {
        if (reverse) {
            final List<ComponentInfoDTO> tmp = new ArrayList<>(input);
            Collections.reverse(tmp);
            return tmp;
        } else {
            return input;
        }
    }

    protected List<ComponentInfoDTO> applySort(final SortColumn sortColumn, final List<ComponentInfoDTO> input) {
        if (sortColumn.getFieldName() != null) {
            switch (sortColumn.getFieldName()) {
            case FIELD_NAME_GROUP_ID:
                input.sort(compareByField(ComponentInfoDTO::getGroupId));
                return reverse(input, sortColumn.getDescending());
            case FIELD_NAME_ARTIFACT_ID:
                input.sort(compareByField(ComponentInfoDTO::getArtifactId));
                return reverse(input, sortColumn.getDescending());
            case FIELD_NAME_VERSION_STRING:
                input.sort(compareByField(ComponentInfoDTO::getVersionString));
                return reverse(input, sortColumn.getDescending());
            case FIELD_NAME_COMMIT_ID:
                input.sort(compareByField((componentInfoDTO) -> {
                    if (componentInfoDTO.getCommitId() == null) {
                        return DEFAULT_COMMIT_ID;
                    }
                    return componentInfoDTO.getCommitId();
                }));
                return reverse(input, sortColumn.getDescending());
            }
        }
        return null;
    }

    private static Comparator<ComponentInfoDTO> compareByField (final Function<ComponentInfoDTO, String> func) {
        return (item1, item2) -> func.apply(item1).compareTo(func.apply(item2));
    }
}
