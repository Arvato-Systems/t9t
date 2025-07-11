package com.arvatosystems.t9t.ariba;

import com.arvatosystems.t9t.base.T9tConstants;
import com.arvatosystems.t9t.base.T9tException;

public class AribaException extends T9tException {

    private static final long serialVersionUID = 8429309564894896786L;

    private static final int CORE_OFFSET                = T9tConstants.EXCEPTION_OFFSET_AI;
    private static final int OFFSET                     = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;

    public static final int ARIBA_EXPORT_ERROR                          = OFFSET + 990;
    public static final int ARIBA_UNEXPECTED_RESULT_COLUMN_NUMBER       = OFFSET + 991;

    static {
        registerRange(CORE_OFFSET, false, T9tException.class, ApplicationLevelType.FRAMEWORK, "t9t framework core");

        registerCode(ARIBA_EXPORT_ERROR, "Error exporting metrics to ariba");
        registerCode(ARIBA_UNEXPECTED_RESULT_COLUMN_NUMBER, "Unexpected number of columns in the result set of an ariba view");
    }
}
