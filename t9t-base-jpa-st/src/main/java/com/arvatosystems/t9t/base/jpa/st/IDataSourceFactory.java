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
package com.arvatosystems.t9t.base.jpa.st;

import javax.sql.DataSource;

/**
 * Factory for creation of data sources during system startup.
 *
 * @author TWEL006
 */
public interface IDataSourceFactory {

    /**
     * Create and initialize a new data source with every call.
     *
     * @return DataSource ready to use
     */
    DataSource createDataSource() throws Exception;

}
