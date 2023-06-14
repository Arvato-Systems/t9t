package com.arvatosystems.t9t.core;

import com.arvatosystems.t9t.base.T9tException;

public class T9tCoreException extends T9tException {
    private static final long serialVersionUID = -1258793470293665993L;

    /*
     * Offset for all codes in this class.
     */
    private static final int CORE_OFFSET = 30000;
    private static final int OFFSET      = CORE_OFFSET + CLASSIFICATION_FACTOR * CL_PARAMETER_ERROR;

    // Update status - start update (1-10)
    public static final int UPDATE_STATUS_ALREADY_IN_PROGRESS   = OFFSET + 1;
    public static final int UPDATE_STATUS_INVALID_STATE         = OFFSET + 2;
    public static final int UPDATE_STATUS_PREREQUISITES         = OFFSET + 3;

    // Update status - finish update (11-20)
    public static final int FINISH_UPDATE_MUST_BE_IN_PROGRESS   = OFFSET + 11;

    static {
        registerCode(UPDATE_STATUS_ALREADY_IN_PROGRESS, "Ticket update already in progress.");
        registerCode(UPDATE_STATUS_INVALID_STATE, "Ticket isn't in valid state.");
        registerCode(UPDATE_STATUS_PREREQUISITES, "Not all prerequisite tickets are completed.");

        registerCode(FINISH_UPDATE_MUST_BE_IN_PROGRESS, "Ticket update must be in progress.");
    }
}
