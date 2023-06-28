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
package com.arvatosystems.t9t.cfg.be.tests

import com.arvatosystems.t9t.cfg.be.ApplicationConfiguration
import com.arvatosystems.t9t.cfg.be.ConfigProvider
import com.arvatosystems.t9t.cfg.be.T9tServerConfiguration
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

class ConfigTest {

    @Disabled  // do not do file output in regression runs
    @Test
    def void writeConfig() {
        val cfg = ConfigProvider.configuration
        cfg.applicationConfiguration = new ApplicationConfiguration => [
            workerPoolSize = 40
            maxWorkerExecuteTime = 43200
        ]
        ConfigProvider.configToFile(cfg, "/tmp/t9tconfig.xml");   // the file in user's home is ~/.t9tconfig.xml
    }

    // example for an AWS RDS DB URL
    val testCfg = '''
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <T9tServerConfiguration xmlns="http://arvatosystems.com/schema/t9t_cfg_be.xsd">
            <persistenceUnitName>t9t-DS</persistenceUnitName>
            <databaseConfiguration>
                <username>fortytwo</username>
                <password>secret</password>
                <databaseBrand>POSTGRES</databaseBrand>
                <jdbcConnectString>jdbc:postgresql://t9t.cxxssfrkkvid.eu-central-1.rds.amazonaws.com:5432/t9tdb</jdbcConnectString>
            </databaseConfiguration>
            <awsConfiguration>
                <snsEndpoint>https://sns.eu-central-1.amazonaws.com</snsEndpoint>
                <sqsEndpoint>https://sqs.eu-central-1.amazonaws.com</sqsEndpoint>
            </awsConfiguration>
        </T9tServerConfiguration>
    '''

    @Test
    def void readConfig() {
        val cfg = ConfigProvider.configFromString(testCfg)
        assertNotNull(cfg)
        assertEquals(T9tServerConfiguration, cfg.class)
        assertNotNull(cfg.databaseConfiguration)
        assertNotNull(cfg.awsConfiguration)
        assertNull(cfg.bpm2Configuration)

        assertEquals("secret", cfg.databaseConfiguration.password)
    }
}
