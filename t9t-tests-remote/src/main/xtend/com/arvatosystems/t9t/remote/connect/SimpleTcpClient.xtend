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
package com.arvatosystems.t9t.remote.connect

import de.jpaw.annotations.AddLogger
import de.jpaw.socket.SessionInfo
import de.jpaw.util.ByteBuilder
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.nio.charset.StandardCharsets
import javax.net.ssl.SSLSocket
import javax.net.ssl.SSLSocketFactory

@AddLogger
class SimpleTcpClient {

    protected final InetAddress addr;
    protected final Socket conn;

    def private static void printSocketInfo(SSLSocket s) {
        LOGGER.info("Socket class: " + s.getClass());
        LOGGER.info("   Remote address = " + s.getInetAddress().toString());
        LOGGER.info("   Remote port = " + s.getPort());
        LOGGER.info("   Local socket address = " + s.getLocalSocketAddress().toString());
        LOGGER.info("   Local address = " + s.getLocalAddress().toString());
        LOGGER.info("   Local port = " + s.getLocalPort());
        LOGGER.info("   Need client authentication = " + s.getNeedClientAuth());
        val ss = s.getSession();
        LOGGER.info("   Cipher suite = " + ss.getCipherSuite());
        LOGGER.info("   Protocol = " + ss.getProtocol());
    }

    new(String hostname, int port, boolean useSsl) throws IOException {
        addr = InetAddress.getByName(hostname);

        if (useSsl) {
            val  f = SSLSocketFactory.getDefault() as SSLSocketFactory
            conn = f.createSocket(addr, port);
            val c = conn as SSLSocket
            printSocketInfo(c);
            c.startHandshake();
            val session = c.getSession();
            SessionInfo.logSessionInfo(session, "Server");
        } else {
            conn = new Socket(addr, port);
        }
    }

    def ByteBuilder doRawIO(byte [] request, int offset, int len) throws Exception {
        conn.getOutputStream().write(request, 0, if (len < 0) request.length else len);
        val ins = conn.inputStream
        val int CHUNK_SIZE = 10240
        val buff = new ByteBuilder(CHUNK_SIZE, StandardCharsets.UTF_8)
        val tmpBuff = newByteArrayOfSize(CHUNK_SIZE)
        var int newbytes = 1;
        while (newbytes > 0) {
            newbytes = ins.read(tmpBuff, 0, CHUNK_SIZE);
            if (newbytes > 0) {
                buff.write(tmpBuff, 0, newbytes)
                if (tmpBuff.get(newbytes-1) == 10)
                    newbytes = 0  // done
            }
        }
        return buff;
    }

    // close the connection
    def void close() throws IOException {
        conn.close();
    }
}
