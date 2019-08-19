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
package com.arvatosystems.t9t.base.be.aws.events.tests;

import org.junit.Ignore;
import org.junit.Test;

import com.arvatosystems.t9t.base.services.IEventImpl;
import com.arvatosystems.t9t.jdp.Init;

import de.jpaw.bonaparte.api.media.MediaTypeInfo;
import de.jpaw.bonaparte.api.media.MediaTypes;
import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Jdp;

public class AwsTest {

    // mock caches on startup
    public static void initAndMockCaches() {
        Jdp.reset();
        Init.initializeT9t();
//      Jdp.bindClassToQualifier(NoTenantCache.class, ICacheTenant.class, null);
//      Jdp.bindClassToQualifier(NoUserCache.class, ICacheUser.class, null);
    }

    private MediaData createJson() {
        return new MediaData(MediaTypes.MEDIA_XTYPE_JSON, "{ \"hello\": 3.14 }", null, null);
    }

    @Test
    @Ignore
    public void testS3Put() throws Exception {
        initAndMockCaches();
        IEventImpl s3Impl = Jdp.getRequired(IEventImpl.class, "S3");

        s3Impl.asyncEvent("t9t-test:samples/test2.json", createJson(), MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_JSON));
    }

    @Ignore
    @Test
    public void testSQSSend() throws Exception {
        initAndMockCaches();
        IEventImpl sqsImpl = Jdp.getRequired(IEventImpl.class, "SQS");

        sqsImpl.asyncEvent("t9t-test", createJson(), MediaTypeInfo.getFormatByType(MediaTypes.MEDIA_XTYPE_JSON));
    }
}
