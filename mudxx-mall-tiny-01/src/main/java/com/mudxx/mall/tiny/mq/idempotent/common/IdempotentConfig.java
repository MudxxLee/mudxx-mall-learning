package com.mudxx.mall.tiny.mq.idempotent.common;


import java.io.Serializable;

/**
 * 幂等策略配置
 * @author laiwen
 */
public class IdempotentConfig implements Serializable {
    private static final long serialVersionUID = 7408839385099033958L;
    /**
     * 消费中的消息，在过期时间内不允许重复(默认一分钟)
     *  即一分钟内的重复消息都会串行处理（等待前一个消息消费成功/失败），
     *  超过这个时间如果消息还在消费就不认为重复了（为了防止消息丢失）
     */
    public static final long DEFAULT_EXPIRE_MILLI_SECONDS = 60000;
    /**
     * 消息留存过期时间，记录在过期时间内不允许重复(默认一天，即一天内的消息不会重复)
     */
    public static final long DEFAULT_RETAIN_EXPIRE_MILLI_SECONDS = 86400000;

    /**
     * 消费中的消息，在过期时间内不允许重复
     */
    private Long expireMilliSeconds;
    /**
     * 消息留存过期时间，记录在过期时间内不允许重复
     */
    private Long retainExpireMilliSeconds;

    public IdempotentConfig() {

    }

    public IdempotentConfig(Long expireMilliSeconds, Long retainExpireMilliSeconds) {
        this.expireMilliSeconds = expireMilliSeconds;
        this.retainExpireMilliSeconds = retainExpireMilliSeconds;
    }


    public Long getExpireMilliSeconds() {
        return expireMilliSeconds;
    }

    public void setExpireMilliSeconds(Long expireMilliSeconds) {
        this.expireMilliSeconds = expireMilliSeconds;
    }

    public Long getRetainExpireMilliSeconds() {
        return retainExpireMilliSeconds;
    }

    public void setRetainExpireMilliSeconds(Long retainExpireMilliSeconds) {
        this.retainExpireMilliSeconds = retainExpireMilliSeconds;
    }
}