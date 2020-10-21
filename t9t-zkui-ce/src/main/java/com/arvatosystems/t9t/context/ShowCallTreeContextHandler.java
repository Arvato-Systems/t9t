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
package com.arvatosystems.t9t.context;

import java.util.List;

import com.arvatosystems.t9t.base.misc.Info;
import com.arvatosystems.t9t.base.request.ProcessStatusDTO;
import com.arvatosystems.t9t.base.request.StackLevel;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.components.ModalWindows;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Named("processStatus.ctx.showCallTree")
public class ShowCallTreeContextHandler implements IGridContextMenu<ProcessStatusDTO> {

    @Override
    public void selected(Grid28 lb, DataWithTracking<ProcessStatusDTO, TrackingBase> dwt) {
        ProcessStatusDTO dto = dwt.getData();
        List<StackLevel> stack = dto.getCallStack();
        if (stack != null) {
            int depth = -1;
            StringBuilder tree = new StringBuilder();
            for (StackLevel sl : stack) {
                if (depth >= 0) {
                    for (int i = 0; i < depth; ++i)
                        tree.append("    ");
                    tree.append("  +-");
                }
                tree.append(sl.getPqon());
                tree.append(String.format(": %d calls, progress %d: %s\n",
                        sl.getNumberOfCallsThisLevel(),
                        sl.getProgressCounter(),
                        sl.getStatusText() != null ? sl.getStatusText() : ""
                    ));
                ++depth;
            }
            Info info = new Info();
            info.setText(tree.toString());
            ModalWindows.runModal("/context/info28.zul", lb.getParent(), info, false, (d) -> {});
        }
    }
}
