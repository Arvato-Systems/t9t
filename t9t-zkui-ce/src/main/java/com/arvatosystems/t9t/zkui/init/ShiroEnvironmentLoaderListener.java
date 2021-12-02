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
package com.arvatosystems.t9t.zkui.init;

import java.util.Collection;

import javax.servlet.ServletContext;

import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.env.WebEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.zkui.security.realms.CommonRealm;
import com.arvatosystems.t9t.zkui.security.realms.DbRealm;

import de.jpaw.dp.Jdp;

public class ShiroEnvironmentLoaderListener extends EnvironmentLoaderListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShiroEnvironmentLoaderListener.class);

    protected final CommonRealm commonRealm = Jdp.getRequired(CommonRealm.class);

    public ShiroEnvironmentLoaderListener() {
        LOGGER.debug("ShiroEnvironmentLoaderListener CONSTRUCTOR (web.xml)");
    }

    @Override
    protected WebEnvironment createEnvironment(ServletContext sc) {
        WebEnvironment environment = super.createEnvironment(sc);

        Collection<Realm> configuredRealms = ((RealmSecurityManager) environment.getSecurityManager()).getRealms();

        for (Realm realm : configuredRealms) {
            if (realm instanceof DbRealm) {
                DbRealm configuredDbRealm = (DbRealm) realm;
                configuredDbRealm.setCommonRealm(this.commonRealm);
            }
        }
        LOGGER.info("Creating shiro environment with {} realms.", configuredRealms.size());

        return environment;
    }
}
