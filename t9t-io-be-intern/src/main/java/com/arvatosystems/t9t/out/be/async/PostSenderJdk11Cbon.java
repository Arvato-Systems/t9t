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
package com.arvatosystems.t9t.out.be.async;

import java.net.http.HttpResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.base.MessagingUtil;
import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.io.AsyncHttpResponse;
import com.arvatosystems.t9t.out.services.IAsyncSender;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.core.CompactByteArrayParser;
import de.jpaw.dp.Dependent;
import de.jpaw.dp.Named;
import de.jpaw.util.ApplicationException;

/**
 * The PostSender implements a simple client invocation via http POST of the JDK 11 HttpClient, using compact bonaparte serialization.
 */
@Dependent
@Named("jdk11cbon")
public class PostSenderJdk11Cbon extends AbstractPostSenderJdk11Cbon implements IAsyncSender {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPostSenderJdk11Json.class);

    @Override
    protected void parseResponse(final AsyncHttpResponse myResponse, final HttpResponse<byte[]> resp) {
        try {
            // obtain the payload (if any) and parse it
            final byte[] data = resp.body();
            final BonaPortable responseObject = (data == null || data.length == 0) ? null : new CompactByteArrayParser(data, 0, -1).readRecord();
            // if the response is a ServiceResponse, extract additional information
            if (responseObject instanceof ServiceResponse serviceResponse) {
                // myResponse.setHttpStatusMessage(null);
                myResponse.setClientReference(serviceResponse.getProcessRef() == null ? null : serviceResponse.getProcessRef().toString());
                myResponse.setClientReturnCode(serviceResponse.getReturnCode());
                myResponse.setErrorDetails(serviceResponse.getErrorDetails());
            } else if (responseObject != null) {
                myResponse.setClientReference(MessagingUtil.truncField(responseObject.ret$PQON(), AsyncHttpResponse.meta$$clientReference.getLength()));
                myResponse.setHttpStatusMessage(responseObject.ret$PQON());
            }
        } catch (final ApplicationException ae) {
            LOGGER.error("Cannot parse response: Code {}:  {}", ae.getErrorCode(), ae.getMessage());
            myResponse.setHttpStatusMessage("ApplicationException");
            myResponse.setClientReference("ApplicationException");
            myResponse.setClientReturnCode(ae.getErrorCode());
            myResponse.setErrorDetails(MessagingUtil.truncField(ae.getMessage(), AsyncHttpResponse.meta$$errorDetails.getLength()));
        } catch (final Exception e) {
            LOGGER.error("Response is not a parseable BonaPortable {}", e.getMessage());
            myResponse.setHttpStatusMessage("Exception");
            myResponse.setClientReference("Exception");
            myResponse.setErrorDetails(MessagingUtil.truncField(e.getMessage(), AsyncHttpResponse.meta$$errorDetails.getLength()));
//            myResponse.setHttpStatusMessage(null);
//            myResponse.setClientReference(null);
//            myResponse.setClientReturnCode(null);
//            myResponse.setErrorDetails(null);
            return;
        }
    }
}
