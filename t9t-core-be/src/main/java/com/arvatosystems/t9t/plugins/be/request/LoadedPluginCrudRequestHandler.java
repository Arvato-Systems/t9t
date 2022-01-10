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
package com.arvatosystems.t9t.plugins.be.request;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.base.be.impl.AbstractCrudSurrogateKeyBERequestHandler;
import com.arvatosystems.t9t.base.crud.CrudSurrogateKeyResponse;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.base.search.ReadAllResponse;
import com.arvatosystems.t9t.base.services.IExecutor;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.plugins.LoadedPluginDTO;
import com.arvatosystems.t9t.plugins.LoadedPluginRef;
import com.arvatosystems.t9t.plugins.PluginLogDTO;
import com.arvatosystems.t9t.plugins.request.LoadedPluginCrudRequest;
import com.arvatosystems.t9t.plugins.request.LoadedPluginSearchRequest;
import com.arvatosystems.t9t.plugins.request.PluginLogCrudRequest;
import com.arvatosystems.t9t.plugins.services.ILoadedPluginResolver;
import com.arvatosystems.t9t.plugins.services.IPluginManager;

import de.jpaw.bonaparte.pojos.api.AndFilter;
import de.jpaw.bonaparte.pojos.api.AsciiFilter;
import de.jpaw.bonaparte.pojos.api.BooleanFilter;
import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.apiw.DataWithTrackingW;
import de.jpaw.dp.Jdp;

import java.time.Instant;
import java.util.List;

//FIXME: There is a severe issue, in that for CREATE / UPDATE / MERGE, the pluginId of the data record is independent of the ID within the plugin itself.
//We should either remove the ID within the plugin (solely trusting the user to assign IDs) or peek into the JAR before loading it, comparing the IDs,
//or (preferred) use a completely different method to install JARs.
public class LoadedPluginCrudRequestHandler
        extends AbstractCrudSurrogateKeyBERequestHandler<LoadedPluginRef, LoadedPluginDTO, FullTrackingWithVersion, LoadedPluginCrudRequest> {

    protected final ILoadedPluginResolver resolver = Jdp.getRequired(ILoadedPluginResolver.class);
    protected final IExecutor executor = Jdp.getRequired(IExecutor.class);
    protected final IPluginManager pluginManager = Jdp.getRequired(IPluginManager.class);

    @Override
    public CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> execute(final RequestContext ctx, final LoadedPluginCrudRequest crudRequest) {
        LoadedPluginDTO loadedPlugin = new LoadedPluginDTO();
        LoadedPluginDTO oldEntry = new LoadedPluginDTO();
        boolean entryExists = false;

        if (OperationType.DELETE == crudRequest.getCrud()) {
            loadedPlugin = getLoadedPluginByObjectRef(crudRequest.getKey()).getDataList().get(0).getData();
            oldEntry = loadedPlugin;
            entryExists = true;
        } else {
            loadedPlugin = crudRequest.getData();
            List<DataWithTrackingW<LoadedPluginDTO, FullTrackingWithVersion>> datalist = getLoadedPlugin(ctx.tenantRef, loadedPlugin.getPluginId())
                    .getDataList();
            if (datalist.size() > 0) {
                entryExists = true;
                oldEntry = datalist.get(0).getData();
            }
        }

        if (entryExists) {
            if (OperationType.CREATE != crudRequest.getCrud()) {
                // create plugin log entry
                final PluginLogDTO pluginLogEntry = new PluginLogDTO();
                pluginLogEntry.setIsActive(false);
                pluginLogEntry.setPluginId(oldEntry.getPluginId());
                pluginLogEntry.setPluginVersion(oldEntry.getPluginVersion());
                pluginLogEntry.setPriority(oldEntry.getPriority());
                pluginLogEntry.setWhenLoaded(oldEntry.getWhenLoaded());
                pluginLogEntry.setWhenRemoved(Instant.now());

                final PluginLogCrudRequest logRequest = new PluginLogCrudRequest();
                logRequest.setCrud(OperationType.CREATE);
                logRequest.setData(pluginLogEntry);

                executor.executeSynchronousAndCheckResult(ctx, logRequest, CrudSurrogateKeyResponse.class);
            } else {
                // remove loaded plugin
                throw new T9tException(T9tException.RECORD_ALREADY_EXISTS);
            }
        }

        final CrudSurrogateKeyResponse<LoadedPluginDTO, FullTrackingWithVersion> response = execute(ctx, crudRequest, resolver);
        // database operation was successful: load plugin or delete it
        switch (crudRequest.getCrud()) {
        case CREATE:
        case MERGE:
        case UPDATE:
            pluginManager.loadPlugin(ctx.getTenantRef(), response.getData().getJarFile());
            break;
        case DELETE:
            pluginManager.removePlugin(ctx.getTenantRef(), response.getData().getPluginId());
            break;
        default:
            break;
        }
        return response;
    }

    @SuppressWarnings("unchecked")
    ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion> getLoadedPlugin(final Long tenantRef, final String pluginId) {
        final BooleanFilter activeFilter = new BooleanFilter("isActive", true);
        final LongFilter tenantFilter = new LongFilter("tenantRef", tenantRef, null, null, null);
        final AsciiFilter pluginFilter = new AsciiFilter("pluginId", pluginId, null, null, null, null);

        final AndFilter andFilter2 = new AndFilter(pluginFilter, tenantFilter);
        final AndFilter andFilter = new AndFilter(activeFilter, andFilter2);

        final LoadedPluginSearchRequest loadedPluginSearchRequest = new LoadedPluginSearchRequest();
        loadedPluginSearchRequest.setSearchFilter(andFilter);

        return (ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>) executor.executeSynchronous(loadedPluginSearchRequest);
    }

    @SuppressWarnings("unchecked")
    ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion> getLoadedPluginByObjectRef(final Long objectRef) {
        final LongFilter objectRefFilter = new LongFilter("objectRef", objectRef, null, null, null);

        final LoadedPluginSearchRequest loadedPluginSearchRequest = new LoadedPluginSearchRequest();
        loadedPluginSearchRequest.setSearchFilter(objectRefFilter);

        return (ReadAllResponse<LoadedPluginDTO, FullTrackingWithVersion>) executor.executeSynchronous(loadedPluginSearchRequest);
    }

    boolean checkIfPluginExists(final Long tenantRef, final String pluginId) {
        return !(getLoadedPlugin(tenantRef, pluginId).getDataList().size() == 0);
    }
}
