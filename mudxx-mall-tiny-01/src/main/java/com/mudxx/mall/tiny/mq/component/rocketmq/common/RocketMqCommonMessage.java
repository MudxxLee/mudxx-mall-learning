package com.mudxx.mall.tiny.mq.component.rocketmq.common;

import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/15 11:42
 */
@Data
public class RocketMqCommonMessage implements Serializable {
    private static final long serialVersionUID = 2109571307660866799L;
    private String topic;
    private String tags;
    private String keys;
    private byte[] body;

    public RocketMqCommonMessage() {

    }

    public RocketMqCommonMessage(String topic, String tags, String keys, byte[] body) {
        this.topic = topic;
        this.tags = tags;
        this.keys = keys;
        this.body = body;
    }

    public RocketMqCommonMessage(RocketMqPropertiesConfig.ConsumerProperties properties) {
        this.topic = properties.getBasic().getTopic();
        this.tags = properties.getBasic().getTags();
    }

    public RocketMqCommonMessage(RocketMqPropertiesConfig.ConsumerProperties properties, byte[] body) {
        this.topic = properties.getBasic().getTopic();
        this.tags = properties.getBasic().getTags();
        this.body = body;
    }
}
