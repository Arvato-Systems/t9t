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
package com.arvatosystems.t9t.changeRequest.jpa.request;

import com.arvatosystems.t9t.base.auth.PermissionType;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.EnumFilter;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.AbstractSearchRequestHandler;
import com.arvatosystems.t9t.base.services.ISearchTools;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowStatus;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestExtendedDTO;
import com.arvatosystems.t9t.changeRequest.jpa.entities.DataChangeRequestEntity;
import com.arvatosystems.t9t.changeRequest.jpa.mapping.IDataChangeRequestDTOMapper;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IDataChangeRequestEntityResolver;
import com.arvatosystems.t9t.changeRequest.request.DataChangeRequestExtendedSearchRequest;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import com.arvatosystems.t9t.server.services.IAuthorize;
import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.DataWithTrackingS;
import de.jpaw.bonaparte.pojos.api.FieldFilter;
import de.jpaw.bonaparte.pojos.api.NotFilter;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.OrFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;
import de.jpaw.dp.Jdp;
import jakarta.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

public class DataChangeRequestExtendedSearchRequestHandler extends AbstractSearchRequestHandler<DataChangeRequestExtendedSearchRequest> {

    private static final String FIELD_NAME_PREFIX = DataChangeRequestExtendedDTO.meta$$change.getName() + ".";
    protected final IDataChangeRequestEntityResolver resolver = Jdp.getRequired(IDataChangeRequestEntityResolver.class);
    protected final IDataChangeRequestDTOMapper mapper = Jdp.getRequired(IDataChangeRequestDTOMapper.class);
    protected final IAuthorize authorizer = Jdp.getRequired(IAuthorize.class);
    protected final IChangeWorkFlowConfigCache configCache = Jdp.getRequired(IChangeWorkFlowConfigCache.class);
    protected final ISearchTools searchTool = Jdp.getRequired(ISearchTools.class);

    @Nonnull
    @Override
    public ReadAllResponse<DataChangeRequestExtendedDTO, FullTrackingWithVersion> execute(@Nonnull final RequestContext ctx,
        @Nonnull final DataChangeRequestExtendedSearchRequest request) throws Exception {

        final List<DataWithTrackingS<DataChangeRequestExtendedDTO, FullTrackingWithVersion>> dwtList = new ArrayList<>();
        final ReadAllResponse<DataChangeRequestExtendedDTO, FullTrackingWithVersion> response = new ReadAllResponse<>();
        response.setDataList(dwtList);

        searchTool.mapNames(request, fieldName -> fieldName.replace(FIELD_NAME_PREFIX, ""));

        if (request.getLimit() > 0) {
            // query without limit. limit is applied in the next step
            request.setLimit(0);
        }
        final int limit = request.getLimit();

        adjustSorting(request);
        adjustFilter(request);

        final List<DataChangeRequestEntity> entityList = resolver.search(request, null);

        int count = 0;
        if (entityList != null) {
            for (DataChangeRequestEntity entity : entityList) {
                count++;
                Permissionset permissions = getPermissionForChangeRequest(ctx, entity);
                if (!permissions.isEmpty()) {
                    final DataWithTrackingS<DataChangeRequestExtendedDTO, FullTrackingWithVersion> dwt = new DataWithTrackingS<>();
                    final DataChangeRequestExtendedDTO dto = new DataChangeRequestExtendedDTO();
                    dto.setChange(mapper.mapToDto(entity));
                    // if already activated then remove permissions
                    if (ChangeWorkFlowStatus.ACTIVATED == entity.getStatus()) {
                        permissions.clear();
                    }
                    dto.setPermissions(permissions);
                    dwt.setData(dto);
                    dwt.setTracking(entity.ret$Tracking());
                    dwt.setTenantId(entity.getTenantId());
                    dwtList.add(dwt);
                }
                if (limit > 0 && count == limit) {  // limit is applied here. 0 is unlimited.
                    break;
                }
            }
        }

        return response;
    }

    protected void adjustSorting(@Nonnull final DataChangeRequestExtendedSearchRequest request) {
        if (request.getSortColumns() == null) {
            request.setSortColumns(new ArrayList<>());
        }
        final List<SortColumn> sortColumns = request.getSortColumns();
        if (sortColumns.isEmpty()) {
            // if no sorting applied, sort by creation date
            sortColumns.add(new SortColumn(DataChangeRequestDTO.meta$$whenCreated.getName(), true));
        }
        // always sort by objectRef for consistent pagination results
        sortColumns.add(new SortColumn(DataChangeRequestDTO.meta$$objectRef.getName(), false));
    }

