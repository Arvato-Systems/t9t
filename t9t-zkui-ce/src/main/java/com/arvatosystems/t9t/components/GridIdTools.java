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
package com.arvatosystems.t9t.components;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;

import com.arvatosystems.t9t.base.CrudViewModel;
import com.arvatosystems.t9t.base.IViewModelContainer;
import com.arvatosystems.t9t.component.ext.IGridIdOwner;
import com.arvatosystems.t9t.component.ext.IPermissionOwner;
import com.arvatosystems.t9t.component.ext.IViewModelOwner;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.bonaparte.pojos.api.auth.Permissionset;

public class GridIdTools {
    private static final Logger LOGGER = LoggerFactory.getLogger(GridIdTools.class);

    public static <T extends Component> T getAnchestorOfType(Component p, Class<T> cls) {
        do {
            p = p.getParent();
            if (p == null)
                throw new RuntimeException("No anchestor of type " + cls.getSimpleName() + " provided");
        } while (!cls.isAssignableFrom(p.getClass()));
        return cls.cast(p);
    }

    /** Similar to getAnchestorOfType, but return null if non found. */
    public static <T extends Component> T findAnchestorOfType(Component p, Class<T> cls) {
        do {
            p = p.getParent();
            if (p == null)
                return null;
        } while (!cls.isAssignableFrom(p.getClass()));
        return cls.cast(p);
    }

    /** Returns the grid ID. If no grid Id is set, it asks the parent. */
    public static String enforceGridId(IGridIdOwner p) {
        String gridId = p.getGridId();
        if (gridId != null)
            return gridId;  // nothing to do
        // it is not set for p. We have to get it from an anchestor and then set it here.

        gridId = enforceGridId(getNextAnchestorWhichIsAGridIdOwner(p));
        p.setGridId(gridId);
        return gridId;
    }

    private static IGridIdOwner getNextAnchestorWhichIsAGridIdOwner(Component p) {
        do {
            p = p.getParent();
            if (p == null)
                throw new RuntimeException("No gridId provided in component nor any anchestor");
        } while (!(p instanceof IGridIdOwner));
        return (IGridIdOwner)p;
    }


//  public static Tabbox28 findTabbox28(Component p) {
//      while (p != null) {
//          if (p instanceof Tabbox28)
//              return (Tabbox28)p;
//          p = p.getParent();
//      }
//      throw new RuntimeException("No parent of type Tabbox28");
//  }

    /** Returns the grid ID. If no grid Id is set, it asks the parent. */
    public static String enforceViewModelId(IViewModelOwner p) {
        String viewModelId = p.getViewModelId();
        if (viewModelId != null)
            return viewModelId;  // nothing to do
        // it is not set for p. We have to get it from an anchestor and then set it here.

        viewModelId = enforceViewModelId(getNextAnchestorWhichIsAViewModelOwner(p));
        p.setViewModelId(viewModelId);
        return viewModelId;
    }

    private static IViewModelOwner getNextAnchestorWhichIsAViewModelOwner(Component p) {
        do {
            p = p.getParent();
            if (p == null)
                throw new RuntimeException("No viewModelId provided in component nor any anchestor");
        } while (!(p instanceof IViewModelOwner));
        return (IViewModelOwner)p;
    }



    public static CrudViewModel<BonaPortable, TrackingBase> getViewModelByGridId(String gridId) {
        String viewModelId = getViewModelIdByGridId(gridId);
        return getViewModelByViewModelId(viewModelId);
    }

    public static String getViewModelIdByGridId(String gridId) {
        String viewModelId = IViewModelContainer.VIEW_MODEL_BY_GRID_ID_REGISTRY.get(gridId);
        if (viewModelId == null) {
            LOGGER.error("No view model registered for grid ID {}", gridId);
            throw new RuntimeException("No view model registered for grid ID " + gridId);
        }
        return viewModelId;
    }

    @SuppressWarnings("unchecked")
    public static CrudViewModel<BonaPortable, TrackingBase> getViewModelByViewModelId(String viewModelId) {
        @SuppressWarnings("rawtypes")
        CrudViewModel crudViewModel = IViewModelContainer.CRUD_VIEW_MODEL_REGISTRY.get(viewModelId);
        if (crudViewModel == null) {
            LOGGER.error("No view model registered for view model ID {}", viewModelId);
            throw new RuntimeException("No view model registered for view model ID " + viewModelId);
        }
        return crudViewModel;
    }

    /** Finds a child component of specified type, and possibly given id. */
    public static <T extends Component> T findChildOfTypeAndId(Component component, String id, Class<T> cls) {
//        if (component instanceof IdSpace) {
//            Component found = component.query("#" + id);
//            if (found != null) return found;
//        }

        for (Component child : component.getChildren()) {
            if (cls.isAssignableFrom(child.getClass())) {
                if (id == null || id.equals(child.getId()))
                    return cls.cast(child);
                // not this, try children
                T found = findChildOfTypeAndId(child, id, cls);
                if (found != null)
                    return found;
            }
        }
        return null;
    }

    public static Permissionset getPermissionFromAnchestor(Component c) {
        IPermissionOwner po = getAnchestorOfType(c, IPermissionOwner.class);
        return po.getPermissions();
    }
}
