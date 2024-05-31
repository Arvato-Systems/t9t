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
