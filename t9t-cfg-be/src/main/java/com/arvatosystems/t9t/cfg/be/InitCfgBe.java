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
package com.arvatosystems.t9t.cfg.be;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Startup;
import de.jpaw.dp.StartupOnly;

@Startup(0)
public final class InitCfgBe implements StartupOnly {

    // started by Jdp initialization
    @Override
    public void onStartup() {
        Jdp.bind(ConfigProvider.getConfiguration());
    }
}
