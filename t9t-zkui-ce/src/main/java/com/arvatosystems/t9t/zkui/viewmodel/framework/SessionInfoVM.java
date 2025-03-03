/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsRequest;
import com.arvatosystems.t9t.monitoring.request.QuerySystemParamsResponse;
import com.arvatosystems.t9t.zkui.services.IT9tRemoteUtils;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.viewmodel.GenericVM;

import de.jpaw.dp.Jdp;

@Init(superclass = true)
public class SessionInfoVM extends GenericVM {

    private final IT9tRemoteUtils remoteUtils = Jdp.getRequired(IT9tRemoteUtils.class);
    private final ApplicationSession applicationSession = ApplicationSession.get();
    private final RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
    private final Runtime rt = Runtime.getRuntime();
    private QuerySystemParamsResponse backendSystemParams;
    private QuerySystemParamsResponse uiSystemParams;

    @AfterCompose
    public void afterCompose() {
        fillBackendSystemParams();
        fillUiSystemParams();
    }

    @Command
    @NotifyChange({ "currentDateTime", "uiUptime", "uiTotalMemory", "uiFreeMemory", "uiMaxMemory", "backendUptime", "backendTotalMemory", "backendFreeMemory",
            "backendMaxMemory" })
    public void refresh() {
        fillBackendSystemParams();
        fillUiSystemParams();
    }

    /**
     *
     * @return tenantId from JwtInfo
     */
    @Override
    public String getTenantId() {

        return applicationSession.getJwtInfo().getTenantId();
    }

    /**
     *
     * @return userId from JwtInfo
     */
    public String getUserId() {

        return applicationSession.getJwtInfo().getUserId();
    }

    /**
     *
     * @return currentLocale from JwtInfo
     */
    public String getCurrentLocale() {

        return applicationSession.getJwtInfo().getLocale();
    }

    /**
     *
     * @return currentTimeZone from JwtInfo
     */
    public String getCurrentTimeZone() {

        return applicationSession.getJwtInfo().getZoneinfo();
    }

    /**
     *
     * @return
     */
    public LocalDateTime getCurrentDateTime() {

        return LocalDateTime.now();
    }

    /**
     *
     * @return uiUptime
     */
    public String getUiUptime() {

        return getFormattedUptime(Duration.ofMillis(uiSystemParams.getJvmUptimeInMillis()));
    }

    /**
     *
     * @return uiTotalMemory
     */
    public Long getUiTotalMemory() {

        return getFormattedMemory(uiSystemParams.getTotalMemory());
    }

    /**
     *
     * @return uiFreeMemory
     */
    public Long getUiFreeMemory() {

        return getFormattedMemory(uiSystemParams.getFreeMemory());
    }

    /**
     *
     * @return uiMaxMemory
     */
    public Long getUiMaxMemory() {

        return getFormattedMemory(uiSystemParams.getMaxMemory());
    }

    /**
    *
    * @return backendUptime
    */
    public String getBackendUptime() {

        return getFormattedUptime(Duration.ofMillis(backendSystemParams.getJvmUptimeInMillis()));
    }

    /**
    *
    * @return backendTotalMemory
    */
    public Long getBackendTotalMemory() {

        return getFormattedMemory(backendSystemParams.getTotalMemory());
    }

    /**
    *
    * @return backendFreeMemory
    */
    public Long getBackendFreeMemory() {

        return getFormattedMemory(backendSystemParams.getFreeMemory());
    }

    /**
    *
    * @return backendMaxMemory
    */
    public Long getBackendMaxMemory() {

        return getFormattedMemory(backendSystemParams.getMaxMemory());
    }

    /**
     *
     * @param memory
     * @return formattedMemory
     */
    private Long getFormattedMemory(final long memory) {

        return memory / (1024L * 1024L);
    }

    /**
     *
     * @param dur
     * @return formattedUptime
     */
    private String getFormattedUptime(final Duration dur) {
        return String.format("%d, %02d:%02d:%02d", dur.toDays(), dur.toHoursPart(), dur.toMinutesPart(), dur.toSecondsPart());
    }

    /**
     * @return the backend server's system parameters.
     */
    public QuerySystemParamsResponse getBackendSystemParams() {
        return backendSystemParams;
    }

    /**
     * @return the ui server's system parameters.
     */
    public QuerySystemParamsResponse getUiSystemParams() {
        return uiSystemParams;
    }

    /**
     * update backendSystemParams
     */
    private void fillBackendSystemParams() {
        backendSystemParams = remoteUtils.executeExpectOk(new QuerySystemParamsRequest(), QuerySystemParamsResponse.class);
    }

    /**
     * create / update uiSystemParams
     */
    private void fillUiSystemParams() {

        if (uiSystemParams == null) {
            uiSystemParams = new QuerySystemParamsResponse();
        }

        uiSystemParams.setCurrentTimeMillis(System.currentTimeMillis());
        uiSystemParams.setAvailableProcessors(rt.availableProcessors());
        uiSystemParams.setTotalMemory(rt.totalMemory());
        uiSystemParams.setFreeMemory(rt.freeMemory());
        uiSystemParams.setMaxMemory(rt.maxMemory());
        uiSystemParams.setHostname(MessagingUtil.HOSTNAME);

        uiSystemParams.setJvmUptimeInMillis(runtimeBean.getUptime());
        uiSystemParams.setName(runtimeBean.getName());
        uiSystemParams.setVmName(runtimeBean.getVmName());
        uiSystemParams.setVmVendor(runtimeBean.getVmVendor());
        uiSystemParams.setVmVersion(runtimeBean.getVmVersion());
        uiSystemParams.setSpecName(runtimeBean.getSpecName());
        uiSystemParams.setSpecVendor(runtimeBean.getSpecVendor());
        uiSystemParams.setSpecVersion(runtimeBean.getSpecVersion());
    }

}
