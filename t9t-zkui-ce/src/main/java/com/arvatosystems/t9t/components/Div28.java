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
package com.arvatosystems.t9t.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.component.ext.IPermissionOwner;

import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class Div28 extends Div implements IPermissionOwner {
    private static final Logger LOGGER = LoggerFactory.getLogger(Div28.class);
    private static final long serialVersionUID = -8089438052410249L;

    private final ApplicationSession session = ApplicationSession.get();
    protected Permissionset permissions = Permissionset.ofTokens();
    private String gridId;

    public Div28() {
        super();
        LOGGER.debug("new Div28() created");
        setWidth("100%");
        setSclass("caption");
    }

    @Override
    public void setId(String id) {
        LOGGER.debug("Div28() assigned  ID {}", id);
        super.setId(id);
    }

    @Override
    public void setVflex(String flex) {
        super.setVflex(flex);
    }

    @Override
    public Permissionset getPermissions() {
        return permissions;
    }

    public String getGridId() {
        return gridId;
    }

    public void setGridId(String gridId) {
        this.gridId = gridId;
        permissions = session.getPermissions(gridId);
    }
}
