package com.mudxx.mall.tiny.mq.component.rocketmq.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/22 13:49
 */
@Data
public class ConsumerProperties implements Serializable {
    private static final long serialVersionUID = 342768164887787699L;
    /**
     * 消费者组
     */
    private String groupName;
    /**
     * 实例名
     */
    private String instanceName;
    /**
     * 主题
     */
    private String topic;
    /**
     * 标签
     */
    private String tags;
    /**
     * 基本属性
     */
    private ConsumerBasicProperties basic;
    /**
     * 扩展属性
     */
    private ConsumerExtraProperties extra;
}
