package com.mudxx.mall.tiny.mq.idempotent.common;

import cn.hutool.core.util.StrUtil;

/**
 * @author laiw
 * @date 2023/2/14 17:01
 */
public enum IdempotentResultStatus  {
    /**
     * 幂等执行结果
     */
    SUCCEED("00", "消费成功"),
    CONSUMING("01", "相同主键的消息正在消费中"),
    FINISHED("02", "相同主键的消息已经消费完成"),
    SYSTEM_ERROR("-99", "执行幂等异常");

    private final String status;
    private final String desc;

    IdempotentResultStatus(String status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public String getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }

    public static IdempotentResultStatus getIdempotentResultStatus(String status) {
        if (StrUtil.isBlank(status)) {
            return null;
        }
        for (IdempotentResultStatus resultStatus : values()) {
            if(StrUtil.equals(resultStatus.getStatus(), status)) {
                return resultStatus;
            }
        }
        return null;
    }

    public static boolean isThrowException(String status) {
        IdempotentResultStatus resultStatus = getIdempotentResultStatus(status);
        if (resultStatus == null) {
            return false;
        }
        switch (resultStatus) {
            case SYSTEM_ERROR:
                return true;
            default:
                return false;
        }
    }
}
