/*
 * Copyright (c) 2012 - 2020 Arvato Systems GmbH
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
package com.arvatosystems.t9t.tfi.web.security;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;

public class JwtUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static JwtInfo getJwtPayload(String decodedJwt) {
        if (decodedJwt != null) {
            int firstDot = decodedJwt.indexOf(".");
            if (firstDot != -1) {
                firstDot++; // + 1
                int secondDot = decodedJwt.indexOf(".", firstDot);
                if (secondDot != -1) {
                    String encoded = decodedJwt.substring(firstDot, secondDot);
                    return JwtConverter.parseJwtInfo(new String(base64urlDecode(encoded), UTF8));
                }
            }
        }
        return null;
    }

    private static byte[] base64urlDecode(String str) {
        return Base64.getUrlDecoder().decode(str.getBytes(UTF8));
    }
}
