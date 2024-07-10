package com.arvatosystems.t9t.base.be.impl;

import com.arvatosystems.t9t.base.IUploadChecker;

import de.jpaw.bonaparte.pojos.api.media.MediaData;
import de.jpaw.dp.Fallback;
import de.jpaw.dp.Singleton;

@Singleton
@Fallback
public class UploadCheckerStub implements IUploadChecker {

    @Override
    public void virusCheck(MediaData data) {
    }

}
