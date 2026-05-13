/*
 * Copyright (c) 2012 - 2026 Arvato Systems GmbH
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
package com.arvatosystems.t9t.auth.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

import com.arvatosystems.t9t.cfg.be.ConfigProvider;
import com.arvatosystems.t9t.cfg.be.ServerConfiguration;

/**
 * Startup initializer that reads the keystore configuration and applies it to both the primary
 * {@link JWT} and the secondary {@link SecondaryJWT}.
 * Running at priority 5 (after InitCfgBe at 0).
 */
@Startup(5)
public final class JwtKeystoreInitializer implements StartupOnly {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtKeystoreInitializer.class);

    @Override
    public void onStartup() {
        final ServerConfiguration serverCfg = ConfigProvider.getConfiguration().getServerConfiguration();

        // Initialize primary keystore
        if (serverCfg != null && (serverCfg.getKeyStorePassword() != null || serverCfg.getKeyStorePath() != null)) {
            JWT.setKeyStore(serverCfg.getKeyStorePath(), serverCfg.getKeyStorePassword(), serverCfg.getKeyStorePath() == null);
            LOGGER.info("Using environment specific keystore and/or password from local config file. Good.");
        } else {
            LOGGER.warn("No environment specific keystore parameters. Using defaults, do not use this in production!");
        }

        // Initialize secondary (issuer-based) keystore
        if (serverCfg != null && serverCfg.getSecondaryKeyStorePath() != null) {
            SecondaryJWT.setKeyStore(serverCfg.getSecondaryKeyStorePath(), serverCfg.getSecondaryKeyStorePassword());
            LOGGER.info("Secondary (issuer-based) keystore configured from local config file.");
        } else {
            LOGGER.trace("No secondary keystore parameters configured. External JWT validation via issuer keystore not available.");
        }
    }
}

