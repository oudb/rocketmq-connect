package org.apache.rocketmq.connect.kafka.connector;


import io.openmessaging.connector.api.component.task.sink.SinkTaskContext;
import io.openmessaging.connector.api.data.*;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.connect.sink.ErrantRecordReporter;
import org.apache.kafka.connect.sink.SinkRecord;
import org.apache.rocketmq.connect.kafka.util.ConfigUtil;
import org.apache.rocketmq.connect.kafka.util.RecordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class RocketmqKafkaSinkTaskContext implements org.apache.kafka.connect.sink.SinkTaskContext {

    private static final Logger log = LoggerFactory.getLogger(RocketmqKafkaSinkTaskContext.class);

    private static final ExecutorService EXECUTOR_SERVICE  = Executors.newFixedThreadPool(1);

    private SinkTaskContext sinkTaskContext;

    public RocketmqKafkaSinkTaskContext(SinkTaskContext sinkTaskContext) {
        this.sinkTaskContext = sinkTaskContext;
    }

    @Override
    public Map<String, String> configs() {
        return ConfigUtil.keyValueConfigToMap(sinkTaskContext.configs());
    }

    @Override
    public void offset(Map<TopicPartition, Long> offsets) {

        Map<RecordPartition, RecordOffset> offsets2 = new HashMap<>(offsets.size());
        offsets.forEach((tp,offset) -> {
            Map<String, String> map = RecordUtil.getPartitionMap(tp.topic());
            map.put(RecordUtil.QUEUE_ID, tp.partition() + "");
            RecordPartition recordPartition = new RecordPartition(map);

            Map<String, String> offsetMap = new HashMap<>();
            offsetMap.put(RecordUtil.QUEUE_OFFSET, offset + "");
            RecordOffset recordOffset = new RecordOffset(offsetMap);

            offsets2.put(recordPartition, recordOffset);
        });
        sinkTaskContext.resetOffset(offsets2);
    }

    @Override
    public void offset(TopicPartition tp, long offset) {
        this.offset(Collections.singletonMap(tp, offset));
    }

    @Override
    public void timeout(long timeoutMs) {
        log.info("ignore timeout because not impl, timeoutMs:{}", timeoutMs);
    }

    @Override
    public Set<TopicPartition> assignment() {
        return sinkTaskContext.assignment()
                .stream()
                .map(RecordUtil::recordPartitionToTopicPartition)
                .collect(Collectors.toSet());
    }

    @Override
    public void pause(TopicPartition... partitions) {
        sinkTaskContext.pause(
                toRecordPartitions(partitions)
        );
    }

    @Override
    public void resume(TopicPartition... partitions) {
        sinkTaskContext.resume(
                toRecordPartitions(partitions)
        );
    }

    private List<RecordPartition> toRecordPartitions(TopicPartition... partitions){
        return Arrays.stream(partitions)
                .map(RecordUtil::topicPartitionToRecordPartition)
                .collect(Collectors.toList());
    }

    @Override
    public void requestCommit() {
        log.info("ignore requestCommit because not impl");
    }


    @Override
    public ErrantRecordReporter errantRecordReporter() {
        return new ErrantRecordReporter() {
            @Override
            public Future<Void> report(SinkRecord record, Throwable error) {

                 return EXECUTOR_SERVICE.submit(new Callable<Void>() {
                     @Override
                     public Void call() throws Exception {

                         Map<String, String> partitionMap = RecordUtil.getPartitionMap(record.topic());
                         partitionMap.put(RecordUtil.QUEUE_ID, record.kafkaPartition() + "");
                         RecordPartition recordPartition = new RecordPartition(partitionMap);

                         Map<String, String> offsetMap = new HashMap<>();
                         offsetMap.put(RecordUtil.QUEUE_OFFSET, record.kafkaOffset() + "");
                         RecordOffset recordOffset = new RecordOffset(offsetMap);

                         ConnectRecord connectRecord = new ConnectRecord(
                                 recordPartition, recordOffset, record.timestamp(),
                                 SchemaBuilder.string().build(), record.value()
                                 );
                         sinkTaskContext.errorRecordReporter().report(connectRecord, error);
                         return null;
                     }
                 });

            }
        };
    }
}
