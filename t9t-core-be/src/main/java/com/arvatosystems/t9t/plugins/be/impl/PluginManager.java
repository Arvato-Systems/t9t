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
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.plugins.LoadedPluginDTO;
import com.arvatosystems.t9t.plugins.PluginMethodInfo;
import com.arvatosystems.t9t.plugins.request.LoadedPluginSearchRequest;
import com.arvatosystems.t9t.plugins.services.Plugin;
import com.arvatosystems.t9t.plugins.services.IPluginManager;
import com.arvatosystems.t9t.plugins.services.PluginMethod;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SortColumn;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ExceptionUtil;

@Singleton
public class PluginManager implements IPluginManager{

    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);

    private ConcurrentMap<String, LoadedClass<Plugin>> loadedPlugins = new ConcurrentHashMap<String, LoadedClass<Plugin>>();
    private ConcurrentMap<String, LoadedClass<PluginMethod>> loadedPluginMethods = new ConcurrentHashMap<String, LoadedClass<PluginMethod>>();

    private static final String PLUGIN_PACKAGE_PREFIX = "com.arvatosystems.t9t.plugins.";
    private static final String PLUGIN_MAIN= ".Main";
    private static final String NO_QUALIFIER = "";
    private static final String PLUGIN_FILE_TYPE = ".jar";
    private static final String PLUGIN_STORAGE_PATH = "\\plugins\\";

    private String sRootPath = new File("").getAbsolutePath();

    public PluginManager() {
       this.initPlugins();
    }

    /**
     * Load Plugin and provided PluginMethods and store them internally.
     * @param path Path to Jar-File
     * @param tenantRef tenantRef
     * @param pluginId ID of plugin
     * @return Loaded Plugin
     * @throws ClassNotFoundException
     */
    @Override
    public Plugin loadPlugin(String path, Long tenantRef, String pluginId) throws ClassNotFoundException {
        Plugin loadedPlugin;
        // create File with given path
        File newPlugin = getFile(path);

        // create classloader and get plugin class
        PluginClassloader classloader = getClassloader(pluginId, newPlugin);
        Class<?> plugin = classloader.loadClass(PLUGIN_PACKAGE_PREFIX + pluginId +  PLUGIN_MAIN);

        try {
            Constructor<?> constructor = plugin.getConstructor();
            loadedPlugin = (Plugin) constructor.newInstance();

            loadedPlugins.put(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN, new LoadedClass<Plugin>(loadedPlugin ,classloader));
            // execute startup routine of plugin
            loadedPlugin.startup();

            this.loadImplementedMethods(loadedPlugin, path, tenantRef);

            return loadedPlugin;
        } catch (Exception e) {
            LOGGER.error("Exception caught while loading plugin {} from path {}", pluginId, path, ExceptionUtil.causeChain(e));
        }

        return null;
    }

    private void loadImplementedMethods(Plugin plugin, String path, Long tenantRef) throws ClassNotFoundException{
        List<PluginMethodInfo> methodInfo = plugin.getMethods();

        for(PluginMethodInfo pluginMethod: methodInfo) {
            this.loadPluginMethod(tenantRef, path, pluginMethod.getImplementsApi(), pluginMethod.getQualifier());
        }
    }

    private PluginMethod loadPluginMethod(Long tenantRef, String path, String pluginId) throws ClassNotFoundException{
        return loadPluginMethod(tenantRef, path, pluginId, NO_QUALIFIER);
    }

    private PluginMethod loadPluginMethod(Long tenantRef, String path, String pluginId, String qualifier) throws ClassNotFoundException{
        PluginMethod loadedPluginMethod;

        // create File with given path
        File newPlugin = getFile(path);

        // create classloader and get plugin class
        PluginClassloader classloader = getClassloader(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN, newPlugin);

        Class<?> plugin = classloader.loadClass(PLUGIN_PACKAGE_PREFIX + pluginId + ((qualifier == null || qualifier == NO_QUALIFIER) ? NO_QUALIFIER : ("." + qualifier)));

        try {
            Constructor<?> constructor = plugin.getConstructor();
            loadedPluginMethod = (PluginMethod) constructor.newInstance();

            loadedPluginMethods.put(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + ((qualifier == null || qualifier == NO_QUALIFIER) ? NO_QUALIFIER : ("." + qualifier)), new LoadedClass<PluginMethod>(loadedPluginMethod ,classloader));

            LOGGER.info("PluginMethod {}.{} was loaded from path {} for tenant {}", pluginId, qualifier, path, tenantRef);
            return loadedPluginMethod;
        } catch (Exception e) {
            LOGGER.error("Exception caught while loading plugin method {}.{} from path {}", pluginId, qualifier, path, ExceptionUtil.causeChain(e));
        }

        LOGGER.error("PluginMethod {}.{} could not be loaded from path {}", pluginId, qualifier, path);
        return null;
    }

    @Override
    public boolean closePlugin(Long tenantRef, String pluginId) {
        if(loadedPlugins.containsKey(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN)) {
             LOGGER.debug("Closing Plugin {} for tenant {}", pluginId, tenantRef);
             LoadedClass<Plugin> loadedClassWrapper = loadedPlugins.get(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN);
             try {

                // remove loaded PluginMethods from Map
                for (PluginMethodInfo pluginMethod: ((Plugin)(loadedClassWrapper.loadedClass)).getMethods()) {
                    loadedPluginMethods.remove(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + ((pluginMethod.getQualifier() != null) ? NO_QUALIFIER : ("." + pluginMethod.getQualifier())));
                }

                // execute shutdown routine of Plugin (if loadedClass is of type Plugin)
                if(loadedClassWrapper.loadedClass instanceof Plugin) {
                    ((Plugin)(loadedClassWrapper.loadedClass)).shutdown();
                }

                loadedClassWrapper.classloader.close();

                // after shutdown of plugin and closing of classloader, remove loaded class from loadedPlugins map
                loadedPlugins.remove(pluginId);
                return true;
            } catch (IOException e) {
                LOGGER.error("Exception caught while closing plugin {}", pluginId, ExceptionUtil.causeChain(e));
            }
        }
        return false;
    }

    @Override
    public Plugin getPlugin(Long tenantRef, String pluginId) throws T9tException {
        if (loadedPlugins.containsKey(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN)) {
            LoadedClass<Plugin> loadedClassWrapper = loadedPlugins.get(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN);

            return (Plugin)(loadedClassWrapper.loadedClass);
        } else {
            throw new T9tException(T9tException.NO_PLUGIN_AVAILABLE);
        }
    }

    @Override
    public PluginMethod getPluginMethod(Long tenantRef, String pluginId) throws T9tException {
        return getPluginMethod(tenantRef, pluginId, NO_QUALIFIER);
    }

    @Override
    public PluginMethod getPluginMethod(Long tenantRef, String pluginId, String qualifier) throws T9tException {
        // if pluginMethod is not loaded yet, try to load it with corresponding plugin
        if (!loadedPluginMethods.containsKey(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + ((qualifier == NO_QUALIFIER) ? NO_QUALIFIER : ("." + qualifier)))) {
            this.initPlugin(tenantRef, pluginId);
        }

        // check if currently loaded version is active in database, otherwise load currently active version
        LoadedClass<Plugin> loadedPlugin = loadedPlugins.get((((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + PLUGIN_MAIN));
        if (loadedPlugin != null && this.compareVersions(loadedPlugin.loadedClass.getInfo().getVersion(), getPluginVersionFromDB(tenantRef, pluginId)) != 0) {
            this.updatePlugin(tenantRef, pluginId);
        }

        // return plugin method if it is in the PluginMethod list, otherwise throw T9tException
        if (loadedPluginMethods.containsKey(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + ((qualifier == NO_QUALIFIER) ? NO_QUALIFIER : ("." + qualifier)))) {
            LoadedClass<PluginMethod> loadedClassWrapper = loadedPluginMethods.get(((tenantRef != null) ? (tenantRef.toString() + ".") : "") + pluginId + ((qualifier == NO_QUALIFIER) ? NO_QUALIFIER : ("." + qualifier)));

            return (PluginMethod)(loadedClassWrapper.loadedClass);
        } else {
            // load plugin
            LOGGER.error("Failed loading plugin {} with qualifier {} for tenant {}", pluginId, qualifier, tenantRef);
            throw new T9tException(T9tException.NO_PLUGIN_AVAILABLE);
        }
    }

    private void updatePlugin(Long tenantRef, String pluginId) {
        // remove inactive plugin
        this.closePlugin(tenantRef, pluginId);

        // init active plugin
        this.initPlugin(tenantRef, pluginId);
    }


    private void initPlugins() {
        // initialize plugins, if list of loaded plugins is empty
        if (this.loadedPlugins.isEmpty()) {
            List<DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion>> loadedPlugins = retrievePluginsFromDatabase();

            for(DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion> dataWT : loadedPlugins) {
                File jarFile = null;
                try {
                    // download loadedPlugin
                    jarFile = this.storeFile(dataWT.getData().getPluginId(), dataWT.getData().getPluginVersion(), dataWT.getTenantRef(), dataWT.getData().getJarFile().getBytes());
                    // load Plugin
                    this.loadPlugin(jarFile.getAbsolutePath(), dataWT.getTenantRef(), dataWT.getData().getPluginId());
                } catch (IOException e) {
                    LOGGER.error("Failed to store jar archive for Plugin {} for Tenant {}", dataWT.getData().getPluginId(), dataWT.getTenantRef());
                }  catch (ClassNotFoundException e) {
                    LOGGER.error("Plugin {} for Tenant {} was not found at location {}", dataWT.getData().getPluginId(), dataWT.getTenantRef(), jarFile.getAbsolutePath());
                }
            }
        }
    }

    private void initPlugin(Long tenantRef, String pluginId) {
        DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion> loadedPlugin = retrievePluginFromDatabase(tenantRef, pluginId);

        if (loadedPlugin != null) {
            File jarFile = null;
            try {
                // download loadedPlugin
                jarFile = this.storeFile(loadedPlugin.getData().getPluginId(), loadedPlugin.getData().getPluginVersion(), tenantRef, loadedPlugin.getData().getJarFile().getBytes());
                // load Plugin
                this.loadPlugin(jarFile.getAbsolutePath(), loadedPlugin.getTenantRef(), loadedPlugin.getData().getPluginId());
            } catch (IOException e) {
                LOGGER.error("Failed to store jar archive for Plugin {} for Tenant {}", loadedPlugin.getData().getPluginId(), loadedPlugin.getTenantRef());
            }  catch (ClassNotFoundException e) {
                LOGGER.error("Plugin {} for Tenant {} was not found at location {}", loadedPlugin.getData().getPluginId(), loadedPlugin.getTenantRef(), jarFile.getAbsolutePath());
            }
        }
    }

    /**
     * Store byte[] as jar in application root path in folder PLUGIN_STORAGE_PATH
     */
    private File storeFile(String pluginId, String pluginVersion, Long tenantRef, byte[] binFile) throws IOException {
        new File(getDirectoryPath(tenantRef)).mkdirs();
        String path = getFilePath(pluginId, pluginVersion, tenantRef);
        File file = new File(path);
        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(binFile);
        } finally {
            stream.close();
        }
        return file;
    }

    private List<DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion>> retrievePluginsFromDatabase(){
        BooleanFilter activeFilter = new BooleanFilter("isActive", true);

        SortColumn priority = new SortColumn("priority", false);
        List<SortColumn> sortPriority = new ArrayList<SortColumn>();
        sortPriority.add(priority);

        LoadedPluginSearchRequest loadedPluginSearchRequest = new LoadedPluginSearchRequest();
        loadedPluginSearchRequest.setSearchFilter(activeFilter);
        loadedPluginSearchRequest.setSortColumns(sortPriority);

        ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion> result = (ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>) executor.executeSynchronous(loadedPluginSearchRequest);
        List<DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion>>datalist = result.getDataList();

        return datalist;
    }

    private DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion> retrievePluginFromDatabase(Long tenantRef, String pluginId){
        BooleanFilter activeFilter = new BooleanFilter("isActive", true);
        LongFilter tenantFilter = new LongFilter("tenantRef", tenantRef, null, null, null);
        AsciiFilter pluginFilter = new AsciiFilter("pluginId", pluginId, null, null, null, null);

        AndFilter andFilter2 = new AndFilter(pluginFilter, tenantFilter);
        AndFilter andFilter = new AndFilter(activeFilter, andFilter2);

        SortColumn priority = new SortColumn("priority", false);
        List<SortColumn> sortPriority = new ArrayList<SortColumn>();
        sortPriority.add(priority);

        LoadedPluginSearchRequest loadedPluginSearchRequest = new LoadedPluginSearchRequest();
        loadedPluginSearchRequest.setSearchFilter(andFilter);
        loadedPluginSearchRequest.setSortColumns(sortPriority);

        ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion> result = (ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>) executor.executeSynchronous(loadedPluginSearchRequest);
        List<DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion>>datalist = result.getDataList();

        if(datalist.isEmpty()) {
            return null;
        } else {
            return datalist.get(0);
        }
    }

    private String getPluginVersionFromDB(Long tenantRef, String pluginId){
        DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion> result = retrievePluginFromDatabase(tenantRef, pluginId);

        // plugin_id + tenant_ref are unique
        if (result == null) {
            return null;
        } else {
            return result.getData().getPluginVersion();
        }
    }

    /**
     * Wrapper for classloader and loadedClass
     *
     * @param <T> Class of which loadedClass is instance of
     */
    private class LoadedClass<T>{
        public T loadedClass;
        public PluginClassloader classloader;


        /**
         * @param loadedClass class which was loaded
         * @param classloader PluginClassloader instance the class was loaded with
         */
        LoadedClass(T loadedClass, PluginClassloader classloader){
            this.loadedClass = loadedClass;
            this.classloader = classloader;
        }
    }

    private File getFile(String path){
        return new File(path);
    }

    private PluginClassloader getClassloader(String pluginId, File jarFile) {
        PluginClassloader classloader;
        if(loadedPlugins.containsKey(pluginId)) {
            classloader = loadedPlugins.get(pluginId).classloader;
        }else {
            classloader = new PluginClassloader(jarFile, this.getClass().getClassLoader());
        }
        return classloader;
    }

    /**
     * Check whether jar file of Plugin  has already been downloaded.
     * @param pluginId Identifier of plugin
     * @param pluginVersion Semantic Versioning number of plugin
     * @return true if file exists, otherwise false
     */
    private boolean isFileDownloaded(String pluginId, String pluginVersion, Long tenantRef) {
        String path = getFilePath(pluginId, pluginVersion, tenantRef);
        File file = new File(path);
        return file.exists();
    }

    private String getFileName(String pluginId, String pluginVersion) {
        return pluginId + "_" + pluginVersion;
    }

    /**
     * @param pluginId Identifier of plugin
     * @param pluginVersion Semantic Versioning number of plugin
     * @return Path of plugin in file storage
     */
    private String getFilePath(String pluginId, String pluginVersion, Long tenantRef) {
        return getDirectoryPath(tenantRef) + getFileName(pluginId, pluginVersion) + PLUGIN_FILE_TYPE;
    }

    private String getDirectoryPath(Long tenantRef) {
        return sRootPath + PLUGIN_STORAGE_PATH + tenantRef.toString() + "\\";
    }

    /**
     * Compare two semantic versioning number strings.
     * @param leftVer First Semantic Versioning number
     * @param rightVer Second Semantic Versioning number
     * @return <b>0</b> if equal, <b>1</b> if leftVer (first version number) is higher, <b>-1</b> if rightVer (second version number) is higher
     */
    private int compareVersions(String leftVer, String rightVer) {
        if (leftVer.equals(rightVer)) {
            // return 0 if equal
            return 0;
        } else if (rightVer == null || leftVer == null){
            return -2;
        } else {
            // tokenize version strings
            String[] leftVerTokens = leftVer.split(".");
            String[] rightVerTokens = rightVer.split(".");

            for(int i = 0; i < leftVerTokens.length; i++) {
                if(i < rightVerTokens.length) {
                    if (Integer.parseInt(leftVerTokens[i]) > Integer.parseInt(leftVerTokens[i])) {
                        // leftVar Version number is higher
                        return 1;
                    } else if (Integer.parseInt(leftVerTokens[i]) > Integer.parseInt(leftVerTokens[i])) {
                        // rightVar version number is higher
                        return -1;
                    }
                } else {
                    // leftVer has PATCH number, rightVer does not
                    // and PATCH number is decider
                    return 1;
                }
            }

            // Strings not equal, but no decision yet -> rightVer has additional PATCH number
            // and Patch number is decider
            return -1;
        }
    }
}
