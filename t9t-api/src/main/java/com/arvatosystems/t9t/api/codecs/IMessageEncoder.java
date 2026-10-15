package com.arvatosystems.t9t.api.codecs;

import de.jpaw.bonaparte.core.BonaPortable;
import de.jpaw.bonaparte.pojos.meta.ObjectReference;

public interface IMessageEncoder<O extends BonaPortable, T> {
    T encode(O obj, ObjectReference di);
}
