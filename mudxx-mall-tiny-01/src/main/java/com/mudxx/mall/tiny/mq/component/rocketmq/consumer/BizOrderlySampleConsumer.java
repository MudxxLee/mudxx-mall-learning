package com.mudxx.mall.tiny.mq.component.rocketmq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.utils.SpringUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.IBizOrderlySampleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
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
@ConditionalOnProperty(prefix="rocketmq.biz-orderly.consumer.biz-sample.extra", value="enabled", havingValue="true")
public class BizOrderlySampleConsumer {

    @Autowired
    private RocketMqPropertiesConfig properties;

    @Bean(name = "bizOrderlySamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        String nameServer = properties.getNameServer();
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq nameServer is null !!!");
        }
        RocketMqPropertiesConfig.ConsumerBasicProperties basicProperties = properties.getBizOrderly().getConsumer().getBizSample().getBasic();
        RocketMqPropertiesConfig.ConsumerExtraProperties extraProperties = properties.getBizOrderly().getConsumer().getBizSample().getExtra();

        if (StrUtil.isBlank(basicProperties.getGroupName())) {
            throw new RuntimeException(StrUtil.format("[{}] rocketmq consumer groupName is null !!!", extraProperties.getLogHeader()));
        }
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(basicProperties.getGroupName());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setNamesrvAddr(nameServer);
        //consumer.setInstanceName(bizSample.getInstanceName());
        consumer.setConsumeThreadMin(basicProperties.getConsumeThreadMin());
        consumer.setConsumeThreadMax(basicProperties.getConsumeThreadMax());
        consumer.setPullInterval(basicProperties.getPullInterval());
        consumer.setPullBatchSize(basicProperties.getPullBatchSize());
        consumer.setConsumeMessageBatchMaxSize(basicProperties.getConsumeMessageBatchMaxSize());
        consumer.registerMessageListener(new BizMessageListenerOrderlyImpl());
        String topic = basicProperties.getTopic();
        String tags = basicProperties.getTags();
        try {
            consumer.subscribe(topic, tags);
            // 修改系统变量,保障启动多个不同ip的消费者
            //System.setProperty("rocketmq.client.name", "bizOrderlySamplePushConsumer");
            consumer.start();
            log.info("[{}] rocketmq consumer is start ... nameServer={}, groupName={}, topic={}",
                    extraProperties.getLogHeader(), nameServer, basicProperties.getGroupName(), topic);
        } catch (MQClientException e) {
            log.error("[{}] rocketmq consumer is error !!! nameServer={}, groupName={}, topic={}",
                    extraProperties.getLogHeader(), nameServer, basicProperties.getGroupName(), topic, e);
            throw new RuntimeException(e);
        }
        return consumer;
    }

    private static class BizMessageListenerOrderlyImpl implements MessageListenerOrderly {

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeOrderlyContext context) {
            TimeInterval timer = DateUtil.timer().restart();
            IBizOrderlySampleProcessor processor = SpringUtil.getBean(IBizOrderlySampleProcessor.class);
            for (MessageExt message : messageExtList) {
                // for循环单线程消费
                processor.consumeMessage(message.getMsgId(), message.getTopic(), message.getTags(), message.getKeys(), message.getBody());
            }
            log.info("[{}:{}] rocketmq orderly-message listener push size: {}, consume time: {}ms",
                    context.getMessageQueue().getTopic(), context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

}
