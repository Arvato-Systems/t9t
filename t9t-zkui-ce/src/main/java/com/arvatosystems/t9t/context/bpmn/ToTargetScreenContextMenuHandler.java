package com.arvatosystems.t9t.context.bpmn;

import com.arvatosystems.t9t.bpmn.ProcessExecutionStatusDTO;
import com.arvatosystems.t9t.components.Grid28;
import com.arvatosystems.t9t.context.IGridContextMenu;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
@Named("bpmnStatus.ctx.toTargetScreen")
public class ToTargetScreenContextMenuHandler implements IGridContextMenu<ProcessExecutionStatusDTO> {

    @Override
    public boolean isEnabled(DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        // there is no target screen in t9t
        return false;
    }

    @Override
    public void selected(Grid28 lb, DataWithTracking<ProcessExecutionStatusDTO, TrackingBase> dwt) {
        // there is no target screen in t9t: do a NOOP
    }
}
