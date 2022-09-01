package com.arvatosystems.t9t.base;

import com.arvatosystems.t9t.base.api.RequestParameters;

public interface IKafkaRequestTransmitter {
    void write(RequestParameters request, String partitionKey);
}
