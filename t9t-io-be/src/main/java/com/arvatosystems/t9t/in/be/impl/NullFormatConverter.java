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
package com.arvatosystems.t9t.in.be.impl;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;

/**
 * Input format converter which discards the data. Useful if the files should be only stored, and not processed further.
 */
@Dependent
@Named("NULL")
public class NullFormatConverter extends AbstractInputFormatConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NullFormatConverter.class);

    @Override
    public void process(final InputStream is) {
        LOGGER.debug("Received a file via data sink {}", importDataSinkDTO.getDataSinkId());
    }
}
