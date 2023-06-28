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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.QueryParam;
import org.zkoss.web.Attributes;
import org.zkoss.zul.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.dp.Jdp;

public class RedirectViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectViewModel.class);
    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    @Init
    public void init(@QueryParam("token") String token, @QueryParam("link") String link,
            @QueryParam("tenantId") String tenantId, @QueryParam("lang") String lang) {

        try {

            ApplicationSession.get().setJwt(token);
            userDAO.switchTenant(tenantId);

            if (lang != null) {
                Locale preferLocale = null;
                if (lang != null && lang.length() == 5) {
                    preferLocale = new Locale(lang.substring(0, 2), lang.substring(3, 5));
                } else {
                    preferLocale = new Locale(lang);
                }
                userDAO.switchLanguage(lang);
                Sessions.getCurrent().setAttribute(Attributes.PREFERRED_LOCALE, preferLocale);
            }

            List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
            ApplicationSession.get().storePermissions(userPermissionForThisTenant);

            String url = Constants.ZulFiles.HOME;
            if (link != null) {
                url += "?link=" + link;
            }
            Executions.getCurrent().sendRedirect(url);

        } catch (ReturnCodeException e) {
            LOGGER.error("Unable to switch tenant or to get permissions " + e);
            Messagebox.show("Unable to switch tenant or to get permissions - " + e.getReturnCodeMessage() + ZulUtils.translate("err", "unableToSwitchTenant"),
                    ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
            return;
        }
    }
}
