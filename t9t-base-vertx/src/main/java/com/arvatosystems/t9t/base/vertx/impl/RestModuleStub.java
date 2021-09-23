package com.arvatosystems.t9t.base.vertx.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.vertx.IRestModule;

import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;
import io.vertx.core.Vertx;

@Singleton
@Fallback
public class RestModuleStub implements IRestModule {
    private static final Logger LOGGER = LoggerFactory.getLogger(RestModuleStub.class);

    @Override
    public void createRestServer(Vertx vertx, int port) {
        LOGGER.error("Cannot create REST module at port {} - implementation not deployed", port);
        throw new RuntimeException("No REST module available");
    }
}
