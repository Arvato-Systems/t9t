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
package com.arvatosystems.t9t.bpmn.pojo;

import java.io.InputStream;

/**
 * Represents a process definition. A process definition contains:
 * <ul>
 *  <li>id of the process definition</li>
 *  <li>input stream from which the process definition will be loaded</li>
 * </ul>
 * @author LIEE001
 */
public final class ProcessDefinition {

    private String id;
    private InputStream inputStream;

    public ProcessDefinition(final String id, final InputStream inputStream) {
        this.id = id;
        this.inputStream = inputStream;
    }

    public String getId() {
        return id;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}
