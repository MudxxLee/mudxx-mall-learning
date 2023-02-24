package com.mudxx.mall.tiny.mq.component.rocketmq.common;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;


/**
 * @author laiw
 * @date 2023/2/15 11:42
 */
@Data
@ToString
@Builder(toBuilder = true)
public class RocketMqCommonMessageExt implements Serializable {
    private static final long serialVersionUID = -5400128304951028763L;

    private String msgId;
    private String topic;
    private String tags;
    private String keys;
    private byte[] body;
    private int reconsumeTimes;

    public RocketMqCommonMessageExt() {

    }

    public RocketMqCommonMessageExt(String msgId, String topic, String tags, String keys, byte[] body, int reconsumeTimes) {
        this.msgId = msgId;
        this.topic = topic;
        this.tags = tags;
        this.keys = keys;
        this.body = body;
        this.reconsumeTimes = reconsumeTimes;
    }
}