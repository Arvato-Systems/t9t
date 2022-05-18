package com.arvatosystems.t9t.auth.be.request;

import com.arvatosystems.t9t.auth.AuthModuleCfgDTO;
import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsRequest;
import com.arvatosystems.t9t.auth.request.GetPasswordChangeRequirementsResponse;
import com.arvatosystems.t9t.auth.services.IAuthModuleCfgDtoResolver;
import com.arvatosystems.t9t.base.services.AbstractReadOnlyRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;

import de.jpaw.dp.Jdp;

public class GetPasswordChangeRequirementsRequestHandler extends AbstractReadOnlyRequestHandler<GetPasswordChangeRequirementsRequest> {

    protected final IAuthModuleCfgDtoResolver authModuleCfgResolver = Jdp.getRequired(IAuthModuleCfgDtoResolver.class);

    @Override
    public GetPasswordChangeRequirementsResponse execute(final RequestContext ctx, final GetPasswordChangeRequirementsRequest request) throws Exception {
        final AuthModuleCfgDTO moduleCfg = authModuleCfgResolver.getModuleConfiguration();
        final GetPasswordChangeRequirementsResponse resp = new GetPasswordChangeRequirementsResponse();
        resp.setPasswordDifferPreviousN(nvl(moduleCfg.getPasswordDifferPreviousN()));
        resp.setPasswordMinimumLength(nvl(moduleCfg.getPasswordMinimumLength()));
        resp.setPasswordMinDigits(nvl(moduleCfg.getPasswordMinDigits()));
        resp.setPasswordMinLetters(nvl(moduleCfg.getPasswordMinLetters()));
        resp.setPasswordMinOtherChars(nvl(moduleCfg.getPasswordMinOtherChars()));
        resp.setPasswordMinUppercase(nvl(moduleCfg.getPasswordMinUppercase()));
        resp.setPasswordMinLowercase(nvl(moduleCfg.getPasswordMinLowercase()));
        resp.setPasswordMaxCommonSubstring(nvl(moduleCfg.getPasswordMaxCommonSubstring()));
        return resp;
    }

    private int nvl(Integer num) {
        return num != null ? num.intValue() : 0;
    }
}
