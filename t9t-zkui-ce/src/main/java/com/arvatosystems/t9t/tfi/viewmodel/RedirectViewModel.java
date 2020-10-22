/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.viewmodel;

import java.util.List;
import java.util.Locale;

import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.web.Attributes;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.arvatosystems.t9t.tfi.web.security.JwtUtils;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.dp.Jdp;

public class RedirectViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectViewModel.class);
    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);
    private boolean showLogoutButton;

    @Init
    public void init(@QueryParam("token") String token, @QueryParam("link") String link,
            @QueryParam("tenantId") String tenantId, @QueryParam("lang") String lang) {

        try {

            if (inconsistenceUserLoggedIn(token)) {
                showLogoutButton = true;
                Messagebox.show(ZulUtils.translate("redirect", "inconsistenceUserLoggedIn"), ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
                return;
            }

            ApplicationSession.get().setJwt(token);
            userDAO.switchTenant(tenantId);

            if (lang != null) {
                Locale prefer_locale = null;
                if(lang!=null && lang.length()==5){
                    prefer_locale = new Locale(lang.substring(0,2),lang.substring(3,5));
                }else{
                    prefer_locale = new Locale(lang);
                }
                userDAO.switchLanguage(lang);
                Sessions.getCurrent().setAttribute(Attributes.PREFERRED_LOCALE, prefer_locale);
            }

            List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
            ApplicationSession.get().storePermissions(userPermissionForThisTenant);
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME + "?link=" + link);

        } catch (ReturnCodeException e) {
            LOGGER.error("Unable to switch tenant or to get permissions " + e);
            Messagebox.show("Unable to switch tenant or to get permissions - " + e.getReturnCodeMessage() + ZulUtils.translate("err", "unableToSwitchTenant"), ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
            return;
        }
    }

    private boolean inconsistenceUserLoggedIn(String token) {
        JwtInfo jwtInfo = JwtUtils.getJwtPayload(token);
        if (SecurityUtils.getSubject() != null && SecurityUtils.getSubject().isAuthenticated()
                && !jwtInfo.getUserId().equals(SecurityUtils.getSubject().getPrincipal())) {
            return true;
        }
        return false;
    }

    public boolean isShowLogoutButton() {
        return showLogoutButton;
    }

    public void setShowLogoutButton(boolean showLogoutButton) {
        this.showLogoutButton = showLogoutButton;
    }
}
