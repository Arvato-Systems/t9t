/*
 * Copyright (c) 2012 - 2025 Arvato Systems GmbH
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

/**
 * This class defines constants for global configuration settings.
 */
public final class T9tConfigConstants {
    private T9tConfigConstants() { }

    public static final String CRUD_PROTECTED_VIEW    = "crud.protected.view.enable";
    public static final String CRUD_SHOW_MESSAGE      = "crud.show.message";
    public static final String GRID_AUTOCOLLAPSE      = "grid.results.autoCollapse";
    public static final String GRID_MARK_RED_ON_SORT  = "grid.markRedOnSort";
    public static final String GRID_LINE_WRAP         = "grid.lineWrap";
    public static final String GRID_DYNAMIC_COL_SIZE  = "grid.dynamicColumnSize";
    public static final String MENU_USE_ICONS         = "menu.useMenuIcons";
    public static final String EXPORT_DEFAULT_LIMIT   = "export.defaultLimit";
    public static final String HEADER_JUMP_BACK_BUTTON_DISABLE = "header.jumpBackButton.disable";
    public static final String HEADER_SEARCH_BOX_DISABLE = "header.searchBox.disable";
    public static final String DROPDOWN_DISPLAY_FORMAT = "dropdown.display.format";

    /** A boolean property to have the "today" button appear for date selection boxes globally. */
    public static final String DATE_PICKER_SHOW_TODAY = "datePicker.showToday";

    /** A string property to set a default height for three section's result overview area, default is 50%, eg, 30% or 200px */
    public static final String THREE_SECTION_DEFAULT_OVERVIEW_HEIGHT = "threeSections.overview.height";
}
