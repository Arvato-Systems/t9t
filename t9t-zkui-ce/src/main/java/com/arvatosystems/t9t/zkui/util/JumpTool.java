/*
 * Copyright (c) 2012 - 2023 Arvato Systems GmbH
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
package com.arvatosystems.t9t.zkui.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zkoss.bind.BindUtils;

import com.arvatosystems.t9t.zkui.session.ApplicationSession;
import com.arvatosystems.t9t.zkui.viewmodel.beans.Navi;

import de.jpaw.bonaparte.pojos.api.LongFilter;
import de.jpaw.bonaparte.pojos.api.SearchFilter;
import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.bonaparte.pojos.api.UuidFilter;

public final class JumpTool {
    private JumpTool() { }

    public static void jump(final String targetZul, final String fieldName, final String id, final String backNaviLink) {
        final UnicodeFilter f = new UnicodeFilter(fieldName);
        f.setEqualsValue(id);
        jump (targetZul, f, backNaviLink);
    }

    public static void jump(final String targetZul, final String fieldName, final UUID ref, final String backNaviLink) {
        final UuidFilter f = new UuidFilter(fieldName);
        f.setEqualsValue(ref);
        jump (targetZul, f, backNaviLink);
    }

    public static void jump(final String targetZul, final String fieldName, final Long ref, final String backNaviLink) {
        final LongFilter f = new LongFilter(fieldName);
        f.setEqualsValue(ref);
        jump (targetZul, f, backNaviLink);
    }

    public static void jump(final String targetZul, final SearchFilter f, final String backNaviLink) {
        ApplicationSession.get().setFilterForPresetSearch(f);
        jump(targetZul, backNaviLink);
    }

    public static void jump(final String targetZul, final String backNaviLink) {
        final Navi navi = ApplicationUtil.getNavigationByLink(targetZul);
        final Map<String, Object> args = new HashMap<>();
        args.put("selected", navi);
        args.put("backNaviLink", backNaviLink);

        BindUtils.postGlobalCommand(null, null, "setSelectedFromJump", args);
    }
}
