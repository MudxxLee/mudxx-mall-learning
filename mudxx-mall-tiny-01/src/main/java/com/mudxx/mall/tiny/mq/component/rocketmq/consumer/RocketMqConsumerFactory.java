package com.mudxx.mall.tiny.mq.component.rocketmq.consumer;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerBasicProperties;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.MessageListener;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;

/**
 * 消息 并发消费者
 * @author laiw
 * @date 2023/2/14 10:22
 */
@Slf4j
public class RocketMqConsumerFactory {

    public static DefaultMQPushConsumer getMonitorRocketMqConsumer(String nameServer, ConsumerProperties properties, MessageListener messageListener) {
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq nameServer is null !!!");
        }
        if (StrUtil.isBlank(properties.getGroupName())) {
            throw new RuntimeException("rocketmq consumer groupName is null !!!");
        }
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(properties.getGroupName());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setNamesrvAddr(nameServer);
        if(StrUtil.isNotBlank(properties.getInstanceName())) {
            consumer.setInstanceName(properties.getInstanceName());
        }
        ConsumerBasicProperties basicProperties = properties.getBasic();
        if(basicProperties != null) {
            if(basicProperties.getConsumeThreadMin() != null) {
                consumer.setConsumeThreadMin(basicProperties.getConsumeThreadMin());
            }
            if(basicProperties.getConsumeThreadMax() != null) {
                consumer.setConsumeThreadMax(basicProperties.getConsumeThreadMax());
            }
            if(basicProperties.getPullInterval() != null) {
                consumer.setPullInterval(basicProperties.getPullInterval());
            }
            if(basicProperties.getPullBatchSize() != null) {
                consumer.setPullBatchSize(basicProperties.getPullBatchSize());
            }
            if(basicProperties.getConsumeMessageBatchMaxSize() != null) {
                consumer.setConsumeMessageBatchMaxSize(basicProperties.getConsumeMessageBatchMaxSize());
            }
        }
        // 设置监听
        consumer.registerMessageListener(messageListener);
        String[] topics = properties.getTopic().split(",");
        String[] tags = properties.getTags().split(",");
        try {
            // 订阅多个topic,代码行增加consumer.subscribe(topic, tags)
            for (int i = 0; i < topics.length; i++) {
                consumer.subscribe(topics[i], tags[i]);
            }
            // 修改系统变量,保障启动多个不同ip的消费者 System.setProperty("rocketmq.client.name", "bizCommonSamplePushConsumer");
            consumer.start();
            log.info("rocketmq consumer is start !!! nameServer={}, groupName={}, topic={}", nameServer,
                    properties.getGroupName(), properties.getTopic());
        } catch (MQClientException e) {
            log.error("rocketmq consumer is error !!! nameServer={}, groupName={}, topic={}", nameServer,
                    properties.getGroupName(), properties.getTopic(), e);
            throw new RuntimeException("rocketmq consumer is error", e);
        }
        return consumer;
    }

}
