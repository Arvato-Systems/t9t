package com.arvatosystems.t9t.kafka.service;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.PartitionInfo;

public interface IKafkaRebalancer extends ConsumerRebalanceListener {
    void init(List<PartitionInfo> partitions);
}
