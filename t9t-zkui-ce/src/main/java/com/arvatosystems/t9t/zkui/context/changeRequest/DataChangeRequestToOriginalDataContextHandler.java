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
package com.arvatosystems.t9t.zkui.context.changeRequest;

import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.crud.CrudAnyKeyRequest;
import com.arvatosystems.t9t.base.types.LongKey;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestExtendedDTO;
import com.arvatosystems.t9t.zkui.IKeyFromCrudRequest;
import com.arvatosystems.t9t.zkui.IKeyFromDataProvider;
import com.arvatosystems.t9t.zkui.components.basic.Grid28;
import com.arvatosystems.t9t.zkui.context.IGridContextMenu;
import com.arvatosystems.t9t.zkui.services.IChangeWorkFlowConfigDAO;
import com.arvatosystems.t9t.zkui.util.JumpTool;
import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.NoTracking;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

/**
 * Jump to the original screen to show the original data of a data change request.
 */
@Singleton
@Named("dataChangeRequestExtended.ctx.toOriginal")
public class DataChangeRequestToOriginalDataContextHandler implements IGridContextMenu<DataChangeRequestExtendedDTO> {

    private static final TrackingBase DUMMY_TRACKING = new NoTracking();

    protected final IChangeWorkFlowConfigDAO configCache = Jdp.getRequired(IChangeWorkFlowConfigDAO.class);

    @SuppressWarnings("unchecked")
    public boolean isEnabled(@Nonnull final DataWithTracking<DataChangeRequestExtendedDTO, TrackingBase> dwt) {
        final DataChangeRequestDTO dataChange = dwt.getData().getChange();
        final CrudAnyKeyRequest<BonaPortable, TrackingBase> crudAnyKeyRequest = (CrudAnyKeyRequest<BonaPortable, TrackingBase>) dataChange.getCrudRequest();
        if (OperationType.CREATE == crudAnyKeyRequest.getCrud()) {
            return false;
        }
        final ChangeWorkFlowConfigDTO config = configCache.getChangeWorkFlowConfigByPqon(dataChange.getPqon());
        return config != null && T9tUtil.isNotBlank(config.getScreenLocation());
    }

    @Override
    public void selected(@Nonnull final Grid28 lb, @Nonnull final DataWithTracking<DataChangeRequestExtendedDTO, TrackingBase> dwt) {
        final DataChangeRequestDTO dataChange = dwt.getData().getChange();
        final ChangeWorkFlowConfigDTO config = configCache.getChangeWorkFlowConfigByPqon(dataChange.getPqon());
        if (config != null) {
            final SearchFilter searchFilter = getSearchFilter(dataChange, config.getViewModelId());
            JumpTool.jump(config.getScreenLocation(), searchFilter, "screens/data_admin/dataChangeRequestExtended28.zul");
        }
    }

    @SuppressWarnings("unchecked")
    private SearchFilter getSearchFilter(@Nonnull final DataChangeRequestDTO dataChange, @Nonnull final String viewModelId) {
        final CrudAnyKeyRequest<BonaPortable, TrackingBase> crudAnyKeyRequest = (CrudAnyKeyRequest<BonaPortable, TrackingBase>) dataChange.getCrudRequest();
        final BonaPortable data = crudAnyKeyRequest.getData();
        if (data != null) {
            IKeyFromDataProvider<BonaPortable, TrackingBase> keyFromDataProvider = Jdp.getRequired(IKeyFromDataProvider.class, viewModelId);
            return keyFromDataProvider.getFilterForKey(new DataWithTracking<>(data, DUMMY_TRACKING));
        } else if (dataChange.getKey() instanceof LongKey longKey) {
            final LongFilter longFilter = new LongFilter("objectRef");
            longFilter.setEqualsValue(longKey.getKey());
            return longFilter;
        } else {
            // extract key filter from the CRUD request
            IKeyFromCrudRequest<BonaPortable, TrackingBase, CrudAnyKeyRequest<BonaPortable, TrackingBase>> keyFromCrudRequest
                = Jdp.getRequired(IKeyFromCrudRequest.class, viewModelId);
            return keyFromCrudRequest.getFilterForKey(crudAnyKeyRequest);
        }
    }
}
