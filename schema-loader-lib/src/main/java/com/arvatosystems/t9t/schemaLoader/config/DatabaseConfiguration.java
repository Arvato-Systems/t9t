/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.schemaLoader.config;

import java.sql.Driver;

public class DatabaseConfiguration {

    /** Database type */
    public String type;

    /** JDBC url for database connection */
    public String url;

    /** JDBC driver class to use */
    public Class<? extends Driver> driverClass;

    /** Username for connection */
    public String username;

    /** Password for connection */
    public String password;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Class<? extends Driver> getDriverClass() {
        return driverClass;
    }

    public void setDriverClass(Class<? extends Driver> driverClass) {
        this.driverClass = driverClass;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "DatabaseConfiguration [type=" + type + ", url=" + url + ", driverClass=" + driverClass + ", username=" + username + "]";
    }

}
