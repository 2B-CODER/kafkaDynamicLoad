package com.it.jxnu.kafka.consumer.factory;

import com.it.jxnu.business.model.ConsumerTopic;
import com.it.jxnu.kafka.consumer.entity.Consumer;
import com.it.jxnu.kafka.consumer.entity.ConsumerConfig;
import com.it.jxnu.business.dao.JdbcUtils;
import com.it.jxnu.kafka.consumer.entity.ConsumerTemplate;
import com.it.jxnu.kafka.consumer.entity.DateInsertion;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * User: xiang_xiang
 * Date: 2015-10-14
 * PackageName: com.yunva.kafka.consumer.factory
 * Github: https://github.com/ysm20140508
 */
public class ConsumerThread implements Runnable {
    private JdbcUtils jdbcUtils;
    private ConsumerConfig consumerConfig;

    public ConsumerThread(JdbcUtils jdbcUtils, ConsumerConfig consumerConfig) {
        this.jdbcUtils = jdbcUtils;
        this.consumerConfig = consumerConfig;
    }

    @Override
    public void run() {
        List<ConsumerTopic> consumerTopicList = jdbcUtils.getUpdatedConsumer();
        for (ConsumerTopic consumerTop : consumerTopicList) {
            if (consumerTop != null && StringUtils.isNotEmpty(consumerTop.getName())) {
                String threadName = consumerTop.getName();
                Integer threadStatus = consumerTop.getThreadStatus();
                Integer threads = consumerTop.getThreadCount();
                if (threadStatus == 0) {
                    if (ThreadFactory.getIntstant().containsKey(threadName)) {
                        Consumer consumer = (Consumer) ThreadFactory.getIntstant().get(threadName);
                        consumer.stopThread();
                        ThreadFactory.getIntstant().remove(threadName);
                        for (int index = threads; index > 0; index--) {
                            String indexThreadName = threadName + index;
                            if (ThreadFactory.getIntstant().containsKey(indexThreadName)) {
                                DateInsertion dateInsertion = (DateInsertion) ThreadFactory.getIntstant().get(indexThreadName);
                                dateInsertion.stopThread();
                                ThreadFactory.getIntstant().remove(indexThreadName);
                            }
                        }
                    }
                } else if (threadStatus == 1) {
                    if (!ThreadFactory.getIntstant().containsKey(threadName)) {
                        ConsumerTemplate consumerTemplate = new ConsumerTemplate();
                        consumerTemplate.setThreadName(threadName);
                        consumerTemplate.setThrads(threads);
                        consumerTemplate.setTopic(consumerTop.getTopic());
                        consumerTemplate.setGroupId(consumerTop.getGroup());
                        consumerTemplate.setTableName(consumerTop.getTableName());
                        consumerTemplate.setFieldName(consumerTop.getFieldName());
                        consumerTemplate.setInsertLimit(consumerTop.getInsertLimit());
                        consumerTemplate.setInsertHeartbeat(consumerTop.getInsertHeartbeat());
                        /*Consumer consumer = new Consumer(consumerConfig, consumerTemplate, jdbcUtils);
                        ExecutorService executorService = Executors.newSingleThreadExecutor();
                        executorService.submit(consumer);
                        ThreadFactory.getIntstant().put(threadName, consumer);*/
                    }
                }
                Integer id = consumerTop.getId();
                jdbcUtils.updateSuccess(id);
            }
        }
    }
}
