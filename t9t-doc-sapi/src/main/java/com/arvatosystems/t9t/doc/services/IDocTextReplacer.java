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
package com.arvatosystems.t9t.doc.services;

import java.util.Locale;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

/**
 * Plugin interface for converting textual data within documents.
 * Called by the r() method of the docFormatter.
 * Implementations can use a qualifier or not.
 */
public interface IDocTextReplacer {
    @Nonnull
    String textReplace(@Nonnull Locale locale, @Nonnull String text, @Nullable String delimiters);
}
