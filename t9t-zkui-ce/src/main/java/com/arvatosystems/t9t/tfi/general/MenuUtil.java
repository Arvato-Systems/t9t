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
package com.arvatosystems.t9t.tfi.general;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;
import com.arvatosystems.t9t.tfi.web.ZulUtils;

import de.jpaw.bonaparte.pojos.api.OperationType;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class MenuUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(MenuUtil.class);

    /** MENU RELATED  **/
    private static final int NAVI_ID                = 0;
    private static final int CATEGORY               = 1;
    private static final int NAME                   = 2;
    private static final int LINK                   = 3;
    private static final int MENU_ITEM_VISIBLE      = 4;

    private static final String MENU_GROUP = "menu.group";
    private static final String DEFAULT = "defaults";

    public static void readMenuConfiguration(ApplicationSession as, final List<Navi> navis) {
        navis.clear();

        Boolean showMenuItem = Boolean.valueOf(ZulUtils.readConfig("menu.use_menu_icons"));

        Map<String, String> categories = new HashMap<String, String>(40); // caches the CATEGORY translations because they are likely to occur multiple times
        String[] menuConfigurations = ZulUtils.readConfig("menu.config").split("\\s*,\\s*"); // trim and split each element
        for (String menuConfigKey : menuConfigurations) {
            String menuConfig = ZulUtils.readConfig("menu."+menuConfigKey);
            //if (key.equals("$")) continue; // this is in case of switching languages there is an additional (default) set of entries. we need to skip it
            LOGGER.debug("Menu Config-key: {} - Config-value: \n{}", menuConfigKey, menuConfig);

            // Object[] menuLines = ZulUtils.i18nLabel("menu").split("\n");
            Object[] menuLines = menuConfig.split("\n");
            Object[] menuitems = null;

            for (Object menu : menuLines) {
                menuitems = String.valueOf(menu).toString().trim().split("\\s*,\\s*"); // trim and split each element
                Permissionset perms = as.getPermissions(String.valueOf(menuitems[NAVI_ID]));
                if (perms != null && perms.contains(OperationType.EXECUTE)) {
                    String thisCategory = String.valueOf(menuitems[CATEGORY]);

                    Navi navi = new Navi();

                    navi.setCategoryId(thisCategory);
                    navi.setNaviId(String.valueOf(menuitems[NAVI_ID]));
                    navi.setCategory(categories.computeIfAbsent(navi.getPrefixCategoryId(), (key) -> as.translate("menu.group", key)));
                    navi.setName(getMenuItemTranslation(as, navi, String.valueOf(menuitems[NAME])));
                    navi.setLink(String.valueOf(menuitems[LINK]));
                    navi.setPermission(String.valueOf(menuitems[NAVI_ID]));
                    navi.setMenuItemVisible(Boolean.valueOf(String.valueOf(menuitems[MENU_ITEM_VISIBLE])));

                    if (showMenuItem) {
                        navi.setImg(String.format("/img/menu/%s.png", menuitems[NAVI_ID]));
                    } else {
                        navi.setImg("/img/transparent.png");
                    }

                    navis.add(navi);
                }
            }
        }

        LOGGER.debug("Sorted menu is of size {}", navis.size());
    }

    private static String getMenuItemTranslation(ApplicationSession as, Navi navi, String menuItemName) {
        String fieldname = String.format("%s.%s", navi.getCategoryId(), menuItemName);
        String fallback = String.format("%s.%s", DEFAULT, menuItemName);

        return as.translateWithFallback(MENU_GROUP, fieldname, fallback);
    }

}
