/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
