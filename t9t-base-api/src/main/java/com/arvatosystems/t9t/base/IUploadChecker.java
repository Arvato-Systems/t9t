package com.arvatosystems.t9t.base;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import jakarta.annotation.Nonnull;

public interface IUploadChecker {

    // check and throw exception if the provided media data contain any virus.
    void virusCheck(@Nonnull MediaData data);
}
