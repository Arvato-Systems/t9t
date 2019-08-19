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
package com.arvatosystems.t9t.voice;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.voice.request.VoiceApplicationCrudRequest;
import com.arvatosystems.t9t.voice.request.VoiceApplicationSearchRequest;
import com.arvatosystems.t9t.voice.request.VoiceModuleCfgCrudRequest;
import com.arvatosystems.t9t.voice.request.VoiceModuleCfgSearchRequest;
import com.arvatosystems.t9t.voice.request.VoiceResponseCrudRequest;
import com.arvatosystems.t9t.voice.request.VoiceResponseSearchRequest;
import com.arvatosystems.t9t.voice.request.VoiceUserCrudRequest;
import com.arvatosystems.t9t.voice.request.VoiceUserSearchRequest;

public class T9tVoiceModels implements IViewModelContainer {
    public static final CrudViewModel<VoiceApplicationDTO, FullTrackingWithVersion> VOICE_APPLICATION_VIEW_MODEL = new CrudViewModel<VoiceApplicationDTO, FullTrackingWithVersion>(
        VoiceApplicationDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        VoiceApplicationSearchRequest.BClass.INSTANCE,
        VoiceApplicationCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<VoiceUserDTO, FullTrackingWithVersion> VOICE_USER_VIEW_MODEL = new CrudViewModel<VoiceUserDTO, FullTrackingWithVersion>(
        VoiceUserDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        VoiceUserSearchRequest.BClass.INSTANCE,
        VoiceUserCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<VoiceResponseDTO, FullTrackingWithVersion> VOICE_RESPONSE_VIEW_MODEL = new CrudViewModel<VoiceResponseDTO, FullTrackingWithVersion>(
        VoiceResponseDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        VoiceResponseSearchRequest.BClass.INSTANCE,
        VoiceResponseCrudRequest.BClass.INSTANCE);
    public static final CrudViewModel<VoiceModuleCfgDTO, FullTrackingWithVersion> VOICE_MODULE_CFG_VIEW_MODEL = new CrudViewModel<VoiceModuleCfgDTO, FullTrackingWithVersion>(
        VoiceModuleCfgDTO.BClass.INSTANCE,
        FullTrackingWithVersion.BClass.INSTANCE,
        VoiceModuleCfgSearchRequest.BClass.INSTANCE,
        VoiceModuleCfgCrudRequest.BClass.INSTANCE);

    static {
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("voiceApplication", VOICE_APPLICATION_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("voiceUser",        VOICE_USER_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("voiceResponse",    VOICE_RESPONSE_VIEW_MODEL);
        IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.putIfAbsent("voiceModuleCfg",   VOICE_MODULE_CFG_VIEW_MODEL);
    }
}
