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
package com.arvatosystems.t9t.components.tools;

import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.BindUtils;

import com.arvatosystems.t9t.tfi.general.ApplicationUtil;
import com.arvatosystems.t9t.tfi.model.bean.Navi;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;

public class JumpTool {
    public static void jump(String targetZul, String fieldName, String id, String backNaviLink) {
        UnicodeFilter f = new UnicodeFilter(fieldName);
        f.setEqualsValue(id);
        jump (targetZul, f, backNaviLink);
    }

    public static void jump(String targetZul, String fieldName, Long ref, String backNaviLink) {
        LongFilter f = new LongFilter(fieldName);
        f.setEqualsValue(ref);
        jump (targetZul, f, backNaviLink);
    }

    public static void jump(String targetZul, SearchFilter f, String backNaviLink) {
        ApplicationSession.get().setFilterForPresetSearch(f);
        jump(targetZul,backNaviLink);
    }

    public static void jump(String targetZul, String backNaviLink) {
        Navi navi = ApplicationUtil.getNavigationByLink(targetZul);

        Map<String, Object> args = new HashMap<>();
        args.put("selected", navi);
        args.put("backNaviLink", backNaviLink);

        BindUtils.postGlobalCommand(null, null, "setSelectedFromJump", args);
    }
}
