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
package com.arvatosystems.t9t.zkui.services;

import java.util.List;
import java.util.Set;

import org.zkoss.util.Pair;
import org.zkoss.zul.Div;

import com.arvatosystems.t9t.base.uiprefs.UIGridPreferences;
import com.arvatosystems.t9t.zkui.session.ApplicationSession;

/** Provide methods to create column config component, a list on the CE and a grouped list (grid component) on EE */
public interface IColumnConfigCreator {
    void createColumnConfigComponent(ApplicationSession session, Div parent, UIGridPreferences uiGridPreferences, Set<String> currentGrid);
    Pair<List<String>, List<String>> getAddRemovePairs(ApplicationSession session);
    void selectColumns(Set<String> columnNames);
    void unselectColumns(Set<String> columnNames);
}
