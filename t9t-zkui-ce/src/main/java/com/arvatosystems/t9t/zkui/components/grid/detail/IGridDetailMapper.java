package com.arvatosystems.t9t.zkui.components.grid.detail;

import java.util.List;

import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import jakarta.annotation.Nonnull;

public interface IGridDetailMapper<MAIN extends DataWithTracking<?, ?>, DETAIL extends DataWithTracking<?, ?>> {

    @Nonnull List<DETAIL> mapDetails(@Nonnull MAIN main);
}
