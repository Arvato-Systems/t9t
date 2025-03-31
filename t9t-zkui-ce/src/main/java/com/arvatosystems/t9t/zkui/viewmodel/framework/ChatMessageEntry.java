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
package com.arvatosystems.t9t.zkui.viewmodel.framework;

import org.zkoss.util.media.Media;

public class ChatMessageEntry {

    private final String from;
    private final String text;
    private final Media media;

    public ChatMessageEntry(final String from, final String text, final Media media) {
        this.from = from;
        this.text = text;
        this.media = media;
    }

    public String getFrom() {
        return from;
    }

    public String getText() {
        return text;
    }

    public Media getMedia() {
        return media;
    }
}
