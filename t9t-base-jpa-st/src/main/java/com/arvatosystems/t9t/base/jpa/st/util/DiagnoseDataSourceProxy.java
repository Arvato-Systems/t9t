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
package com.arvatosystems.t9t.base.jpa.st.util;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper using reflection proxies for java.sql.DataSource to track open and close of connections to analyse connection
 * usage. The proxy is only created if provided logger is enabled in trace level. In any other case the original data
 * source is just returned.
 *
 * Only for analysis and testing purpose. Using this proxy in production setup is not recommended!
 */
public class DiagnoseDataSourceProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiagnoseDataSourceProxy.class);

    public static DataSource createProxy(DataSource target) {
        if (LOGGER.isTraceEnabled()) {
            LOGGER.error("Wrap data source {} with diagnose proxy - only recommended for testing and not for production setup!", target);
            return DataSourceInvocationHandler.wrap(target);
        } else {
            return target;
        }
    }

    private static class DataSourceInvocationHandler implements InvocationHandler {

        private final DataSource target;

        private final Map<Connection, InvocationContext> connectionUsage = new HashMap<>();

        public static DataSource wrap(DataSource dataSource) {
            return (DataSource) Proxy.newProxyInstance(ConnectionInvocationHandler.class.getClassLoader(), new Class[] { DataSource.class },
                                                       new DataSourceInvocationHandler(dataSource));
        }

        private DataSourceInvocationHandler(DataSource target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object result = method.invoke(target, args);

            if ("getConnection".equals(method.getName()) && result instanceof Connection) {
                connectionOpen((Connection) result);
                result = ConnectionInvocationHandler.wrap((Connection) result, this);
            }

            return result;
        }

        public synchronized void connectionClosed(Connection connection) {
            connectionUsage.remove(connection);

            LOGGER.trace("Connection {} closed", connection);

            logUsage();
        }

        public synchronized void connectionOpen(Connection connection) {
            final InvocationContext context = new InvocationContext();
            connectionUsage.put(connection, context);

            LOGGER.trace("Connection {} opened:\n{}", connection, context);

            logUsage();
        }

        private void logUsage() {
            LOGGER.trace("Connection usage: {} connections\n{}", connectionUsage.size(), connectionUsage.values()
                                                                                                        .stream()
                                                                                                        .map(InvocationContext::toString)
                                                                                                        .collect(joining("\n")));
        }
    }

    private static class InvocationContext {
        private StackTraceElement[] stackTrace;
        private Date timestamp;
        private long threadId;
        private String threadName;

        public InvocationContext() {
            final Thread thread = Thread.currentThread();
            this.timestamp = new Date();
            this.stackTrace = thread.getStackTrace();
            this.threadId = thread.getId();
            this.threadName = thread.getName();
        }

        @Override
        public String toString() {
            final Formatter formatter = new Formatter();

            formatter.format("%12s %s (%s)%n", new SimpleDateFormat("HH:mm:ss''SSS").format(timestamp), threadName, threadId);

            if (stackTrace != null && stackTrace.length > 0) {
                for (StackTraceElement ste : stackTrace) {
                    formatter.format("  %s.%s (%s:%s)%n", ste.getClassName(), ste.getMethodName(), ste.getFileName(), ste.getLineNumber());
                }
            } else {
                formatter.format("  no stack trace available%n");
            }

            return formatter.toString();
        }
    }

    private static class ConnectionInvocationHandler implements InvocationHandler {

        private final Connection target;
        private final DataSourceInvocationHandler dataSourceHandler;

        public static Connection wrap(Connection connection, DataSourceInvocationHandler dataSourceHandler) {
            return (Connection) Proxy.newProxyInstance(ConnectionInvocationHandler.class.getClassLoader(), new Class[] { Connection.class },
                                                       new ConnectionInvocationHandler(connection, dataSourceHandler));
        }

        private ConnectionInvocationHandler(Connection target, DataSourceInvocationHandler dataSourceHandler) {
            this.target = target;
            this.dataSourceHandler = dataSourceHandler;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            final Object result = method.invoke(target, args);

            if ("close".equals(method.getName())) {
                dataSourceHandler.connectionClosed(target);
            }

            return result;
        }

    }

}
