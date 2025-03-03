package com.arvatosystems.t9t.changeRequest.jpa.impl;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.jpa.impl.AbstractConfigCache;
import com.arvatosystems.t9t.changeRequest.jpa.mapping.IChangeWorkFlowConfigDTOMapper;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigDTO;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigKey;
import com.arvatosystems.t9t.changeRequest.ChangeWorkFlowConfigRef;
import com.arvatosystems.t9t.changeRequest.jpa.entities.ChangeWorkFlowConfigEntity;
import com.arvatosystems.t9t.changeRequest.jpa.persistence.IChangeWorkFlowConfigEntityResolver;
import com.arvatosystems.t9t.changeRequest.services.IChangeWorkFlowConfigCache;
import de.jpaw.bonaparte.pojos.apiw.Ref;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import jakarta.annotation.Nonnull;

import java.util.Map;

@Singleton
public class ChangeWorkFlowConfigCache extends AbstractConfigCache<ChangeWorkFlowConfigDTO, FullTrackingWithVersion, ChangeWorkFlowConfigEntity>
    implements IChangeWorkFlowConfigCache {

    private final IChangeWorkFlowConfigDTOMapper mapper = Jdp.getRequired(IChangeWorkFlowConfigDTOMapper.class);

    public ChangeWorkFlowConfigCache() {
        super(Jdp.getRequired(IChangeWorkFlowConfigEntityResolver.class), ChangeWorkFlowConfigDTO.class, false);
    }

    @Override
    protected void populateCache(@Nonnull final Map<Ref, ChangeWorkFlowConfigDTO> cache, @Nonnull final ChangeWorkFlowConfigEntity e) {
        // create a frozen DTO, in order to prevent subsequent changes
        final ChangeWorkFlowConfigDTO dto = mapper.mapToDto(e);
        dto.freeze();
        // create a frozen key
        final ChangeWorkFlowConfigKey key = new ChangeWorkFlowConfigKey(e.getPqon());
        key.freeze();
        cache.put(key, dto);
        // also create an entry for access by primary key
        final ChangeWorkFlowConfigRef ref = new ChangeWorkFlowConfigRef(e.getObjectRef());
        ref.freeze();
        cache.put(ref, dto);
    }

    @Override
    public ChangeWorkFlowConfigDTO getOrNull(@Nonnull final String pqon) {
        return getOrNull(new ChangeWorkFlowConfigKey(pqon));
    }
}
