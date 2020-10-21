package com.arvatosystems.t9t.tfi.web;

public class RefUtils {
    /**
     * Factor to multiply the value obtained from sequences with.
     */
    public static final long KEY_FACTOR = 10000L;

    /**
     * Offset to be added to scaled keys generated in the backup location.
     */
    public static final int OFFSET_BACKUP_LOCATION = 5000;

    /** Offsets of sequences which are unscaled (don't contain a RTTI). */
    public static final int OFFSET_UNSCALED_T9T           = 5000;
    public static final int OFFSET_UNSCALED_APPLICATION   = 6000;
    public static final int OFFSET_UNSCALED_CUSTOMIZATION = 7000;

    /**
     * Retrieves the run time type information from a generated key.
     *
     * @param id
     *            The key to extract the RTTI from.
     * @return the RTTI
     */
    public static int getRtti(long id) {
        int rttiPlusLocation = (int) (id % KEY_FACTOR);
        return rttiPlusLocation >= OFFSET_BACKUP_LOCATION ? rttiPlusLocation - OFFSET_BACKUP_LOCATION : rttiPlusLocation;
    }

}
