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

import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler
import com.arvatosystems.t9t.plugins.LoadedPluginRef
import com.arvatosystems.t9t.plugins.LoadedPluginDTO
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion
import com.arvatosystems.t9t.plugins.request.LoadedPluginCrudRequest
import de.jpaw.dp.Inject
import com.arvatosystems.t9t.plugins.services.ILoadedPluginResolver
import com.arvatosystems.t9t.base.api.ServiceResponse
import com.arvatosystems.t9t.base.services.RequestContext
import de.jpaw.bonaparte.pojos.api.OperationType
import com.arvatosystems.t9t.plugins.PluginLogDTO
import org.joda.time.Instant
import com.arvatosystems.t9t.plugins.request.PluginLogCrudRequest
import com.arvatosystems.t9t.base.services.IExecutor
import com.arvatosystems.t9t.plugins.request.LoadedPluginSearchRequest
import com.arvatosystems.t9t.base.search.ReadAllResponse
import de.jpaw.bonaparte.pojos.api.AsciiFilter
import de.jpaw.bonaparte.pojos.api.LongFilter
import de.jpaw.bonaparte.pojos.api.AndFilter
import de.jpaw.bonaparte.pojos.api.BooleanFilter
import com.arvatosystems.t9t.base.T9tException

class LoadedPluginCrudRequestHandler  extends AbstractCrudSurrogateKeyBERequestHandler<LoadedPluginRef, LoadedPluginDTO, FullTrackingWithVersion, LoadedPluginCrudRequest>{
    @Inject ILoadedPluginResolver resolver
    @Inject IExecutor executor

    override ServiceResponse execute(RequestContext ctx, LoadedPluginCrudRequest crudRequest) {
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

        if (entryExists){
            if (crudRequest.crud != OperationType.CREATE){
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

                val resp = executor.executeSynchronous(ctx, logRequest)

                if (resp.returnCode != 0) {
                    return resp;
                }
            } else {
                // remove loaded plugin
                throw new T9tException(T9tException.RECORD_ALREADY_EXISTS);
            }
        }

        return execute(ctx, crudRequest, resolver)
    }

    def getLoadedPlugin(Long tenantRef, String pluginId){
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
