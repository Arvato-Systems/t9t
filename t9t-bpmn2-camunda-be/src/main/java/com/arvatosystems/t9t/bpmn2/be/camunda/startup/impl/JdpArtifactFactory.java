/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.bpmn2.be.camunda.startup.impl;

import org.camunda.bpm.engine.ArtifactFactory;
import org.camunda.bpm.engine.impl.DefaultArtifactFactory;

import de.jpaw.dp.Jdp;

/**
 * Artifact factory for resolving classes by JDP and only as fallback to always create a new instance.
 *
 * @author TWEL006
 */
public class JdpArtifactFactory implements ArtifactFactory {

    private final DefaultArtifactFactory defaultArtifactFactory = new DefaultArtifactFactory();

    @Override
    public <T> T getArtifact(Class<T> clazz) {
        T instance = Jdp.getOptional(clazz);

        if (instance == null) {
            instance = defaultArtifactFactory.getArtifact(clazz);
        }

        return instance;
    }

}
