package com.mudxx.mall.tiny.mq.component.rocketmq.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/14 17:01
 */
@Data
public class RocketMqCommonResult implements Serializable {
    private static final long serialVersionUID = -2447598029081990954L;

    private boolean result;
    private String topic;
    private String tags;
    private String keys;

    protected RocketMqCommonResult() {

    }

    protected RocketMqCommonResult(boolean result, String topic, String tags, String keys) {
        this.result = result;
        this.topic = topic;
        this.tags = tags;
        this.keys = keys;
    }

    public static RocketMqCommonResult success(String keys) {
        return success(null, null, keys);
    }

    public static RocketMqCommonResult success(String topic, String tags) {
        return success(topic, tags, null);
    }

    public static RocketMqCommonResult success(String topic, String tags, String keys) {
        return new RocketMqCommonResult(Boolean.TRUE, topic, tags, keys);
    }

    public static RocketMqCommonResult error(String keys) {
        return error(null, null, keys);
    }

    public static RocketMqCommonResult error(String topic, String tags) {
        return error(topic, tags, null);
    }

    public static RocketMqCommonResult error(String topic, String tags, String keys) {
        return new RocketMqCommonResult(Boolean.FALSE, topic, tags, keys);
    }


}
