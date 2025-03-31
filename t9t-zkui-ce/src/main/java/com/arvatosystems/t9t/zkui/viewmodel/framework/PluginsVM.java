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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import java.io.IOException;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.BindContext;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.ContextParam;
import org.zkoss.bind.annotation.ContextType;
import org.zkoss.bind.annotation.Init;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.plugins.LoadedPluginDTO;
import com.arvatosystems.t9t.plugins.LoadedPluginRef;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;

import de.jpaw.util.ByteArray;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;

@Init(superclass = true)
public class PluginsVM extends CrudSurrogateKeyVM<LoadedPluginRef, LoadedPluginDTO, FullTrackingWithVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginsVM.class);
    private static final String FILE_EXTENSION_JAR = ".jar";
    protected String path = "";

    @Override
    protected void clearData() {
        super.clearData();
    }

    @Override
    protected void loadData(DataWithTracking<LoadedPluginDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
    }

    @Command
    public void uploadPlugin(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) throws IOException {
        UploadEvent event = (UploadEvent) ctx.getTriggerEvent();
        final Media media = event.getMedia();
        final String filename = media.getName();

        if (filename == null || !filename.endsWith(FILE_EXTENSION_JAR)) {
            Messagebox.show(session.translate("PluginsVM", "jarOnly"), session.translate("PluginsVM", "com.badinput"), Messagebox.OK,
                Messagebox.ERROR);
            LOGGER.error("ERROR: Invalid file name: {} ", filename);
            return;
        }

        byte[] uploaded = media.getByteData();
        if (uploaded != null) {
            data.setJarFile(new ByteArray(uploaded));
            path = event.getMedia().getName();
        }
    }

    @Override
    @Command
    public void commandSave() {
        data.setWhenLoaded(Instant.now());

        super.commandSave();
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
