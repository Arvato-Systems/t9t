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
package com.arvatosystems.t9t.rep.services.impl;

import java.util.Map;

import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider;
import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration;
import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.rep.ReportParamsDTO;
import com.arvatosystems.t9t.rep.be.IJasperReportFiller;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Singleton
public class T9tJasperReportFiller implements IJasperReportFiller {
    protected final ApplicationConfiguration applConfig = ConfigProvider.getConfiguration().getApplicationConfiguration();
    protected final boolean useShadowDatabase = applConfig != null && Boolean.TRUE.equals(applConfig.getUseShadowDatabaseForReports());
    protected final IJdbcConnectionProvider jdbcProvider = useShadowDatabase
            ? Jdp.getRequired(IJdbcConnectionProvider.class, "shadow")
            : Jdp.getRequired(IJdbcConnectionProvider.class);

    @Override
    public JasperPrint fillReport(final JasperReport jasperReport, final ReportParamsDTO reportParamsDTO,
      final Map<String, Object> parameters) throws JRException {
        return JasperFillManager.fillReport(jasperReport, parameters, jdbcProvider.getJDBCConnection());
    }
}
