package com.mudxx.mall.tiny.mq.component.rocketmq.config.properties;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/22 13:49
 */
@Data
public class ConsumerExtraProperties implements Serializable {
    private static final long serialVersionUID = 4625749214791367288L;
    /**
     * 消费中的消息，在过期时间内不允许重复
     */
    private Long expireMilliSeconds;
    /**
     * 消息留存过期时间，在过期时间内不允许重复
     */
    private Long retainExpireMilliSeconds;
}
