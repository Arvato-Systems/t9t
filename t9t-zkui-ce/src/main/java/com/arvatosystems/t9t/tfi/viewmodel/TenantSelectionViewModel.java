/*
 * Copyright (c) 2012 - 2018 Arvato Systems GmbH
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;

import com.arvatosystems.t9t.tfi.general.ApplicationUtil;
import com.arvatosystems.t9t.tfi.general.Constants;
import com.arvatosystems.t9t.tfi.services.IUserDAO;
import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;
import com.arvatosystems.t9t.tfi.web.shiro.ICacheableAuthorizationRealm;
import com.arvatosystems.t9t.authc.api.TenantDescription;
import com.arvatosystems.t9t.base.auth.PermissionEntry;

import de.jpaw.dp.Jdp;

/**
 * TenantDescription selection ViewModel.
 * @author INCI02
 */
public class TenantSelectionViewModel {
    private List<TenantDescription> tenantListModel = new ArrayList<TenantDescription>();
    private TenantDescription       selected;
    private boolean      visible          = true;
    private List<TenantDescription> allowedTenants;
    private static final Logger LOGGER = LoggerFactory.getLogger(TenantSelectionViewModel.class);
    private static final String TENANT_COOKIE                     = "TENANT_COOKIE";
    private final IUserDAO     userDAO = Jdp.getRequired(IUserDAO.class);
    private Boolean isCancelClose = true;

    @Init
    public void init() {
        Boolean arg = (Boolean) Executions.getCurrent().getArg().get("isCancelClose");
        try{
            isCancelClose = arg == null ? new Boolean(false) : arg;
        }catch(Exception s){
            s.printStackTrace();
        }
    }
    public TenantSelectionViewModel() {
        setSessionData();
        setListbox();
        setTenantFromCookie();

        if ((tenantListModel != null) && (tenantListModel.size() == 1)) {
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
    public final void setVisible(boolean visible) {
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
    public final void setTenantListModel(List<TenantDescription> tenantListModel) {
        this.tenantListModel = tenantListModel;
    }

    public final TenantDescription getSelected() {
        return selected;
    }

    public final void setSelected(TenantDescription selected) {
        this.selected = selected;
    }

    /**
     * If no Tenant is selected logoff.
     */
    @Command
    public final void redirect() {
        if (getSelected() == null) {
            throw new WrongValueException(ZulUtils.translate("tenant", "choose"));
        }

        ApplicationUtil.setCookie(TENANT_COOKIE, selected.getTenantId());

        try {
            if ((tenantListModel != null) && (tenantListModel.size() > 1)) {
                userDAO.switchTenant(selected.getTenantId());
            }
            setTenantInfo();
            List<PermissionEntry> userPermissionForThisTenant = userDAO.getPermissions();
            ApplicationSession.get().storePermissions(userPermissionForThisTenant);
        } catch (ReturnCodeException e) {
           LOGGER.error("Unable to switch tenant or to get permissions " + e);
           Messagebox.show("Unable to switch tenant or to get permissions - " + e.getReturnCodeMessage() + ZulUtils.translate("err", "unableToSwitchTenant"), ZulUtils.translate("err", "title"), Messagebox.OK, Messagebox.ERROR);
           return;
        }
        Executions.getCurrent().sendRedirect(Constants.ZulFiles.HOME);
    }

    private void setTenantInfo() throws ReturnCodeException {
        LOGGER.info("setTenantInfo(): Not sure why we have to set data in ApplicationSession here ... probably not required, commenting out");

        // clear authorization realms
        RealmSecurityManager securityManager = (RealmSecurityManager) SecurityUtils.getSecurityManager();
        for (Realm realm : securityManager.getRealms()) {
            if (realm instanceof ICacheableAuthorizationRealm) {
                ((ICacheableAuthorizationRealm) realm).clearAuthorizationCache();
            }
        }
    }

    void setTenantFromCookie() {
        if (tenantListModel != null) {
            String cookieTenant  =        ApplicationUtil.getCookie(TENANT_COOKIE);
            if (null != cookieTenant) {
                int index = tenantListModel.stream()
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

    public void setIsCancelClose(Boolean isCancelClose) {
        this.isCancelClose = isCancelClose;
    }
}
