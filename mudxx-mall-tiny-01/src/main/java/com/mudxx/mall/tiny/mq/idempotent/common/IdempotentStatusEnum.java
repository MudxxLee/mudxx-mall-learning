package com.mudxx.mall.tiny.mq.idempotent.common;

/**
 * 消息消费状态枚举
 * @author laiwen
 */
public enum IdempotentStatusEnum {
    /**
     * 消息消费状态枚举
     */
    Consuming(1, "消费进行中"),
    Consumed(2, "消费完成");


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
        return this == Consuming;
    }

    public boolean isConsumed() {
        return this == Consumed;
    }

    public static IdempotentStatusEnum getStatusEnum(int status) {
        for (IdempotentStatusEnum statusEnum : values()) {
            if (statusEnum.getStatus() == status) {
                return statusEnum;
            }
        }
        return null;
    }

}
