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
