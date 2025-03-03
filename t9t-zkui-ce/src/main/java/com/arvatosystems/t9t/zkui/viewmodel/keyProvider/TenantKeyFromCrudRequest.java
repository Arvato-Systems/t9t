package com.arvatosystems.t9t.zkui.viewmodel.keyProvider;

import com.arvatosystems.t9t.auth.TenantDTO;
import com.arvatosystems.t9t.auth.request.TenantCrudRequest;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.zkui.IKeyFromCrudRequest;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("tenant")
public class TenantKeyFromCrudRequest implements IKeyFromCrudRequest<TenantDTO, FullTrackingWithVersion, TenantCrudRequest> {

    @Override
    public SearchFilter getFilterForKey(final TenantCrudRequest crudRequest) {
        final UnicodeFilter tenantFilter = new UnicodeFilter(TenantDTO.meta$$tenantId.getName());
        tenantFilter.setEqualsValue(crudRequest.getKey());
        return tenantFilter;
    }
}
