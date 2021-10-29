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
package com.arvatosystems.t9t.tfi.model.bean;

/**
 * Menu bean.
 * @author INCI02
 *
 */
public class Navi {
    private String  naviId;
    private String  category;
    private String  name;
    private String  link;
    private String  permission;
    private boolean menuItemVisible = true;
    private String  img;
    private String  subcategory;
    private String  categoryId;

    public Navi(String naviId, String category, String subcategory, String name, String link, String permission, boolean menuItemVisible, String img) {
        super();
        this.naviId = naviId;
        this.category = category;
        this.subcategory = subcategory;
        this.name = name;
        this.link = link;
        this.permission = permission;
        this.menuItemVisible = menuItemVisible;
        this.img = img;
    }

    public Navi() { }

    /**
     * @return the menuItemVisible
     */
    public boolean isMenuItemVisible() {
        return menuItemVisible;
    }

    /**
     * @param menuItemVisible the menuItemVisible to set
     */
    public void setMenuItemVisible(boolean menuItemVisible) {
        this.menuItemVisible = menuItemVisible;
    }

    public final String getCategory() {
        return category;
    }

    public final void setCategory(String category) {
        this.category = category;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getLink() {
        return link;
    }

    public final void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the permission
     */
    public final String getPermission() {
        return permission;
    }

    /**
     * @param permission the permission to set
     */
    public final void setPermission(String permission) {
        this.permission = permission;
    }

    /**
     * @return the img
     */
    public String getImg() {
        return img;
    }

    /**
     * @param img the img to set
     */
    public void setImg(String img) {
        this.img = img;
    }

    /**
     * @return the naviId
     */
    public String getNaviId() {
        return naviId;
    }

    /**
     * @param naviId the naviId to set
     */
    public void setNaviId(String naviId) {
        this.naviId = naviId;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Navi [naviId=" + naviId + ", category=" + category
                + ", subcategory=\" + subcategory + \", name=" + name + ", link="
                + link + ", permission=" + permission + ", menuItemVisible=" + menuItemVisible
                + ", img=" + img + "]";
    }

    /**
     * This method get the first part of the category id.
     */
    public String getPrefixCategoryId() {
        int i = categoryId.indexOf(".");
        if (i > -1) {
            return categoryId.substring(0, i);
        }
        return categoryId;
    }

    /**
     * This method get all parts before the last part.
     */
    public String getFolderCategoryId() {
        return getCategoryIdBeforeLastDot(categoryId);
    }

    public static String getCategoryIdBeforeLastDot(String categoryId) {
        int i = categoryId.lastIndexOf(".");
        if (i > -1) {
            return categoryId.substring(0, i);
        }
        return categoryId;
    }

    public static String getCategoryIdAfterLastDot(String categoryId) {
        int i = categoryId.lastIndexOf(".");
        if (i > -1) {
            return categoryId.substring(i + 1);
        }
        return categoryId;
    }

}
