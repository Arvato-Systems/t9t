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
package com.arvatosystems.t9t.plugins.demo;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.plugins.PluginInfo;
import com.arvatosystems.t9t.plugins.PluginMethodInfo;
import com.arvatosystems.t9t.plugins.services.Plugin;

public class Main implements Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private static final String PLUGIN_ID = "demo"; // PLUGIN_ID
    private static final String MAJOR = "1"; // MAJOR
    private static final String MINOR = "0"; // MINOR

    public static void main(String[] args) {
        System.out.println("t9t Demo plugin version 1.0");
    }

    /** Retrieves information about the plugin. */
    @Override
    public PluginInfo getInfo() {
        LOGGER.debug("PluginInfo: PluginID = {} | Version = {}.{}", PLUGIN_ID, MAJOR, MINOR);
        final PluginInfo info = new PluginInfo();
        info.setPluginId(PLUGIN_ID);
        info.setVersion(MAJOR + "." + MINOR);
        return info;
    }

    /** get the list of implemented methods. Never returns null. */
    @Override
    public List<PluginMethodInfo> getMethods() {
//        final PluginMethodInfo method1 = new PluginMethodInfo();
//        method1.setImplementsApi("test");
//        method1.setQualifier("HelloWorld");
//        method1.setVersionMajor(1);
//        method1.setVersionMinMinor(0);
//
//        final List<PluginMethodInfo> list = new ArrayList<PluginMethodInfo>();
//        list.add(method1);
//
//        return list;
        return Collections.singletonList(new DemoRequestHandler().getInfo());
    }

    /** Performs a cleanup, before the plugin is unloaded. */
    @Override
    public void shutdown() {
        LOGGER.debug("demoPlugin Main shutdown");
    }

    /** Starts the plugin (instantiates implementations). */
    @Override
    public void startup() {
        LOGGER.debug("demoPlugin Main startup");
    }
}
