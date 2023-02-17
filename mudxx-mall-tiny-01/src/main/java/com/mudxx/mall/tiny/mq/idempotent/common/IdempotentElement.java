package com.mudxx.mall.tiny.mq.idempotent.common;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * 消息幂等基础信息
 * @author laiw
 * @date 2023/2/17 09:39
 */
@Data
@ToString
public class IdempotentElement implements Serializable {
    private static final long serialVersionUID = 1799439878140424675L;
    /**
     * 消费的应用名（可以用消费者组名称）
     */
    private String applicationName;
    /**
     * 消息来源的topic（不同topic消息不会认为重复）
     */
    private String topic;
    /**
     * 消息的tag（同一个topic不同的tag，就算去重键一样也不会认为重复），没有tag则存""字符串
     */
    private String tags;
    /**
     * 消息的唯一键（建议使用业务主键）
     */
    private String msgUniqKey;
}
