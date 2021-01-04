package com.arvatosystems.t9t.services;

import com.arvatosystems.t9t.components.tools.JumpTool;

import de.jpaw.bonaparte.pojos.api.UnicodeFilter;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class TitleBarSearch implements ITitleBarSearch {

    @Override
    public void search(String searchText) {
        UnicodeFilter f = new UnicodeFilter("name");
        f.setLikeValue(searchText.replace('*', '%'));
        JumpTool.jump("screens/user_admin/user28.zul", f, null);
    }
}
