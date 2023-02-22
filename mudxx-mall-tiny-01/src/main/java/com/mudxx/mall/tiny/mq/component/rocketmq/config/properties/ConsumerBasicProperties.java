package com.mudxx.mall.tiny.mq.component.rocketmq.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/22 13:49
 */
@Data
public class ConsumerBasicProperties implements Serializable {
    private static final long serialVersionUID = -7489431771175659786L;
    /**
     * 内置消费线程池的core size    默认20
     */
    private Integer consumeThreadMin;
    /**
     * 消费线程池的max size   默认64
     */
    private Integer consumeThreadMax;
    /**
     * 拉取的间隔    默认0 单位毫秒
     */
    private Integer pullInterval;
    /**
     * 一次最大拉取的批量大小  默认32
     */
    private Integer pullBatchSize;
    /**
     * 批量消费的最大消息条数  默认1
     */
    private Integer consumeMessageBatchMaxSize;
}
