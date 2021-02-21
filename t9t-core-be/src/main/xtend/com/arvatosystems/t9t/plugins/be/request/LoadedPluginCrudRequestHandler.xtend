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
package com.arvatosystems.t9t.plugins.be.request

import com.arvatosystems.t9t.base.T9tException
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.base.search.ReadAllResponse
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.base.services.RequestContext
import com.arvatosystems.t9t.plugins.LoadedPluginDTO
import com.arvatosystems.t9t.plugins.LoadedPluginRef
import com.arvatosystems.t9t.plugins.PluginLogDTO
import com.arvatosystems.t9t.plugins.request.LoadedPluginCrudRequest
import com.arvatosystems.t9t.plugins.request.LoadedPluginSearchRequest
import com.arvatosystems.t9t.plugins.request.PluginLogCrudRequest
import com.arvatosystems.t9t.plugins.services.ILoadedPluginResolver
import com.arvatosystems.t9t.plugins.services.IPluginManager
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.BooleanFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.OperationType
import de.jpaw.dp.Inject
import org.joda.time.Instant

// FIXME: There is a severe issue, in that for CREATE / UPDATE / MERGE, the pluginId of the data record is independent of the ID within the plugin itself.
// We should either remove the ID within the plugin (solely trusting the user to assign IDs) or peek into the JAR before loading it, comparing the IDs,
// or (preferred) use a completely different method to install JARs.
class LoadedPluginCrudRequestHandler  extends AbstractCrudSurrogateKeyBERequestHandler<LoadedPluginRef, LoadedPluginDTO, FullTrackingWithVersion, LoadedPluginCrudRequest> {
    @Inject ILoadedPluginResolver resolver
    @Inject IExecutor executor
    @Inject IPluginManager pluginManager

    override CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> execute(RequestContext ctx, LoadedPluginCrudRequest crudRequest) {
        var DTO = new LoadedPluginDTO
        var oldEntry = new LoadedPluginDTO
        var entryExists = false

        if (crudRequest.crud == OperationType.DELETE) {
            DTO = getLoadedPluginByObjectRef(crudRequest.key).dataList.get(0).data
            oldEntry = DTO
            entryExists = true
        } else {
            DTO = crudRequest.data
            val datalist = getLoadedPlugin(ctx.tenantRef, DTO.pluginId).dataList
            if (datalist.size > 0) {
                entryExists = true
                oldEntry = datalist.get(0).data
            }
        }

        if (entryExists) {
            if (crudRequest.crud != OperationType.CREATE) {
                // create plugin log entry
                val pluginLogEntry = new PluginLogDTO
                pluginLogEntry.isActive = false
                pluginLogEntry.pluginId = oldEntry.pluginId
                pluginLogEntry.pluginVersion = oldEntry.pluginVersion
                pluginLogEntry.priority = oldEntry.priority
                pluginLogEntry.whenLoaded = oldEntry.whenLoaded
                pluginLogEntry.whenRemoved = Instant.now

                val logRequest = new PluginLogCrudRequest
                logRequest.crud = OperationType.CREATE
                logRequest.data = pluginLogEntry

                executor.executeSynchronousAndCheckResult(ctx, logRequest, CrudSurrogateKeyResponse)
            } else {
                // remove loaded plugin
                throw new T9tException(T9tException.RECORD_ALREADY_EXISTS);
            }
        }

        val response = execute(ctx, crudRequest, resolver) as CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion>
        // database operation was successful: load plugin or delete it
        switch (crudRequest.crud) {
            case CREATE,
            case MERGE,
            case UPDATE:
                pluginManager.loadPlugin(ctx.tenantRef, response.data.jarFile)
            case DELETE:
                pluginManager.removePlugin(ctx.tenantRef, response.data.pluginId)
        }
        return response
    }

    def getLoadedPlugin(Long tenantRef, String pluginId) {
        val activeFilter = new BooleanFilter("isActive", true)
        val tenantFilter = new LongFilter("tenantRef", tenantRef, null, null, null)
        val pluginFilter = new AsciiFilter("pluginId", pluginId, null, null, null, null)

        val andFilter2 = new AndFilter(pluginFilter, tenantFilter)
        val andFilter = new AndFilter(activeFilter, andFilter2)

        val loadedPluginSearchRequest = new LoadedPluginSearchRequest()
        loadedPluginSearchRequest.setSearchFilter(andFilter)

        return executor.executeSynchronous(loadedPluginSearchRequest) as ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>
    }

    def getLoadedPluginByObjectRef(Long objectRef){
        val objectRefFilter = new LongFilter("objectRef", objectRef, null, null, null)

        val loadedPluginSearchRequest = new LoadedPluginSearchRequest()
        loadedPluginSearchRequest.setSearchFilter(objectRefFilter)

        return executor.executeSynchronous(loadedPluginSearchRequest) as ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>
    }

    def checkIfPluginExists(Long tenantRef, String pluginId){
        return !(getLoadedPlugin(tenantRef, pluginId).getDataList().size == 0);
    }

}
