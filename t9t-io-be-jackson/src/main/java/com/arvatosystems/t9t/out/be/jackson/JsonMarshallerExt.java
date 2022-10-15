package com.arvatosystems.t9t.out.be.jackson;

import java.nio.charset.StandardCharsets;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.jackson.JacksonTools;
import com.arvatosystems.t9t.out.services.IMarshallerExt;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;
import de.jpaw.util.ByteArray;
import de.jpaw.util.ByteBuilder;

@Singleton
@Named("JSONJackson")
public class JsonMarshallerExt implements IMarshallerExt<Object> {

    protected final ObjectMapper objectMapper = JacksonTools.createJacksonMapperForExports(false);

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public ByteArray marshal(BonaPortable obj) throws Exception {
        return ByteArray.fromString(objectMapper.writeValueAsString(obj), StandardCharsets.UTF_8);
    }

    @Override
    public BonaPortable unmarshal(ByteBuilder arg0) throws Exception {
        throw new T9tException(T9tException.NOT_YET_IMPLEMENTED);
    }
}
