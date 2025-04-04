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
package com.arvatosystems.t9t.client.init;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.IRemoteDefaultUrlRetriever;

public class DefaultsConfigurationProvider implements IRemoteDefaultUrlRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultsConfigurationProvider.class);

    public static final int    DEFAULT_REMOTE_PORT = 8024;
    public static final String DEFAULT_REMOTE_HOST = "localhost";
    public static final String DEFAULT_REQUEST_PATH = "/rpc";
    public static final String DEFAULT_AUTHENTICATION_PATH = "/login";

    private final int remotePort;
    private final String remoteHost;
    private final String remotePathRequests;
    private final String fullUrl;

    protected DefaultsConfigurationProvider() {
        this("DEFAULTS", null, null, null);
    }
    protected DefaultsConfigurationProvider(
      final String source,
      final String remotePort,
      final String remoteHost,
      final String remotePathRequests) {
        this.remotePort         = remotePort         == null ? DEFAULT_REMOTE_PORT  : Integer.parseInt(remotePort);
        this.remoteHost         = remoteHost         == null ? DEFAULT_REMOTE_HOST  : remoteHost;
        this.remotePathRequests = remotePathRequests == null ? DEFAULT_REQUEST_PATH : remotePathRequests;
        this.fullUrl = "http://" + this.remoteHost + ":" + this.remotePort + this.remotePathRequests;
        LOGGER.info("Configuration loaded via {} for remote {}", source, this.fullUrl);
    }

    public int getRemotePort() {
        return remotePort;
    }
    public String getRemoteHost() {
        return remoteHost;
    }
    public String getRemotePathRequests() {
        return remotePathRequests;
    }

    @Override
    public String getDefaultRemoteUrl() {
        return fullUrl;
    }
}
