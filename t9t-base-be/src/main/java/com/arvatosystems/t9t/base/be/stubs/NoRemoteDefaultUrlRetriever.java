package com.arvatosystems.t9t.base.be.stubs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;
import com.arvatosystems.t9t.base.T9tException;

import de.jpaw.dp.Any;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Fallback
@Any
@Singleton
public class NoRemoteDefaultUrlRetriever implements IRemoteDefaultUrlRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(NoRemoteDefaultUrlRetriever.class);

    @Override
    public String getDefaultRemoteUrl() {
        LOGGER.error("No implementation for a default remote URL available (IRemoteDefaultUrlRetriever)");
        throw new T9tException(T9tException.ILE_MISSING_DEPENDENCY, "No implementation for IRemoteDefaultUrlRetriever available");
    }
}
