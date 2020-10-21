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
package com.arvatosystems.t9t.tfi.model.bean;

/**
 * Menu bean.
 * @author INCI02
 *
 */
public class Navi {
    private String  naviId;
    private int     position;
    private String  category;
    private String  name;
    private String  link;
    private int     hierarchy;
    private String  permission;
    private boolean closeGroup = false;
    private boolean menuItemVisible=true;
    private String  img;
    private String  subcategory;

    public Navi(String naviId, int position, String category, String subcategory, String name, String link, int hierarchy, String permission, boolean closeGroup, boolean menuItemVisible,String img) {
        super();
        this.naviId = naviId;
        this.position = position;
        this.category = category;
        this.subcategory = subcategory;
        this.name = name;
        this.link = link;
        this.hierarchy = hierarchy;
        this.permission = permission;
        this.closeGroup = closeGroup;
        this.menuItemVisible = menuItemVisible;
        this.img = img;
    }

    public Navi() {}

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

    public final int getHierarchy() {
        return hierarchy;
    }

    public final void setHierarchy(int hierarchy) {
        this.hierarchy = hierarchy;
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
     * @return the closeGroup
     */
    public boolean isCloseGroup() {
        return closeGroup;
    }

    /**
     * @param closeGroup the closeGroup to set
     */
    public void setCloseGroup(boolean closeGroup) {
        this.closeGroup = closeGroup;
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

    public final int getPosition() {
        return position;
    }

    public final void setPosition(int position) {
        this.position = position;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Navi [naviId=" + naviId + ", position=" + position
                + ", category=" + category + ", subcategory=\" + subcategory + \", name=" + name + ", link="
                + link + ", hierarchy=" + hierarchy + ", permission="
                + permission + ", closeGroup=" + closeGroup
                + ", menuItemVisible=" + menuItemVisible + ", img=" + img + "]";
    }



}
