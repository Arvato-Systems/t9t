package com.arvatosystems.t9t.zkui.viewmodel;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.api.DataWithTracking;
import de.jpaw.bonaparte.pojos.api.TrackingBase;
import jakarta.annotation.Nonnull;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.ExecutionArgParam;
import org.zkoss.bind.annotation.Init;

public abstract class AbstractGenericObjectVM<GENERICOBJ extends BonaPortable, DTO extends BonaPortable, TRACKING extends TrackingBase,
    PARENT extends AbstractViewOnlyVM<DTO, TRACKING>> extends AbstractViewOnlyVM<DTO, TRACKING> {

    protected GENERICOBJ genericObject;

    @Init(superclass = true)
    public void init(@ExecutionArgParam("parentVm") final PARENT parentVm) {
        parentVm.setChildViewModel(this);
    }

    @Override
    protected void loadData(@Nonnull final DataWithTracking<DTO, TRACKING> dwt) {
        genericObject = getGenericObject(dwt);
        BindUtils.postNotifyChange(this, "genericObject");
    }

    @Override
    protected void enrichData(@Nonnull final DTO data) {
        populateGenericObject(data);
    }

    public GENERICOBJ getGenericObject() {
        return genericObject;
    }

    protected abstract GENERICOBJ getGenericObject(@Nonnull DataWithTracking<DTO, TRACKING> dwt);

    protected abstract void populateGenericObject(@Nonnull DTO data);
}
