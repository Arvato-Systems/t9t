package com.arvatosystems.t9t.out.be.async;

import java.util.concurrent.atomic.AtomicInteger;

import com.arvatosystems.t9t.base.api.RequestParameters;
import com.arvatosystems.t9t.base.services.IInputQueuePartitioner;

import de.jpaw.dp.Named;
import de.jpaw.dp.Singleton;

@Named("roundRobin")
@Singleton
public class QueuePartitionerRoundRobin implements IInputQueuePartitioner {
    private final AtomicInteger counter = new AtomicInteger(-1);

    @Override
    public int determinePartitionKey(RequestParameters rq) {
        return getPreliminaryPartitionKey(null);
    }

    @Override
    public int getPreliminaryPartitionKey(String value) {
        return counter.incrementAndGet() % INITIAL_PARTITION_MODULUS;
    }
}
