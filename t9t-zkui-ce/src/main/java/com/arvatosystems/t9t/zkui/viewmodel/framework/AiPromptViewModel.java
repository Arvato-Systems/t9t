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

import com.arvatosystems.t9t.ai.AiPromptDTO;
import com.arvatosystems.t9t.ai.AiPromptParameter;
import com.arvatosystems.t9t.ai.AiPromptParameterDTO;
import com.arvatosystems.t9t.ai.AiPromptRef;
import com.arvatosystems.t9t.base.T9tUtil;
import com.arvatosystems.t9t.base.entities.FullTrackingWithVersion;
import com.arvatosystems.t9t.zkui.exceptions.ServiceResponseException;
import com.arvatosystems.t9t.zkui.util.Constants;
import com.arvatosystems.t9t.zkui.viewmodel.CrudSurrogateKeyVM;
import com.arvatosystems.t9t.zkui.viewmodel.MapHelper;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Init(superclass = true)
public class AiPromptViewModel extends CrudSurrogateKeyVM<AiPromptRef, AiPromptDTO, FullTrackingWithVersion> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AiPromptViewModel.class);

    private final List<MapHelper<String, AiPromptParameter>> parameters = new ArrayList<>();
    private MapHelper<String, AiPromptParameter> selectedParameter;

    @Override
    protected void loadData(@Nonnull final DataWithTracking<AiPromptDTO, FullTrackingWithVersion> dwt) {
        super.loadData(dwt);
        parameters.clear();
        if (data != null && data.getParameters() != null && data.getParameters().getParameters() != null) {
            for (final Map.Entry<String, AiPromptParameter> entry : data.getParameters().getParameters().entrySet()) {
                parameters.add(new MapHelper<>(entry.getKey(), entry.getValue()));
            }
        }
    }

    @Override
    protected void saveHook() {
        if (data.getParameters() != null && data.getParameters().getParameters() != null) {
            for (Map.Entry<String, AiPromptParameter> entry: data.getParameters().getParameters().entrySet()) {
                if (T9tUtil.isBlank(entry.getKey()) || entry.getValue() == null || T9tUtil.isBlank(entry.getValue().getDescription())) {
                    throw new ServiceResponseException(Constants.ErrorCodes.PARAMETER_ERROR, session.translate("aiPrompt.parameters.error", "empty"), null);
                }
            }
        } else {
            data.setParameters(new AiPromptParameterDTO(new HashMap<>()));
        }

    }

    @Override
    protected void clearData() {
        super.clearData();
        parameters.clear();
    }

    @NotifyChange({ "parameters", "selectedParameter" })
    @Command
    public void addParameter() {
        if (data.getParameters() == null || data.getParameters().getParameters() == null) {
            data.setParameters(new AiPromptParameterDTO(new HashMap<>()));
        }
        selectedParameter = new MapHelper<>(null, new AiPromptParameter());
        parameters.add(selectedParameter);
        onParameterChange();
    }

    @NotifyChange({ "parameters", "selectedParameter" })
    @Command
    public void removeParameter() {
        parameters.remove(selectedParameter);
        if (!parameters.isEmpty()) {
            selectedParameter = parameters.getFirst();
        }
        onParameterChange();
    }

    @Command
    public void onParameterChange() {
        data.getParameters().getParameters().clear();
        for (final MapHelper<String, AiPromptParameter> entry : parameters) {
            if (!data.getParameters().getParameters().containsKey(entry.getKey())) {
                data.getParameters().getParameters().put(entry.getKey(), entry.getValue());
            } else {
                LOGGER.warn("duplicate parameter key: {}", entry.getKey());
            }
        }
    }

    @Nonnull
    public List<MapHelper<String, AiPromptParameter>> getParameters() {
        return parameters;
    }

    public MapHelper<String, AiPromptParameter> getSelectedParameter() {
        return selectedParameter;
    }

    public void setSelectedParameter(@Nonnull final MapHelper<String, AiPromptParameter> selectedParameter) {
        this.selectedParameter = selectedParameter;
    }

}
