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
package com.arvatosystems.t9t.viewmodel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zul.Messagebox;

import com.arvatosystems.t9t.components.GenericVM;
import com.arvatosystems.t9t.email.api.SendTestEmailRequest;
import com.arvatosystems.t9t.email.api.SendTestEmailResponse;
import com.arvatosystems.t9t.services.T9TRemoteUtils;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.bonaparte.pojos.api.media.MediaType;
import de.jpaw.dp.Jdp;
import de.jpaw.util.ApplicationException;

@Init(superclass = true)
public class SendTestEmailVM extends GenericVM {
    private static final Logger LOGGER = LoggerFactory.getLogger(SendTestEmailVM.class);
    protected final ApplicationSession session = ApplicationSession.get();
    protected final T9TRemoteUtils t9tremoteUtils = Jdp.getRequired(T9TRemoteUtils.class);


    private String toEmail = null;
    protected String VM_ID = "SendTestEmailVM";

    @AfterCompose
    public void afterCompose() {
    }

    @Command
        public void sendEmail() {
            if (!validateBeforeSend()) {
                return;
            }
            MediaData media = new MediaData();
            media.setMediaType(MediaType.TEXT);
            media.setText("Lorem ipsum");
            SendTestEmailRequest request = new SendTestEmailRequest();
            request.setEmailAddress(toEmail);
            request.setEmailSubject("test");
            request.setEmailBody(media);

            LOGGER.debug("Sending Email");
            SendTestEmailResponse res = t9tremoteUtils.executeExpectOk(request, SendTestEmailResponse.class);
            if (res.getReturnCode() == ApplicationException.SUCCESS) {
                Messagebox.show(session.translate(VM_ID,"sentsuccessfully"), session.translate(VM_ID,"emailsent"), Messagebox.OK,
                        Messagebox.INFORMATION);

            }
        }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    protected boolean validateBeforeSend() {
        if (toEmail == null || !validateEmail(toEmail)) {
            Messagebox.show(session.translate(VM_ID,"err.invalidemail"), session.translate(VM_ID,"com.badinput"), Messagebox.OK,
                    Messagebox.INFORMATION);
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        return email.matches(".+@.+\\.[a-z]+");
    }

}
