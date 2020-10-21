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
package com.arvatosystems.t9t.tfi.component;

import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.arvatosystems.t9t.tfi.services.ReturnCodeException;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

public abstract class OnEventConfirmbox implements EventListener<Event> {

    private boolean showSuccessMessage = true;

    public OnEventConfirmbox() {
    }

    public OnEventConfirmbox(boolean showSuccessMessage) {
        super();
        this.showSuccessMessage = showSuccessMessage;
    }

    @Override
    public void onEvent(Event event) throws Exception {
        final ApplicationSession session = ApplicationSession.get();
        Messagebox.show(session.translate(null, "com.confirm.message"), session.translate(null, "com.confirm"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new EventListener<Event>() {
            @Override
            public void onEvent(Event evt) throws Exception {
                if (evt.getName().equals("onOK")) {
                    try {
                        command();
                        if (showSuccessMessage) {
                            Messagebox.show(session.translate(null, "success.transaction"), session.translate(null, "com.confirm"), Messagebox.OK, Messagebox.INFORMATION);
                        }
                    } catch (Exception e) {
                                String msg = (e instanceof ReturnCodeException) ? ((ReturnCodeException) e).getReturnCodeMessage().getReturnMessage() : e
                                        .getMessage();
                        msg = msg == null ? NullPointerException.class.getSimpleName() : msg;
                        Messagebox.show(msg, session.translate(null, "err.title"), Messagebox.OK, Messagebox.ERROR);
                    }
                } else {
                    //canceled
                }
            }
        });
    }

    abstract public void command() throws Exception;

}
