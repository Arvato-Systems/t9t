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
package com.arvatosystems.t9t.remote.tests.simple

import com.arvatosystems.t9t.auth.tests.setup.SetupUserTenantRole
import com.arvatosystems.t9t.authc.api.GetTenantsRequest
import com.arvatosystems.t9t.authc.api.GetTenantsResponse
import com.arvatosystems.t9t.base.request.AutonomousTransactionRequest
import com.arvatosystems.t9t.base.request.BatchRequest
import com.arvatosystems.t9t.base.request.ExecuteAsyncRequest
import com.arvatosystems.t9t.base.request.LogMessageRequest
import com.arvatosystems.t9t.base.request.PauseRequest
import com.arvatosystems.t9t.base.request.PerformUntilRequest
import com.arvatosystems.t9t.base.request.PingRequest
import com.arvatosystems.t9t.remote.connect.Connection
import com.arvatosystems.t9t.remote.connect.ConnectionTypes
import com.arvatosystems.t9t.remote.connect.TcpConnection
import com.arvatosystems.t9t.uiprefs.request.GridConfigRequest
import com.arvatosystems.t9t.uiprefs.request.GridConfigResponse
import de.jpaw.bonaparte.util.ToStringHelper
import java.util.UUID
import org.junit.Test

class ITPing {
    @Test
    def void pingTest() {
        val dlg = new Connection

        dlg.okIO(new PingRequest)
    }

    @Test
    def void pingViaJsonTest() {
        val dlg = new Connection(true, ConnectionTypes.JSON)

        dlg.okIO(new PingRequest)
    }

    @Test
    def void selectTenantsAndSwitchTenantViaJsonTest() {
        val dlg = new Connection(true, ConnectionTypes.JSON)

        val tenants = dlg.typeIO(new GetTenantsRequest, GetTenantsResponse)
        for (t: tenants.tenants) {
            println('''Tenant «t.tenantId» has ref «t.tenantRef» and name «t.name»''')
        }

        // switch tenant
        dlg.switchTenant("MOON", 0)
    }

    @Test
    def void tcpPingTest() {
        val dlg = new TcpConnection

        dlg.okIO(new PingRequest)
    }

    @Test
    def void pingLessLogTest() {
        val dlg = new Connection(false)

        dlg.okIO(new PingRequest)
    }

    @Test
    def void pingTestMoreLog() {
        val dlg = new Connection(true)

        dlg.okIO(new PingRequest)
    }

    @Test
    def void asyncPingTest() {
        val dlg = new Connection

        dlg.okIO(new ExecuteAsyncRequest(new PingRequest))
    }

    @Test
    def void pingForNewUserTest() {
        val dlg = new Connection

        val setup = new SetupUserTenantRole(dlg)

        val newKey = UUID.randomUUID
        setup.createUserTenantRole("ping", newKey, true)

        dlg.okIO(new PingRequest)
    }

    @Test
    def void helloTest() {
        val dlg = new Connection

        dlg.okIO(new LogMessageRequest("hello, world"))
    }

    @Test
    def void t9tGridConfigTest() {
        val dlg = new Connection

        val result = dlg.typeIO(new GridConfigRequest => [
            gridId = "roleToPermission"
        ], GridConfigResponse)
        println('''Grid config result is «ToStringHelper.toStringML(result.gridConfig)»''')
    }

    @Test
    def void t9tGridConfigJsonTest() {
        val dlg = new Connection(true, ConnectionTypes.JSON)

        val result = dlg.typeIO(new GridConfigRequest => [
            gridId = "roleToPermission"
        ], GridConfigResponse)
        println('''Grid config result is «ToStringHelper.toStringML(result.gridConfig)»''')
    }

    @Test
    def void helloAsyncAsyncAsyncTest() {
        val dlg = new Connection

        dlg.okIO(
            new ExecuteAsyncRequest(
                new ExecuteAsyncRequest(
                    new ExecuteAsyncRequest(
                        new ExecuteAsyncRequest(
                            new ExecuteAsyncRequest(
                                new ExecuteAsyncRequest(
                                    new LogMessageRequest("hello, world")
                                )
                            )
                        )
                    )
                )
            )
        )
    }

    @Test
    def void tryProcessStatusTest() {
        val dlg = new Connection

        dlg.okIO(
            new BatchRequest => [
                commands = #[
                    new LogMessageRequest("hello, world"),
                    new PauseRequest => [ delayInMillis = 3000 ],
                    new LogMessageRequest("new message"),
                    new PauseRequest => [ delayInMillis = 3000 ],
                    new LogMessageRequest("processing X"),
                    new PauseRequest => [ delayInMillis = 3000 ],
                    new LogMessageRequest("processing ZZZ")
                ]
            ]
        )
    }

    @Test
    def void tryAnotherProcessStatusTest() {
        val dlg = new Connection

        dlg.okIO(
            new BatchRequest => [
                new BatchRequest => [
                    new BatchRequest => [
                        new BatchRequest => [
                            commands = #[
                                new LogMessageRequest("hello, world"),
                                new PauseRequest => [delayInMillis = 3000],
                                new LogMessageRequest("new message"),
                                new PauseRequest => [delayInMillis = 3000],
                                new LogMessageRequest("processing X"),
                                new PauseRequest => [delayInMillis = 3000],
                                new LogMessageRequest("processing ZZZ")
                            ]
                        ]
                    ]
                ]
            ]
        )
    }

    def protected runSomeTime(String text, int delay) {
        return new BatchRequest => [
            commands = #[
                new PauseRequest => [delayInMillis = delay],
                new PauseRequest => [delayInMillis = delay + 100]
            ]
        ]
    }

    @Test
    def void nestedCallStackDemoTest() {
        val dlg = new Connection

        dlg.okIO(
            new PerformUntilRequest => [
                // stopAt  = new Instant(System.currentTimeMillis + 60L * 1000L)
                maxNumberOfMilliseconds = 60000
                // maxNumberOfRuns = 50
                request = new BatchRequest => [
                    commands = #[
                        new LogMessageRequest("processing top level batch"),
                        new ExecuteAsyncRequest => [
                            asyncRequest = runSomeTime("X Async", 888)
                        ],
                        new PauseRequest => [delayInMillis = 250],
                        runSomeTime("X Sync", 690),
                        new PauseRequest => [delayInMillis = 350],
                        new AutonomousTransactionRequest => [
                            request = runSomeTime("X Autonomous", 710)
                        ]
                ]
            ]
        ])
    }
}
