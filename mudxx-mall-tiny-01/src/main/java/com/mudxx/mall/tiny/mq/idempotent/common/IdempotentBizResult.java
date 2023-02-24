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
     * 业务消费结果(true: 消费成功 false: 消费异常)
     */
    private Boolean result;
    /**
     * 是否删除消息记录(业务消费结果为false时是否删除消息记录)
     */
    private Boolean delete;
    /**
     * 是否延迟重试消息(删除消息记录为true时是否延迟重试消息)
     */
    private Boolean retry;

    private IdempotentBizResult(Boolean result, Boolean delete, Boolean retry) {
        this.result = result;
        this.delete = delete;
        this.retry = retry;
    }

    private static IdempotentBizResult create(Boolean result, Boolean delete, Boolean retry) {
        return new IdempotentBizResult(result, delete, retry);
    }

    public static IdempotentBizResult createSuccess() {
        return create(true, false, false);
    }

    private static IdempotentBizResult createFail(Boolean delete, Boolean retry) {
        return create(false, delete, retry);
    }

    public static IdempotentBizResult createFail() {
        return createFail(true, true);
    }

    public static IdempotentBizResult createFailDeleteNotRetry() {
        return createFail(true, false);
    }

    public static IdempotentBizResult createFailNotDeleteNotRetry() {
        return createFail(false, false);
    }

}
