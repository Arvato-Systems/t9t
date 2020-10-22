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
package com.arvatosystems.t9t.plugins.be.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import de.jpaw.annotations.AddLogger;

/**
 * Defines the classloader for the plugins
 **/
@AddLogger
public class PluginClassloader extends URLClassLoader{
    public PluginClassloader(URL[] urls) {
        super(urls);
    }

    public PluginClassloader(File file, ClassLoader parentClassloader) {
        super(new URL[] {fileToURL(file)}, parentClassloader);
    }

    public static URL fileToURL(File file) {
        URL url = null;
        try {
            url = file.toURI().toURL();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return url;
    }

    /** load class with name className **/
    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException{
        synchronized (getClassLoadingLock(className)) {
            // check if already loaded
            Class<?> pluginClass = findLoadedClass(className);
            if(pluginClass != null) {
                return pluginClass;
            }

            // try to load from classpath
            try {
                pluginClass = findClass(className);
                return pluginClass;
            } catch (ClassNotFoundException e) {
                // do nothing now and proceed with next step
            }

            // use standard ClassLoader
            return super.loadClass(className);
        }
    }
}
