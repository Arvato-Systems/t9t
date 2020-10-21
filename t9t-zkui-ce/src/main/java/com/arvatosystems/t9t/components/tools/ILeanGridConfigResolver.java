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

import java.util.List;

import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;

import de.jpaw.bonaparte.pojos.meta.FieldDefinition;
import de.jpaw.bonaparte.pojos.ui.UIFilter;

/** Class which supports dynamic screen building. An instance is created for a specific gridId,
 * and upon initialization it loads the config relevant for the current user.
 * @author BISC02
 *
 */
public interface ILeanGridConfigResolver {
    List<UIFilter>  getFilters();
    FieldDefinition getFieldDefinitionForPath(String fieldname);
    List <FieldDefinition> getVisibleColumns();
    List<String> getHeaders();
    List<Integer> getWidths();
    UILeanGridPreferences getGridPreferences();

    // methods to alter the configuration
    void newSort(String fieldname, boolean isDescending);
    void changeWidth(int columnIndex, int newWidth, int oldWidth);

    /** Move the column "from" to position "to". */
    void changeColumnOrder(int from, int to);

    /** Adds a field at the end of the grid. */
    void addField(String path);

    /** Removes a field from the grid. */
    void deleteField(int index);

    /** Hides a field from the grid (it is not removed, just not displayed) or restores it. */
    void setVisibility(int index, boolean isVisible);

    int getVariant();
    void setVariant(int variant);
    void save(boolean asTenantDefault);
    void deleteConfig(boolean tenantDefault);
    boolean reload();  // resets the config.   returns true if sort has changed (because then a new SEARCH must be done) else false
    int defaultWidth(FieldDefinition f);

    void setFilters(List<UIFilter> filters);
}
