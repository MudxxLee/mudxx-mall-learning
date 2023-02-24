package com.mudxx.mall.tiny.mq.idempotent.common;

import cn.hutool.core.util.StrUtil;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author laiw
 * @date 2023/2/14 17:01
 */
@Data
@ToString
public class IdempotentResult implements Serializable {
    private static final long serialVersionUID = -2447598029081990954L;
    /**
     * 幂等消费结果
     */
    private String result;
    /**
     * 幂等消费结果描述
     */
    private String resultMsg;
    /**
     * 业务消费结果
     */
    private IdempotentBizResult bizResult;

    private IdempotentResult(String result, String resultMsg, IdempotentBizResult bizResult) {
        this.result = result;
        this.resultMsg = resultMsg;
        this.bizResult = bizResult;
    }

    public static IdempotentResult create(IdempotentResultStatus status, String resultMsg, IdempotentBizResult bizResult) {
        return new IdempotentResult(status.getStatus(), resultMsg, bizResult);
    }

    public static IdempotentResult create(IdempotentResultStatus status, String resultMsg) {
        return create(status, resultMsg, null);
    }

    public static IdempotentResult create(IdempotentResultStatus status, IdempotentBizResult bizResult) {
        return create(status, status.getDesc(), bizResult);
    }

    public static IdempotentResult create(IdempotentResultStatus status) {
        return create(status, status.getDesc());
    }

    public static IdempotentResult createSuccess() {
        return create(IdempotentResultStatus.SUCCEED);
    }

    public static IdempotentResult createSuccess(String resultMsg) {
        return create(IdempotentResultStatus.SUCCEED, resultMsg);
    }

    public static IdempotentResult createSuccess(IdempotentBizResult bizResult) {
        return create(IdempotentResultStatus.SUCCEED, bizResult);
    }

    public static IdempotentResult createSystemError(String resultMsg) {
        return create(IdempotentResultStatus.SYSTEM_ERROR, StrUtil.blankToDefault(resultMsg, IdempotentResultStatus.SYSTEM_ERROR.getDesc()));
    }

}
