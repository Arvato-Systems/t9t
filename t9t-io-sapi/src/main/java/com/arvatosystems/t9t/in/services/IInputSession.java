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
package com.arvatosystems.t9t.in.services;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.io.DataSinkDTO;

import de.jpaw.bonaparte.core.BonaPortable;

/**
 * A processor for input records.
 *
 */
public interface IInputSession {

    /** Opens the import, reads the configuration, establishes the backend session. */
    DataSinkDTO open(String dataSourceId, UUID apiKey, String sourceURI, Map<String, Object> params);

    /**
     * Retrieve the sourceUri as provided with {#link {@link #open(String, UUID, String, Map)}}.
     */
    String getSourceURI();

    /**
     * Return the tenant the current connection is authenticated for.
     *
     * @return {@code null} if no valid authentication has been performed, otherwise the tenant id of the related JWT.
     */
    String getTenantId();

    /** Process stream data. Only possible with a configured commFormat.
     * If used, then the other process methods will be invoked as required.
     * The stream is not */
    void process(InputStream is);


    /** Processes a single record (or preassembled group of records) via backend call in a single transaction.
     * The record will be pushed through a configured transformer, if one exists. */
    void process(BonaPortable dto);

    /** executes a transformed request. */
    ServiceResponse process(RequestParameters rp);

    /** Ends processing, writes a summary into the sink table. */
    void close();

    /** Retrieves possible header fields which are provided before the main record type starts.
     * Usually only required for XML formats.
     * By default, the XML parsers passes back all fields as Strings.
     * null is returned if no such field has been parsed.
     *
     * @param name
     * @return
     */
    Object getHeaderData(String name);

    /** Stores possible header fields which are provided before the main record type starts.
     * Usually only required for XML formats.
     */
    void setHeaderData(String name, Object value);
}
