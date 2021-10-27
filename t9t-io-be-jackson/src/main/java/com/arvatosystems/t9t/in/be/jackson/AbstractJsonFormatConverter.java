package com.arvatosystems.t9t.in.be.jackson;

import java.util.Map;

import com.arvatosystems.t9t.in.be.impl.AbstractInputFormatConverter;
import com.arvatosystems.t9t.in.services.IInputSession;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import de.jpaw.bonaparte.core.BonaPortableClass;

public abstract class AbstractJsonFormatConverter extends AbstractInputFormatConverter {
    protected ObjectMapper objectMapper;

    @Override
    public void open(final IInputSession inputSession, final Map<String, Object> params, final BonaPortableClass<?> baseBClass) {
        super.open(inputSession, params, baseBClass);
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }
}
