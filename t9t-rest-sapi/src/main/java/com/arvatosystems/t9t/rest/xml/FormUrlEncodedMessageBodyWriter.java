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
package com.arvatosystems.t9t.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

import com.arvatosystems.t9t.base.T9tHttpClientExtensions;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.MapComposer;
import de.jpaw.dp.Singleton;

/**
 * Special {@link MessageBodyWriter} for
 * {@link MediaType#APPLICATION_FORM_URLENCODED_TYPE} writing
 * {@link BonaPortable} as JSon.
 */
@Provider
@Produces("application/x-www-form-urlencoded")
@Singleton
public class FormUrlEncodedMessageBodyWriter implements MessageBodyWriter<BonaPortable> {

    @Override
    public boolean isWriteable(final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return mt.isCompatible(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    @Override
    public long getSize(final BonaPortable t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt) {
        return 0;
    }

    @Override
    public void writeTo(final BonaPortable t, final Class<?> type, final Type type1, final Annotation[] antns, final MediaType mt,
        final MultivaluedMap<String, Object> mm, final OutputStream out) throws IOException, WebApplicationException {
        final String encoded = T9tHttpClientExtensions.urlEncode(MapComposer.marshal(t, false, false));
        out.write(encoded.getBytes(StandardCharsets.UTF_8));
    }
}
