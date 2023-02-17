package com.mudxx.mall.tiny.mq.idempotent.common;

/**
 * 消息消费状态枚举
 * @author laiwen
 */
public enum IdempotentStatusEnum {
    /**
     *
     */
    CONSUMING(0, "消息消费进行中"),
    CONSUMED(1, "消息消费完成");

    private final int status;
    private final String desc;

    IdempotentStatusEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }

    public boolean isConsuming() {
        return status == 0;
    }

    public boolean isConsumed() {
        return status == 1;
    }

}
