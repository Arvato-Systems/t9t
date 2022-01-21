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
package com.arvatosystems.t9t.zkui.viewmodel.support;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;

import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.auth.PermissionEntry;
import com.arvatosystems.t9t.zkui.exceptions.ReturnCodeException;
import com.arvatosystems.t9t.zkui.services.IUserDAO;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.util.ApplicationUtil;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.util.ZulUtils;

import de.jpaw.dp.Jdp;

/**
 * TenantDescription selection ViewModel.
 */
public class TenantSelectionViewModel {
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantSelectionViewModel.class);
    private static final String TENANT_COOKIE = "TENANT_COOKIE";

    private final IUserDAO userDAO = Jdp.getRequired(IUserDAO.class);

    private List<TenantDescription> tenantListModel = new ArrayList<>();
    private TenantDescription       selected;
    private boolean                 visible         = true;
    private List<TenantDescription> allowedTenants;
    private Boolean                 isCancelClose   = true;
    private boolean                 showLoginErrorMessage = false;

    @Init
    public void init() {
        final Boolean arg = (Boolean) Executions.getCurrent().getArg().get("isCancelClose");
        try {
            isCancelClose = arg == null ? Boolean.FALSE : arg;
        } catch (final Exception s) {
            s.printStackTrace();
        }
    }
    public TenantSelectionViewModel() {
        setSessionData();
        setListbox();
        setTenantFromCookie();

        if (tenantListModel != null && tenantListModel.size() == 1) {
            setVisible(false);
            selected = tenantListModel.get(0);
            redirect();
        }
    }

    private void setSessionData() {
        allowedTenants = ApplicationSession.get().getAllowedTenants();
    }

    /**
     * @return the visible
     */
    public final boolean isVisible() {
        return visible;
    }

    /**
     * @param visible the visible to set
     */
    public final void setVisible(final boolean visible) {
        this.visible = visible;
    }

    private void setListbox() {
        tenantListModel = allowedTenants;
    }

    /**
     * @return the tenantListModel
     */
    public final List<TenantDescription> getTenantListModel() {
        return tenantListModel;
    }

    /**
     * @param tenantListModel the tenantListModel to set
     */
    public final void setTenantListModel(final List<TenantDescription> tenantListModel) {
        this.tenantListModel = tenantListModel;
    }

    public final TenantDescription getSelected() {
        return selected;
    }

    public final void setSelected(final TenantDescription selected) {
        this.selected = selected;
    }

    /**
     * If no Tenant is selected logoff.
     */
    @NotifyChange("showLoginErrorMessage")
    @Command
    public final void redirect() {
        setShowLoginErrorMessage(false); // Reset error message flag on every attempt

        if (getSelected() == null) {
            throw new WrongValueException(ZulUtils.translate("tenant", "choose"));
        }

        ApplicationUtil.setCookie(TENANT_COOKIE, selected.getTenantId());

        try {
            if (tenantListModel != null && tenantListModel.size() > 1) {
                userDAO.switchTenant(selected.getTenantId());
            }
            final List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
            ApplicationSession.get().storePermissions(userPermissionForThisTenant);
        } catch (final ReturnCodeException e) {
            LOGGER.error("Unable to switch tenant or to get permissions " + e);
            Messagebox.show("Unable to switch tenant or to get permissions - " + e.getReturnCodeMessage() + ZulUtils.translate("err", "unableToSwitchTenant"),
                   ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
            setShowLoginErrorMessage(true);
            return;
        }

        final String additionalScreenConfig = ZulUtils.readConfig("login.additional.selections.qualifier");
        if (additionalScreenConfig == null) {
            Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME);
        } else {
            final String url = Constants.ZulFiles.ADDITIONAL_SELECTIONS + "?qualifier=" + additionalScreenConfig;
            LOGGER.info("redirecting to selections page with {} qualifier, complete url as {}", additionalScreenConfig, url);
            Executions.getCurrent().sendRedirect(url);
        }
    }

    void setTenantFromCookie() {
        if (tenantListModel != null) {
            final String cookieTenant  = ApplicationUtil.getCookie(TENANT_COOKIE);
            if (null != cookieTenant) {
                final int index = tenantListModel.stream()
                        .map(i -> i.getTenantId())
                        .collect(Collectors.toList())
                        .indexOf(cookieTenant);
                if (-1 != index) {
                    selected = tenantListModel.get(index);
                }
            }
        }
    }

    public Boolean getIsCancelClose() {
        return isCancelClose;
    }

    public void setIsCancelClose(final Boolean isCancelClose) {
        this.isCancelClose = isCancelClose;
    }

    public boolean isShowLoginErrorMessage() {
        return showLoginErrorMessage;
    }

    public void setShowLoginErrorMessage(final boolean showLoginErrorMessage) {
        this.showLoginErrorMessage = showLoginErrorMessage;
    }
}
