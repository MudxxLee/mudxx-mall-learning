package com.mudxx.mall.tiny.mq.component.rocketmq.producer;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonDelay;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

/**
 * 默认消息发送者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
public class DefaultCommonProducerSender {

    private final DefaultMQProducer producer;

    public DefaultCommonProducerSender(String nameServer, ProducerProperties properties){
        this.producer = RocketMqProducerFactory.getDefaultMQProducer(nameServer, properties);
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    /**
     * 发送消息 实时消费
     */
    public RocketMqCommonResult sendCommonMessage(RocketMqCommonMessage commonMessage) {
        return sendDelayMessage(commonMessage, null);
    }

    /**
     * 发送消息 延迟消费
     */
    public RocketMqCommonResult sendDelayMessage(RocketMqCommonMessage commonMessage, RocketMqCommonDelay delayTimeLevel) {
        String keys = StrUtil.blankToDefault(commonMessage.getKeys(), IdUtil.simpleUUID());
        String topic = commonMessage.getTopic();
        String tags = commonMessage.getTags();
        byte[] body = commonMessage.getBody();
        Message message = new Message(topic, tags, keys, body);
        if(delayTimeLevel != null) {
            // 延时消息
            message.setDelayTimeLevel(delayTimeLevel.getLevel());
        }
        return sendMessage(message);
    }

    /**
     * 发送消息
     */
    public RocketMqCommonResult sendMessage(Message message) {
        try {
            SendResult sendResult = getProducer().send(message);
            if(StrUtil.equals(SendStatus.SEND_OK.toString(), sendResult.getSendStatus().toString())) {
                return RocketMqCommonResult.success(message.getTopic(), message.getTags(), message.getKeys());
            }
        } catch (Exception e) {
            log.error("[keys={}] rocketmq common-message send error: {}", message.getKeys(), e.getMessage(), e);
        }
        return RocketMqCommonResult.error(message.getTopic(), message.getTags(), message.getKeys());
    }

}
