package com.arvatosystems.t9t.components.tools;

import java.util.List;
import java.util.Set;

import org.zkoss.util.Pair;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.tfi.web.ApplicationSession;

/** Provide methods to create column config component, a list on the CE and a grouped list (grid component) on EE */
public interface IColumnConfigCreator {
    void createColumnConfigComponent(final ApplicationSession session, final Div parent, final UIGridPreferences uiGridPreferences, final Set<String> currentGrid);
    Pair<List<String>, List<String>> getAddRemovePairs(final ApplicationSession session);
}
