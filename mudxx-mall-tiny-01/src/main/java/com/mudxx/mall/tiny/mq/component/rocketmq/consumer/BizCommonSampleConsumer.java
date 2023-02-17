package com.mudxx.mall.tiny.mq.component.rocketmq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.utils.SpringUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.IBizCommonSampleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消息 并发消费者
 * @author laiw
 * @date 2023/2/14 10:22
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix="rocketmq.biz-common.consumer.biz-sample", value="enabled", havingValue="true")
public class BizCommonSampleConsumer {

    @Autowired
    private RocketMqPropertiesConfig propertiesConfig;

    @Bean(name = "bizCommonSamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        String nameServer = propertiesConfig.getNameServer();
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq nameServer is null !!!");
        }
        RocketMqPropertiesConfig.ConsumerProperties bizSample = propertiesConfig.getBizCommon().getConsumer().getBizSample();
        if (StrUtil.isBlank(bizSample.getGroupName())) {
            throw new RuntimeException(StrUtil.format("[{}] rocketmq consumer groupName is null !!!", bizSample.getLogHeader()));
        }
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(bizSample.getGroupName());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setNamesrvAddr(nameServer);
        //consumer.setInstanceName(bizSample.getInstanceName());
        consumer.setConsumeThreadMin(bizSample.getConsumeThreadMin());
        consumer.setConsumeThreadMax(bizSample.getConsumeThreadMax());
        consumer.setPullInterval(bizSample.getPullInterval());
        consumer.setPullBatchSize(bizSample.getPullBatchSize());
        consumer.setConsumeMessageBatchMaxSize(bizSample.getConsumeMessageBatchMaxSize());
        // 设置监听
        consumer.registerMessageListener(new BizMessageListenerConcurrentlyImpl());
        String topic = bizSample.getTopic();
        String tags = bizSample.getTags();
        try {
            // 订阅多个topic,代码行增加consumer.subscribe(topic, tags)
            consumer.subscribe(topic, tags);
            // 修改系统变量,保障启动多个不同ip的消费者
            //System.setProperty("rocketmq.client.name", "bizCommonSamplePushConsumer");
            consumer.start();
            log.info("[{}] rocketmq consumer is start ... nameServer={}, groupName={}, topic={}",
                    bizSample.getLogHeader(), nameServer, bizSample.getGroupName(), topic);
        } catch (MQClientException e) {
            log.error("[{}] rocketmq consumer is error !!! nameServer={}, groupName={}, topic={}",
                    bizSample.getLogHeader(), nameServer, bizSample.getGroupName(), topic, e);
            throw new RuntimeException(e);
        }
        return consumer;
    }

    private static class BizMessageListenerConcurrentlyImpl implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeConcurrentlyContext context) {
            TimeInterval timer = DateUtil.timer().restart();
            IBizCommonSampleProcessor processor = SpringUtil.getBean(IBizCommonSampleProcessor.class);
            for (MessageExt message : messageExtList) {
                // for循环单线程消费
                processor.consumeMessage(message.getMsgId(), message.getTopic(), message.getTags(), message.getKeys(), message.getBody());
            }
            log.info("[{}:{}] rocketmq common-message listener push size: {}, consume time: {}ms", context.getMessageQueue().getTopic(),
                    context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }

}
