package com.arvatosystems.t9t.tfi.web.security;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import de.jpaw.bonaparte.api.auth.JwtConverter;
import de.jpaw.bonaparte.pojos.api.auth.JwtInfo;
import de.jpaw.bonaparte.util.impl.MarshallerBonaparte;

public class JwtUtils {

    private static final Charset UTF8 = StandardCharsets.UTF_8;

    public static JwtInfo getJwtPayload(String decodedJwt) {
        MarshallerBonaparte marshaller = new MarshallerBonaparte();
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
