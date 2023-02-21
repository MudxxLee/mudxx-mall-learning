package com.mudxx.mall.tiny.mq.component.rocketmq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.utils.SpringUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonDelay;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.IBizCommonSampleProcessor;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.sender.BizCommonMessageSender;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.Message;
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
@ConditionalOnProperty(prefix="rocketmq.biz-common.consumer.biz-sample.extra", value="enabled", havingValue="true")
public class BizCommonSampleConsumer {

    @Autowired
    private RocketMqPropertiesConfig properties;

    @Bean(name = "bizCommonSamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        String nameServer = properties.getNameServer();
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq nameServer is null !!!");
        }
        RocketMqPropertiesConfig.ConsumerBasicProperties basicProperties = properties.getBizCommon().getConsumer().getBizSample().getBasic();
        RocketMqPropertiesConfig.ConsumerExtraProperties extraProperties = properties.getBizCommon().getConsumer().getBizSample().getExtra();

        if (StrUtil.isBlank(basicProperties.getGroupName())) {
            throw new RuntimeException(StrUtil.format("[{}] rocketmq consumer groupName is null !!!", extraProperties.getLogHeader()));
        }
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(basicProperties.getGroupName());
        consumer.setMessageModel(MessageModel.CLUSTERING);
        consumer.setNamesrvAddr(nameServer);
        //consumer.setInstanceName(basicProperties.getInstanceName());
        consumer.setConsumeThreadMin(basicProperties.getConsumeThreadMin());
        consumer.setConsumeThreadMax(basicProperties.getConsumeThreadMax());
        consumer.setPullInterval(basicProperties.getPullInterval());
        consumer.setPullBatchSize(basicProperties.getPullBatchSize());
        consumer.setConsumeMessageBatchMaxSize(basicProperties.getConsumeMessageBatchMaxSize());
        // 设置监听
        consumer.registerMessageListener(new BizMessageListenerConcurrentlyImpl());
        String topic = basicProperties.getTopic();
        String tags = basicProperties.getTags();
        try {
            // 订阅多个topic,代码行增加consumer.subscribe(topic, tags)
            consumer.subscribe(topic, tags);
            // 修改系统变量,保障启动多个不同ip的消费者
            //System.setProperty("rocketmq.client.name", "bizCommonSamplePushConsumer");
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
                } catch (Exception e) {
                    int reconsumeTimes = messageExt.getReconsumeTimes();
                    log.error("msgId={} reconsumeTimes={}  消息处理异常: {}", messageExt.getMsgId(), reconsumeTimes, e.getMessage(), e);
                    if(reconsumeTimes < 2) {
                        // 重新投入队列
                        Message message = new Message(messageExt.getTopic(), messageExt.getTags(), messageExt.getKeys(), messageExt.getBody());
                        message.setDelayTimeLevel(RocketMqCommonDelay.S10.getLevel());
                        SpringUtil.getBean(BizCommonMessageSender.class).sendMessage(message);
                    } else {
                        // TODO 记录异常

                    }
                }
            }
            log.info("[{}:{}] rocketmq common-message listener push size: {}, consume time: {}ms", context.getMessageQueue().getTopic(),
                    context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }

}
