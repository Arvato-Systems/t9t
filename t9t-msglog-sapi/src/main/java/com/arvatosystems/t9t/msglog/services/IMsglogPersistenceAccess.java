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
package com.arvatosystems.t9t.msglog.services;

import java.util.List;

import com.arvatosystems.t9t.msglog.MessageDTO;

import jakarta.annotation.Nonnull;

/**
 * Interface to persist transaction execution logs.
 */
public interface IMsglogPersistenceAccess {
    /** Opens a request logger backend. */
    void open();

    /** Writes the given entries to the database. */
    void write(@Nonnull List<MessageDTO> entries);

    /** closes a request logger backend. */
    void close();
}
