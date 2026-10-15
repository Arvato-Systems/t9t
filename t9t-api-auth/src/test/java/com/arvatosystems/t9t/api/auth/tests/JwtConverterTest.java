package com.arvatosystems.t9t.api.auth.tests;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import de.jpaw.bonaparte.core.BonaPortableFactory;
import de.jpaw.bonaparte.core.BonaparteJsonEscaper;
import de.jpaw.bonaparte.util.ToStringHelper;

import com.arvatosystems.t9t.api.OperationType;
import com.arvatosystems.t9t.api.auth.JwtConverter;
import com.arvatosystems.t9t.api.auth.JwtInfo;
import com.arvatosystems.t9t.api.auth.JwtPayload;
import com.arvatosystems.t9t.api.auth.Permissionset;
import com.arvatosystems.t9t.api.auth.UserLogLevelType;

public class JwtConverterTest {

    private JwtInfo createSampleInfo(Instant now, UUID sessionId) {
        // test one field per data type at least
        JwtInfo info = new JwtInfo();
        info.setUserId("John");
        info.setUserRef(4711L);
        info.setIssuedAt(now);
        info.setLogLevel(UserLogLevelType.REQUESTS);
        info.setPermissionsMax(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.SEARCH));
        info.setResource("B.test");
        info.setResourceIsWildcard(Boolean.TRUE);
        info.setSessionId(sessionId);
        return info;
    }

    @Test
    public void testInfoToMap() throws Exception {
        BonaPortableFactory.addToPackagePrefixMap("api", "com.arvatosystems.t9t.api");     // migrated from bonaparte
        Instant now = JwtConverter.lastFullSecond();
        UUID sessionId = UUID.randomUUID();

        // test one field per data type at least
        JwtInfo info = createSampleInfo(now, sessionId);

        Map<String, Object> jsonMap = JwtConverter.asMap(info);
        Assertions.assertEquals(8, jsonMap.size());
        Assertions.assertEquals("John", jsonMap.get("sub"));
        Assertions.assertEquals(now, jsonMap.get("iat"));
        Assertions.assertEquals(Long.valueOf(4711L), jsonMap.get("u"));
        Assertions.assertEquals(UserLogLevelType.REQUESTS.ordinal(), jsonMap.get("l"));
        Assertions.assertEquals(Permissionset.ofTokens(OperationType.EXECUTE, OperationType.SEARCH).getBitmap(), jsonMap.get("pu"));
        Assertions.assertEquals(Boolean.TRUE, jsonMap.get("w"));
        Assertions.assertEquals("B.test", jsonMap.get("p"));
        Assertions.assertEquals(sessionId, jsonMap.get("o"));

        // test the conversion back to the JwtInfo
        JwtPayload payload = JwtConverter.parsePayload(jsonMap);
        JwtInfo info2 = JwtConverter.parseJwtInfo(payload);
        System.out.println(ToStringHelper.toStringML(payload));
        System.out.println(ToStringHelper.toStringML(info2));
        Assertions.assertEquals(info, info2);
    }

    @Test
    public void testInfoJson() {
        Instant now = JwtConverter.lastFullSecond();
        UUID sessionId = UUID.randomUUID();

        // test one field per data type at least
        JwtInfo info = createSampleInfo(now, sessionId);
        String json = BonaparteJsonEscaper.asJson(JwtConverter.asMap(info));
        System.out.println(json);
    }
}
