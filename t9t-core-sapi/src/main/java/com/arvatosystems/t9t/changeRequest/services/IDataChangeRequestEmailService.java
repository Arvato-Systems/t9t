package com.arvatosystems.t9t.changeRequest.services;

import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.changeRequest.DataChangeRequestDTO;
import jakarta.annotation.Nonnull;

public interface IDataChangeRequestEmailService {

    void sendReviewEmail(@Nonnull RequestContext ctx, @Nonnull DataChangeRequestDTO data);

}
