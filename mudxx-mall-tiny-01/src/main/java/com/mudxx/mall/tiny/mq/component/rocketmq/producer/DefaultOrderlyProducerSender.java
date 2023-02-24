package com.mudxx.mall.tiny.mq.component.rocketmq.producer;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;

/**
 * 顺序消息发送者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
public class DefaultOrderlyProducerSender {

    private final DefaultMQProducer producer;

    public DefaultOrderlyProducerSender(String nameServer, ProducerProperties properties){
        this.producer = RocketMqProducerFactory.getDefaultMQProducer(nameServer, properties);
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    /**
     * 发送消息 顺序消费
     */
    public RocketMqCommonResult sendOrderlyMessage(RocketMqCommonMessage commonMessage) {
        String keys = StrUtil.blankToDefault(commonMessage.getKeys(), IdUtil.simpleUUID());
        return sendOrderlyMessage(commonMessage.getTopic(), commonMessage.getTags(), keys, commonMessage.getBody());
    }

    /**
     * 发送消息 顺序消费
     */
    public RocketMqCommonResult sendOrderlyMessage(String topic, String tags, String keys, byte[] body) {
        try {
            Message message = new Message(topic, tags, keys, body);
            SendResult sendResult = getProducer().send(message, (arg0, arg1, arg2) -> {
                int queueIndex = Math.abs(HashUtil.rotatingHash(keys, arg0.size()));
                if(queueIndex < 0) {
                    queueIndex = 0;
                }
                return arg0.get(queueIndex);
            }, keys);
            if(StrUtil.equals(SendStatus.SEND_OK.toString(), sendResult.getSendStatus().toString())) {
                return RocketMqCommonResult.success(message.getTopic(), message.getTags(), message.getKeys());
            }
        } catch (Exception e) {
            log.error("[keys={}] rocketmq orderly-message send error: {}", keys, e.getMessage(), e);
        }
        return RocketMqCommonResult.error(topic, tags, keys);
    }

}
