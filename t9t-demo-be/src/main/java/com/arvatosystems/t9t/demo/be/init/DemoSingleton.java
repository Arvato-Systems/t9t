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
package com.arvatosystems.t9t.demo.be.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Singleton;

@Singleton
public class DemoSingleton implements IDemo {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemoSingleton.class);

    static {
        LOGGER.info("Demo singleton class initialized");
    }

    public DemoSingleton() {
        LOGGER.info("Demo singleton class instantiated");
    }

    @Override
    public void doNothing() {
    }

}
