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
package com.arvatosystems.t9t.plugins.be.impl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.PluginInfo;
import com.arvatosystems.t9t.plugins.StoredPluginKey;
import com.arvatosystems.t9t.plugins.StoredPluginMethodKey;
import com.arvatosystems.t9t.plugins.services.IPluginManager;
import com.arvatosystems.t9t.plugins.services.IPluginMethodLifecycle;
import com.arvatosystems.t9t.plugins.services.Plugin;
import com.arvatosystems.t9t.plugins.services.PluginMethod;
import com.google.common.io.Files;

import de.jpaw.dp.Jdp;
import de.jpaw.dp.Provider;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class PluginManager implements IPluginManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);
    private final Provider<RequestContext> ctxProvider = Jdp.getProvider(RequestContext.class);

    private static class LoadedPlugin {
        public final Plugin         loadedClass;
        public final URLClassLoader classloader;

        LoadedPlugin(final Plugin loadedClass, final URLClassLoader classloader) {
            this.loadedClass = loadedClass;
            this.classloader = classloader;
        }
    }

    private final Object lock = new Object();

    private final ConcurrentMap<StoredPluginKey,       LoadedPlugin> loadedPlugins = new ConcurrentHashMap<>();
    private final ConcurrentMap<StoredPluginMethodKey, PluginMethod> loadedPluginMethods = new ConcurrentHashMap<>();

    protected String getMainClassFromManifest(final JarFile jar) throws IOException {
        final Manifest manifest = jar.getManifest();
        if (manifest != null) {
            final Attributes attrs = manifest.getMainAttributes();
            if (attrs != null) {
                final Object mc = attrs.get("Main-Class");
                if (mc != null) {
                    LOGGER.debug("Using Main class {} due to MANIFEST directive", mc);
                    return mc.toString();
                }
            }
        }
        return null;
    }

    protected String getMainClassByNamingConvention(final JarFile jar) {
        final Enumeration<JarEntry> jes = jar.entries();
        while (jes.hasMoreElements()) {
            final JarEntry je = jes.nextElement();
            if (!je.isDirectory()) {
                //LOGGER.debug("Found JAR entry {}", je.getName());
                if (je.getName().endsWith("/Main.class")) {
                    // -6 because of .class
                    final String main = je.getName().substring(0, je.getName().length() - 6).replace('/', '.');
                    LOGGER.debug("Using Main class {} due to match of naming convention scan", main);
                    return main;
                }
            }
        }
        return null;
    }

    protected void registerPluginMethods(final String tenantId, final Plugin loadedPlugin, final boolean unload) {
        for (final PluginMethod pm: loadedPlugin.getMethods()) {
            final StoredPluginMethodKey key = new StoredPluginMethodKey(tenantId, pm.implementsApi(), pm.getQualifier());
            final IPluginMethodLifecycle lifecycle = Jdp.getRequired(IPluginMethodLifecycle.class, pm.getQualifier());
            if (unload) {
                lifecycle.unregisterPluginMethod(tenantId, loadedPlugin, pm, true);
                loadedPluginMethods.remove(key, pm);
                lifecycle.unregisterPluginMethod(tenantId, loadedPlugin, pm, false);
            } else {
                lifecycle.registerPluginMethod(tenantId, loadedPlugin, pm, true);
                loadedPluginMethods.put(key, pm);
                lifecycle.registerPluginMethod(tenantId, loadedPlugin, pm, false);
            }
        }
    }

    @Override
    public PluginInfo loadPlugin(final String tenantId, final ByteArray pluginData) {
        final Class pluginClass;
        final URLClassLoader cl;
        try {
            // store the binary data in a temp file
            final File file = File.createTempFile("t9t-plugin-", ".jar");
            LOGGER.info("Created tmp file at {}", file.getAbsolutePath());
            Files.write(pluginData.getBytes(), file);

            // reopen it as a JAR file for analysis
            final JarFile jar = new JarFile(file);
            String mainClassName = getMainClassFromManifest(jar);
            if (mainClassName == null) {
                mainClassName = getMainClassByNamingConvention(jar);
                if (mainClassName == null) {
                    throw new T9tException(T9tException.NO_MAIN_IN_PLUGIN);
                }
            }

            final URL[] urls = { new URL("jar:file:" + file.getAbsolutePath() + "!/") };
            cl = new URLClassLoader(urls, this.getClass().getClassLoader());

            LOGGER.debug("loading MAIN {}", mainClassName);
            pluginClass = cl.loadClass(mainClassName);
            if (!Plugin.class.isAssignableFrom(pluginClass)) {
                throw new T9tException(T9tException.MAIN_IS_NOT_PLUGIN);
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new T9tException(T9tException.PLUGIN_LOADING_ERROR, ExceptionUtil.causeChain(e));
        }

        try {
            final Constructor<?> constructor = pluginClass.getConstructor();
            final Plugin loadedPlugin = (Plugin) constructor.newInstance();
            final PluginInfo info = loadedPlugin.getInfo();
            // now everything which could go wrong has succeeded!
            // if a previous plugin was registered, kick it out, replace it with the new one.
            final StoredPluginKey key = new StoredPluginKey(tenantId, info.getPluginId());
            synchronized (lock) {
                final LoadedPlugin previousPlugin = loadedPlugins.get(key);
                if (previousPlugin != null) {
                    // remove the method entries
                    registerPluginMethods(tenantId, previousPlugin.loadedClass, true);
                    // now also unload the plugin
                    previousPlugin.loadedClass.shutdown();
                    try {
                        previousPlugin.classloader.close();
                    } catch (final IOException e) {
                        LOGGER.warn("Problem closing classloader: {}", ExceptionUtil.causeChain(e));
                    }
                }
                loadedPlugins.put(key, new LoadedPlugin(loadedPlugin, cl));
                registerPluginMethods(tenantId, loadedPlugin, false);
            }
            return info;
        } catch (final Throwable e) {
            throw new T9tException(T9tException.PLUGIN_INSTANTIATION_ERROR, ExceptionUtil.causeChain(e));
        }
    }

    @Override
    public <R extends PluginMethod> R getPluginMethod(final String pluginApiId, final String qualifier, final Class<R> requiredType, final boolean allowNulls) {
        return getPluginMethod(ctxProvider.get().tenantId, pluginApiId, qualifier, requiredType, allowNulls);
    }

    @Override
    public <R extends PluginMethod> R getPluginMethod(final String tenantId, final String pluginApiId, final String qualifier,
      final Class<R> requiredType, final boolean allowNulls) {
        final StoredPluginMethodKey key = new StoredPluginMethodKey(tenantId, pluginApiId, qualifier);
        PluginMethod method = loadedPluginMethods.get(key);
        if (method == null && !tenantId.equals(T9tConstants.GLOBAL_TENANT_ID)) {
            // perform another attempt using the global tenant
            final StoredPluginMethodKey key2 = new StoredPluginMethodKey(T9tConstants.GLOBAL_TENANT_ID, pluginApiId, qualifier);
            method = loadedPluginMethods.get(key2);
        }
        if (method == null) {
            if (allowNulls) {
                return null;
            }
            throw new T9tException(T9tException.NO_PLUGIN_METHOD_AVAILABLE, pluginApiId + ":" + qualifier);
        }
        // check its type
        if (!requiredType.isAssignableFrom(method.getClass())) {
            LOGGER.error("Expected type {} in plugin {}:{}, but got type {}",
              requiredType.getCanonicalName(), pluginApiId, qualifier, method.getClass().getCanonicalName());
            throw new T9tException(T9tException.PLUGIN_METHOD_WRONG_TYPE, pluginApiId + ":" + qualifier);
        }
        return (R)method;
    }


    @Override
    public boolean removePlugin(final String tenantId, final String pluginId) {
        final StoredPluginKey key = new StoredPluginKey(tenantId, pluginId);
        synchronized (lock) {
            final LoadedPlugin previousPlugin = loadedPlugins.get(key);
            if (previousPlugin != null) {
                // remove the method entries
                registerPluginMethods(tenantId, previousPlugin.loadedClass, true);
                // now also unload the plugin
                previousPlugin.loadedClass.shutdown();
                loadedPlugins.remove(key);
                try {
                    previousPlugin.classloader.close();
                } catch (final IOException e) {
                    LOGGER.warn("Problem closing classloader: {}", ExceptionUtil.causeChain(e));
                }
                return true;
            }
        }
        return false;
    }
}
