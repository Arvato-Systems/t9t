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
package com.arvatosystems.t9t.zkui.components.basic;

import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class Button28 extends Button {
    private static final long serialVersionUID = 4013844852590749628L;
    public static final String PREFIX_BUTTON28 = "com.button";

    private final ApplicationSession session = ApplicationSession.get();
    protected boolean autoLabel = true;
    protected boolean autoblur = true;
    protected String resourceId;

    public Button28() {
        super();
        setSclass("button28");
    }

    /**
     * This is to fix button stays in focus after every clicks
     */
    public void onClick() {
        if (autoblur) {
            Clients.evalJavaScript("$(':button').trigger('blur');");
        }
    }

    /** The Button28 setImage() method disables the automatic label translation, if used before setId(). */
    @Override
    public void setImage(String image) {
        super.setImage(image);
        autoLabel = false;
    }

    /** The Button28 setId() method uses the id to obtain the button label. */
    @Override
    public void setId(String id) {
        super.setId(id);
        if (autoLabel)
            setLabel(session.translate(PREFIX_BUTTON28, id));
    }

    public String getResourceId() {
        return resourceId;
    }

    /** Defines a resource for which permissions are required. */
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
        Permissionset perms = session.getPermissions(resourceId);
        if (!perms.contains(OperationType.EXECUTE)) {
            setVisible(false);
            setDisabled(true);
        }
    }

    /** All the button use min-width instead of width to allow flex on button that has long text */
    @Override
    public void setWidth(String width) {
        String style = this.getStyle() == null ? "" : this.getStyle();
        this.setStyle(style + " min-width: " + width + ";");
        super.setWidth(width);
    }

    /**
     * Setting autoblur on button which will blur all buttons on the onClick event,
     * setting false to disable the feature, this is only required if this
     * components are embedded in the popup based components
     **/
    public void setAutoblur(final boolean autoblur) {
        this.autoblur = autoblur;
    }
}
