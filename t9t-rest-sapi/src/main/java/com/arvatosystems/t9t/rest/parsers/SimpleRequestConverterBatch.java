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
package com.arvatosystems.t9t.rest.parsers;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.request.AutonomousCollectionRequest;

/**
 * Converts a list of elements with the help of a given request parameter converter into a single request.
 *
 * @param <T> the type of input data
 * @param <R> extends {@link RequestParameters} - type of output data
 */
public class SimpleRequestConverterBatch<T, R extends RequestParameters> implements Function<List<T>, R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRequestConverterBatch.class);

    private Function<T, R> requestParameterConverter;

    /**
     * Constructor.
     *
     * @param requestParameterConverter the converter to use for converting a single element
     */
    public SimpleRequestConverterBatch(final Function<T, R> requestParameterConverter) {
        this.requestParameterConverter = requestParameterConverter;
    }

    @SuppressWarnings("unchecked")
    @Override
    public R apply(final List<T> inputData) {
        if (inputData == null || this.requestParameterConverter == null) {
            LOGGER.error("Invalid parameters - inputData={}, requestParameterConverter={}", inputData, this.requestParameterConverter);
            return null;
        }
        final List<RequestParameters> requests = new ArrayList<>(inputData.size());
        inputData.forEach(input -> requests.add(this.requestParameterConverter.apply(input)));
        return (R) new AutonomousCollectionRequest(requests);
    }
}
