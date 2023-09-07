package com.arvatosystems.t9t.cluster.be.kafka;

import java.util.Map;

import org.apache.kafka.common.TopicPartition;

import com.arvatosystems.t9t.cluster.be.kafka.KafkaSimplePartitionOrderedRequestProcessor.PartitionMonitor;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

/**
 * {@link MeterBinder} for exporting partition monitor information of {@link KafkaSimplePartitionOrderedRequestProcessor}.<br>
 * Sample output:
 * <pre>
 * t9t_kafka_partitions_processing_time_total{topic="candaRequestToMAIN",} 1763.0
 * t9t_kafka_partitions_paused_total{topic="candaRequestToMAIN",} 3.0
 * t9t_kafka_partitions_tasks_pending_total{topic="candaRequestToMAIN",} 8.0
 * t9t_kafka_partitions_tasks_total{topic="candaRequestToMAIN",} 15.0
 * </pre>
 */
public class KafkaClusterPartitionMetrics implements MeterBinder {

    private static final String PREFIX = "t9t.kafka.partitions";

    private static final String METRIC_PAUSED_TOTAL = PREFIX + ".paused.total";
    private static final String METRIC_PAUSED_TOTAL_DESC = "Total number of paused partitions";

    private static final String METRIC_PROCESSING_TIME_TOTAL = PREFIX + ".processing.time.total";
    private static final String METRIC_PROCESSING_TIME_TOTAL_DESC = "Total processing time of all partitions";

    private static final String METRIC_PROCESSING_TIME_AVG = PREFIX + ".processing.time.avg";
    private static final String METRIC_PROCESSING_TIME_AVG_DESC = "Average processing time of all partitions";

    private static final String METRIC_TASKS_PENDING_TOTAL = PREFIX + ".tasks.pending.total";
    private static final String METRIC_TASKS_PENDING_TOTAL_DESC = "Total number of pending tasks";

    private static final String METRIC_TASKS_TOTAL = PREFIX + ".tasks.total";
    private static final String METRIC_TASKS_TOTAL_DESC = "Total number of currently assigned tasks";

    private static final String TAG_TOPIC = "topic";

    private final Map<TopicPartition, PartitionMonitor> partitionStatusTable;
    private final String topicName;

    public KafkaClusterPartitionMetrics(final Map<TopicPartition, PartitionMonitor> partitionStatusTable, final String topicName) {
        this.partitionStatusTable = partitionStatusTable;
        this.topicName = topicName;
    }

    @Override
    public void bindTo(final MeterRegistry registry) {
        Gauge.builder(METRIC_PAUSED_TOTAL, () -> this.partitionStatusTable.size()).description(METRIC_PAUSED_TOTAL_DESC).tag(TAG_TOPIC, topicName)
                .register(registry);

        Gauge.builder(METRIC_PROCESSING_TIME_TOTAL, () -> {
            long total = 0L;
            for (final PartitionMonitor monitor : this.partitionStatusTable.values()) {
                total += monitor.getProcessingTime();
            }
            return total;
        }).description(METRIC_PROCESSING_TIME_TOTAL_DESC).tag(TAG_TOPIC, topicName).register(registry);

        Gauge.builder(METRIC_PROCESSING_TIME_AVG, () -> {
            long total = 0L;
            for (final PartitionMonitor monitor : this.partitionStatusTable.values()) {
                total += monitor.getProcessingTime();
            }
            if (total > 0 && this.partitionStatusTable.size() > 0) {
                return total / this.partitionStatusTable.size();
            }
            return total;
        }).description(METRIC_PROCESSING_TIME_AVG_DESC).tag(TAG_TOPIC, topicName).register(registry);

        Gauge.builder(METRIC_TASKS_PENDING_TOTAL, () -> {
            int total = 0;
            for (final PartitionMonitor monitor : this.partitionStatusTable.values()) {
                total += monitor.getNumPending();
            }
            return total;
        }).description(METRIC_TASKS_PENDING_TOTAL_DESC).tag(TAG_TOPIC, topicName).register(registry);

        Gauge.builder(METRIC_TASKS_TOTAL, () -> {
            int total = 0;
            for (final PartitionMonitor monitor : this.partitionStatusTable.values()) {
                total += monitor.getNumRecords();
            }
            return total;
        }).description(METRIC_TASKS_TOTAL_DESC).tag(TAG_TOPIC, topicName).register(registry);
    }

}
