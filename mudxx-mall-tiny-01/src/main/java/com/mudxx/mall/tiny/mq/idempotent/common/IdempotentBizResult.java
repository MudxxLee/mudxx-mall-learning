package com.mudxx.mall.tiny.mq.idempotent.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/14 17:01
 */
@Data
public class IdempotentBizResult implements Serializable {
    private static final long serialVersionUID = -2447598029081990954L;
    /**
     * 业务消费结果(true: 执行消息标记 false: 执行消息删除)
     */
    private Boolean result;
    /**
     * 消息是否重试(业务消费结果为false时是否重试消息)
     */
    private Boolean retry;

    private IdempotentBizResult(Boolean result, Boolean retry) {
        this.result = result;
        this.retry = retry;
    }

    public static IdempotentBizResult create(Boolean result, Boolean retry) {
        return new IdempotentBizResult(result, retry);
    }

    public static IdempotentBizResult createSuccess() {
        return create(true, false);
    }

    public static IdempotentBizResult createFail(Boolean retry) {
        return create(false, retry);
    }

    public static IdempotentBizResult createDefaultFail() {
        return create(false, true);
    }

}
