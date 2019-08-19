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
package com.arvatosystems.t9t.ssm.be.impl

import com.arvatosystems.t9t.base.services.IJdbcConnectionProvider
import de.jpaw.annotations.AddLogger
import de.jpaw.dp.Inject
import de.jpaw.dp.Named
import java.sql.SQLException
import org.quartz.utils.ConnectionProvider

// custom connection provider, used to avoid that connection information has to be maintained multiple times
// (for the JPA and for quartz)
@AddLogger
class JdbcConnectionProvider implements ConnectionProvider {
    @Inject @Named("independent") IJdbcConnectionProvider provider

    override getConnection() throws SQLException {
        return provider.JDBCConnection
    }

    override initialize() throws SQLException {
        LOGGER.info("Quartz connection provider initializing")
    }

    override shutdown() throws SQLException {
        LOGGER.info("Quartz connection provider shutting down")
    }
}
