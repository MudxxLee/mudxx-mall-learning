package com.mudxx.mall.tiny.mq.component.rocketmq.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/22 13:38
 */
@Data
public class ProducerProperties implements Serializable {
    private static final long serialVersionUID = -6889332087798739468L;
    /**
     * 生产者组
     */
    private String groupName;
    /**
     * 实例名
     */
    private String instanceName;
}
