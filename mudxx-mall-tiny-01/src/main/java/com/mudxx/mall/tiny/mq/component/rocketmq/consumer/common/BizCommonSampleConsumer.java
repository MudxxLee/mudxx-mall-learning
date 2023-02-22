package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.utils.SpringUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizCommonPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerBasicProperties;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerProperties;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor.IBizCommonSampleProcessor;
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
    private BizCommonPropertiesConfig propertiesConfig;

    @Bean(name = "bizCommonSamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        String nameServer = propertiesConfig.getNameServer();
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException(StrUtil.format("rocketmq nameServer is null (consumer={})", this.getClass().getSimpleName()));
        }
        ConsumerProperties properties = propertiesConfig.getConsumer().getBizSample();
        if (StrUtil.isBlank(properties.getGroupName())) {
            throw new RuntimeException(StrUtil.format("rocketmq consumer groupName is null (consumer={})", this.getClass().getSimpleName()));
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
        consumer.registerMessageListener(new BizMessageListenerConcurrentlyImpl());
        String topic = properties.getTopic();
        String tags = properties.getTags();
        try {
            // 订阅多个topic,代码行增加consumer.subscribe(topic, tags)
            consumer.subscribe(topic, tags);
            // 修改系统变量,保障启动多个不同ip的消费者
            //System.setProperty("rocketmq.client.name", "bizCommonSamplePushConsumer");
            consumer.start();
            log.info("rocketmq consumer is start !!! nameServer={}, groupName={}, topic={}", nameServer, properties.getGroupName(), topic);
        } catch (MQClientException e) {
            log.error("rocketmq consumer is error !!! nameServer={}, groupName={}, topic={}", nameServer, properties.getGroupName(), topic, e);
            throw new RuntimeException("rocketmq consumer is error", e);
        }
        return consumer;
    }

    private static class BizMessageListenerConcurrentlyImpl implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeConcurrentlyContext context) {
            TimeInterval timer = DateUtil.timer().restart();
            IBizCommonSampleProcessor processor = SpringUtil.getBean(IBizCommonSampleProcessor.class);
            for (MessageExt messageExt : messageExtList) {
                try {
                    // for循环单线程消费
                    RocketMqCommonMessageExt commonMessageExt = RocketMqCommonMessageExt.builder()
                            .msgId(messageExt.getMsgId())
                            .topic(messageExt.getTopic())
                            .tags(messageExt.getTags())
                            .keys(messageExt.getKeys())
                            .body(messageExt.getBody())
                            .build();
                    processor.consumeMessage(commonMessageExt, true);
                } catch (Throwable e) {
                    int reconsumeTimes = messageExt.getReconsumeTimes();
                    log.error("msgId={} reconsumeTimes={}  消息处理异常: {}", messageExt.getMsgId(), reconsumeTimes, e.getMessage(), e);
                    if(reconsumeTimes < 2) {
                        // 稍后重试
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        // TODO 记录异常
                        
                    }
                }
            }
            log.info("[{}:{}] rocketmq common-message listener 本次拉取数量: {} 耗时{}ms", context.getMessageQueue().getTopic(),
                    context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }

}
