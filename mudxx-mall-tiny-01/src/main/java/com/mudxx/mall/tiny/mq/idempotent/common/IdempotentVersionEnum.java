package com.mudxx.mall.tiny.mq.idempotent.common;

/**
 * 消息消费版本号枚举
 * @author laiwen
 */
public enum IdempotentVersionEnum {
    /**
     *
     */
    HISTORY(0, "历史版本"),
    PRESENT(1, "当前版本");

    private final int status;
    private final String desc;

    IdempotentVersionEnum(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }
    public String getDesc() {
        return desc;
    }

    public boolean isHistory() {
        return status == 0;
    }

    public boolean isPresent() {
        return status == 1;
    }

}
