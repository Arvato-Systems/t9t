package com.arvatosystems.t9t.services;

/**
 * Implementation will be called when a search key is entered in the main title.
 * The implementation usually does a redirect.
 */
public interface ITitleBarSearch {
    void search(String text);
}
