package com.arvatosystems.t9t.authc.be.api;

import java.util.Properties;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.authc.api.GetPasswordResetApiKeyRequest;
import com.arvatosystems.t9t.authc.api.GetPasswordResetApiKeyResponse;
import com.arvatosystems.t9t.base.BaseConfigurationProvider;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

public class GetPasswordResetApiKeyRequestHandler extends AbstractReadOnlyRequestHandler<GetPasswordResetApiKeyRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GetPasswordResetApiKeyRequestHandler.class);

    @Override
    public GetPasswordResetApiKeyResponse execute(RequestContext ctx, GetPasswordResetApiKeyRequest rq) {
        final GetPasswordResetApiKeyResponse resp = new GetPasswordResetApiKeyResponse();
        Properties baseProperties = BaseConfigurationProvider.getBaseProperties();
        String candidateForKey = baseProperties.getProperty("forget.password.api.key");
        if (candidateForKey != null) {
            try {
                UUID apiKey = UUID.fromString(candidateForKey);
                resp.setApiKey(apiKey);
            } catch (Exception e) {
                LOGGER.error("Password RESET API-Key malformatted", e);
                // but still return null (avoid exception, because the caller has no immediate impact)
            }
        } else {
            LOGGER.warn("No API key configured for Password RESET");
        }
        return resp;
    }
}
