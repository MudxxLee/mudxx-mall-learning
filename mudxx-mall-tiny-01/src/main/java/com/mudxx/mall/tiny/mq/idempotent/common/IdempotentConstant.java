package com.mudxx.mall.tiny.mq.idempotent.common;


/**
 * @author laiwen
 */
public class IdempotentConstant {

    /**
     * 对于消费中的消息，多少毫秒内认为重复，默认一分钟
     *  即一分钟内的重复消息都会串行处理（等待前一个消息消费成功/失败），
     *  超过这个时间如果消息还在消费就不认为重复了（为了防止消息丢失）
     */
    public static final long CONSUMING_EXPIRE_MILLI_SECONDS = 60000;

    /**
     * 消息消费成功后，记录保留多少分钟，默认一天，即一天内的消息不会重复
     */
    public static final long CONSUMED_EXPIRE_MILLI_SECONDS = 86400000;

}