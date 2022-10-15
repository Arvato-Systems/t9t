package com.arvatosystems.t9t.out.be.async;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.IInputQueuePartitioner;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("zero")
@Singleton
public class QueuePartitionerZero implements IInputQueuePartitioner {

    @Override
    public int determinePartitionKey(RequestParameters rq) {
        return 0;
    }

    @Override
    public int getPreliminaryPartitionKey(String value) {
        return 0;
    }
}
