/*
 * Copyright (c) 2012 - 2022 Arvato Systems GmbH
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
package com.arvatosystems.t9t.uiprefsv3.jpa.request;

import com.arvatosystems.t9t.base.api.ServiceResponse;
import com.arvatosystems.t9t.base.services.AbstractRequestHandler;
import com.arvatosystems.t9t.base.services.RequestContext;
import com.arvatosystems.t9t.base.uiprefs.UILeanGridPreferences;
import com.arvatosystems.t9t.uiprefsv3.jpa.entities.LeanGridConfigEntity;
import com.arvatosystems.t9t.uiprefsv3.jpa.persistence.ILeanGridConfigEntityResolver;
import com.arvatosystems.t9t.uiprefsv3.request.DeleteLeanGridConfigFieldReferenceRequest;
import de.jpaw.dp.Jdp;
import java.util.ArrayList;
import java.util.List;

public class DeleteLeanGridConfigFieldReferenceRequestHandler
    extends AbstractRequestHandler<DeleteLeanGridConfigFieldReferenceRequest> {

    private final ILeanGridConfigEntityResolver resolver = Jdp.getRequired(ILeanGridConfigEntityResolver.class);

    @Override
    public ServiceResponse execute(final RequestContext ctx, final DeleteLeanGridConfigFieldReferenceRequest request)
        throws Exception {
        List<LeanGridConfigEntity> gridConfigs = resolver.findByGridId(false, request.getGrid());
        List<String> fieldsToDelete = request.getFieldsToDelete();
        for (LeanGridConfigEntity gridConfig : gridConfigs) {
            UILeanGridPreferences gridPrefs = gridConfig.getGridPrefs();

            // remove fields and fieldWidths
            List<String> newFields = new ArrayList<>();
            List<Integer> newFieldWidths = new ArrayList<>();
            if (gridPrefs.getFields() != null) {
                boolean hasFieldWidths = gridPrefs.getFieldWidths() != null && !gridPrefs.getFieldWidths().isEmpty();
                for (int i = 0; i < gridPrefs.getFields().size(); i++) {
                    String field = gridPrefs.getFields().get(i);
                    if (!fieldsToDelete.contains(field)) {
                        newFields.add(field);
                        if (hasFieldWidths) {
                            newFieldWidths.add(gridPrefs.getFieldWidths().get(i));
                        }
                    }
                }
                gridPrefs.setFields(newFields);
                if (hasFieldWidths) {
                    gridPrefs.setFieldWidths(newFieldWidths);
                }
            }

            // remove filters
            gridPrefs.getFilters().removeIf(filter -> fieldsToDelete.contains(filter.getFieldName()));

            // remove sort
            if (fieldsToDelete.contains(gridPrefs.getSortColumn())) {
                gridPrefs.setSortColumn(null);
                gridPrefs.setSortDescending(null);
            }
            gridConfig.setGridPrefs(gridPrefs);
        }
        return ok();
    }
}
