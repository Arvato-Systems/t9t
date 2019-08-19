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
package com.arvatosystems.t9t.out.be.jackson;

import java.io.IOException;
import java.io.OutputStreamWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.T9tException;
import com.arvatosystems.t9t.io.T9tIOException;
import com.arvatosystems.t9t.out.be.impl.formatgenerator.AbstractFormatGenerator;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.joda.JodaModule;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;

/**
 * Implementation of {@linkplain ITextDataGenerator} which generates output data in JSON format.
 * The current implementation utilises Jackson library for JSON generation.
 *
 * @author LIEE001
 */
@Dependent
@Named("JSONJackson")
public class JSONJacksonDataGenerator extends AbstractFormatGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONJacksonDataGenerator.class);
    protected OutputStreamWriter osw = null;
    protected ObjectMapper objectMapper = new ObjectMapper();
    protected FilterProvider filters;

    @Override
    protected void openHook() throws IOException, ApplicationException {
        super.openHook();
        osw = new OutputStreamWriter(outputResource.getOutputStream(), encoding);
        objectMapper.registerModule(new JodaModule());
        // add "regexFilter" ID to base class i.e. Object class
        objectMapper.addMixInAnnotations(Object.class, RegexFilterMixIn.class);

        // mapped "regexFilter" ID to actual filter for filtering
        filters = new SimpleFilterProvider().addFilter("regexFilter", new RegexBeanPropertyFilter("\\$.*"));
    }

    /**
     * Mix-in class to be "mixed" with all POJO class which will be serialized as JSON.
     * This mix-in class is used to make all classes have the JsonFilter applied
     */
    @JsonFilter("regexFilter")
    class RegexFilterMixIn {}

    /**
     * Implementation of {@link BeanPropertyFilter} which filter all POJO properties
     * from being serialized if the property name matches given Regex pattern.
     */
    private class RegexBeanPropertyFilter extends SimpleBeanPropertyFilter {

        private String pattern;

        public RegexBeanPropertyFilter(final String pattern) {
            this.pattern = pattern;
        }

        @Override
        @Deprecated
        // Used by BeanPropertyFilter which is deprecated since 2.3
        protected boolean include(final BeanPropertyWriter writer) {
            return !writer.getName().matches(pattern);
        }

        // added for jackson 2.3
        @Override
        protected boolean include(final PropertyWriter writer) {
            return !writer.getName().matches(pattern);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateData(int recordNo, int mappedRecordNo, long recordId, BonaPortable object) throws IOException, ApplicationException {

        try {
            objectMapper.writer(filters).writeValue(osw, object);
        } catch (Exception ex) {
            LOGGER.error("Failed to generate JSON data for output", ex);
            throw new T9tException(T9tIOException.OUTPUT_JSON_EXCEPTION, "Failed to generate JSON data");
        }
    }

    @Override
    public void close() throws IOException, ApplicationException {
        osw.flush();
    }
}
