package com.mudxx.mall.tiny.mq.component.rocketmq.producer.sender;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonDelay;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz.BizCommonProducer;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

/**
 * 消息 生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
@Service
public class BizCommonMessageSender {

    @Autowired(required = false)
    private BizCommonProducer bizCommonProducer;

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
        String keys = StrUtil.isBlank(commonMessage.getKeys()) ? StrUtil.uuid() : commonMessage.getKeys();
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
    private RocketMqCommonResult sendMessage(Message message) {
        beforeSendCheck();
        try {
            SendResult sendResult = bizCommonProducer.getProducer().send(message);
            log.info("[keys={}] rocketmq common-message send result: {}", message.getKeys(), JSONObject.toJSONString(sendResult));
            if(StrUtil.equals(SendStatus.SEND_OK.toString(), sendResult.getSendStatus().toString())) {
                return RocketMqCommonResult.success(message.getTopic(), message.getTags(), message.getKeys());
            }
        } catch (Exception e) {
            log.error("[keys={}] rocketmq common-message send error: {}", message.getKeys(), e.getMessage(), e);
        }
        return RocketMqCommonResult.error(message.getTopic(), message.getTags(), message.getKeys());
    }

    private void beforeSendCheck() {
        if (ObjectUtils.isEmpty(bizCommonProducer)) {
            throw new RuntimeException("rocketmq 生产者未启动 !!!");
        }
    }

}