    protected void adjustFilter(@Nonnull final DataChangeRequestExtendedSearchRequest request) {
        // if the filter is null or not for status field then add status filter (status != ACTIVATED)
        if (request.getSearchFilter() == null) {
            request.setSearchFilter(getExcludeActivatedFilter());
        } else if (!hasStatusOrObjectRefFilter(request.getSearchFilter())) {
            request.setSearchFilter(new AndFilter(request.getSearchFilter(), getExcludeActivatedFilter()));
        }
    }

    private SearchFilter getExcludeActivatedFilter() {
        final EnumFilter enumFilter = new EnumFilter();
        enumFilter.setFieldName(DataChangeRequestDTO.meta$$status.getName());
        enumFilter.setEqualsToken(ChangeWorkFlowStatus.ACTIVATED.getToken());
        return new NotFilter(enumFilter);
    }

    protected boolean hasStatusOrObjectRefFilter(@Nonnull final SearchFilter filter) {
        if (filter instanceof NotFilter notFilter) {
            return hasStatusOrObjectRefFilter(notFilter.getFilter());
        } else if (filter instanceof AndFilter andFilter) {
            return hasStatusOrObjectRefFilter(andFilter.getFilter1()) || hasStatusOrObjectRefFilter(andFilter.getFilter2());
        } else if (filter instanceof OrFilter orFilter) {
            return hasStatusOrObjectRefFilter(orFilter.getFilter1()) || hasStatusOrObjectRefFilter(orFilter.getFilter2());
        } else if (filter instanceof FieldFilter fieldFilter) {
            return fieldFilter.getFieldName().equals(DataChangeRequestDTO.meta$$status.getName())
                || fieldFilter.getFieldName().equals(DataChangeRequestDTO.meta$$objectRef.getName());
        } else {
            throw new IllegalArgumentException("Unknown filter type " + filter.getClass().getSimpleName());
        }
    }

    @Nonnull
    protected Permissionset getPermissionForChangeRequest(@Nonnull final RequestContext ctx, @Nonnull final DataChangeRequestEntity entity) {
        final CrudAnyKeyRequest<?, ?> crudRequest = (CrudAnyKeyRequest<?, ?>) entity.getCrudRequest();
        final ChangeWorkFlowConfigDTO config = configCache.getOrNull(entity.getPqon());
        final Permissionset authPermissions = authorizer.getPermissions(ctx.internalHeaderParameters.getJwtInfo(), PermissionType.BACKEND,
            crudRequest.ret$PQON());
        final Permissionset permissions = new Permissionset();
        final boolean isPrivate = config != null && config.getPrivateChangeIds() && !entity.getUserIdCreated().equals(ctx.userId);
        final boolean enforceFourEyes = config != null && config.getEnforceFourEyes() && entity.getUserIdSubmitted() != null
            && entity.getUserIdSubmitted().equals(ctx.userId);

        if (!isPrivate && authPermissions.contains(crudRequest.getCrud())) {
            // user can edit the request data and also delete the whole request
            permissions.add(OperationType.UPDATE);
            permissions.add(OperationType.DELETE);
        }

        // User can reject if the request is in TO_REVIEW or APPROVED status and user has REJECT permission
        if (authPermissions.contains(OperationType.REJECT) && (ChangeWorkFlowStatus.TO_REVIEW == entity.getStatus()
            || ChangeWorkFlowStatus.APPROVED == entity.getStatus())) {
            permissions.add(OperationType.REJECT);
        }

        // User can approve if the request is in TO_REVIEW status and user has APPROVE permission
        if (!enforceFourEyes && authPermissions.contains(OperationType.APPROVE) && ChangeWorkFlowStatus.TO_REVIEW == entity.getStatus()) {
            permissions.add(OperationType.APPROVE);
        }

        // User can activate if the request is in APPROVED status and user has ACTIVATE permission
        if (authPermissions.contains(OperationType.ACTIVATE) && ChangeWorkFlowStatus.APPROVED == entity.getStatus()) {
            permissions.add(OperationType.ACTIVATE);
        }

        return permissions;
    }
}
