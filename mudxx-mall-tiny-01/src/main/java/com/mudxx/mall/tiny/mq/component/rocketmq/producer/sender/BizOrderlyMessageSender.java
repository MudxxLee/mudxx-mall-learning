package com.mudxx.mall.tiny.mq.component.rocketmq.producer.sender;

import cn.hutool.core.util.HashUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz.BizOrderlyProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.MessageQueueSelector;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 消息 生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
@Service
public class BizOrderlyMessageSender {

    @Autowired(required = false)
    private BizOrderlyProducer bizOrderlyProducer;

    /**
     * 发送消息 顺序消费
     */
    public RocketMqCommonResult sendOrderlyMessage(RocketMqCommonMessage commonMessage) {
        String keys = StrUtil.isBlank(commonMessage.getKeys()) ? StrUtil.uuid() : commonMessage.getKeys();
        return sendOrderlyMessage(commonMessage.getTopic(), commonMessage.getTags(), keys, commonMessage.getBody());
    }

    /**
     * 发送消息 顺序消费
     */
    public RocketMqCommonResult sendOrderlyMessage(String topic, String tags, String keys, byte[] body) {
        beforeSendCheck();
        try {
            Message message = new Message(topic, tags, keys, body);
            SendResult sendResult = bizOrderlyProducer.getProducer().send(message, new MessageQueueSelector(){
                @Override
                public MessageQueue select(List<MessageQueue> arg0, Message arg1, Object arg2) {
                    int queueIndex = Math.abs(HashUtil.rotatingHash(keys, arg0.size()));
                    if(queueIndex < 0) {
                        queueIndex = 0;
                    }
                    return arg0.get(queueIndex);
                }
            }, keys);
            log.info("[keys={}] rocketmq orderly-message send result: {}", message.getKeys(), JSONObject.toJSONString(sendResult));
            if(StrUtil.equals(SendStatus.SEND_OK.toString(), sendResult.getSendStatus().toString())) {
                return RocketMqCommonResult.success(message.getTopic(), message.getTags(), message.getKeys());
            }
        } catch (Exception e) {
            log.error("[keys={}] rocketmq orderly-message send error: {}", keys, e.getMessage(), e);
        }
        return RocketMqCommonResult.error(topic, tags, keys);
    }

    private void beforeSendCheck() {
        if (ObjectUtils.isEmpty(bizOrderlyProducer)) {
            throw new RuntimeException("rocketmq 生产者未启动 !!!");
        }
    }
}
