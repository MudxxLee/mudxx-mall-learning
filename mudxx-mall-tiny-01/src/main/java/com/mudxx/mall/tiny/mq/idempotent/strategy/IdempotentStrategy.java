package com.mudxx.mall.tiny.mq.idempotent.strategy;


import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentStatusEnum;

/**
 * 消息幂等策略
 * @author laiwen
 */
public interface IdempotentStrategy {

    /**
     * 设置消息正在消费
     * @param element 消息基础信息
     * @param consumingExpireMilliSeconds 过期时长
     * @return true: 允许消费 false: 不允许消费
     */
    boolean setConsuming(IdempotentElement element, long consumingExpireMilliSeconds);

    /**
     * 标记消息消费完成
     * @param element 消息基础信息
     * @param consumedExpireMilliSeconds 过期时长
     */
    void markConsumed(IdempotentElement element, long consumedExpireMilliSeconds);

    /**
     * 获取消息状态
     * @param element 消息基础信息
     * @return IdempotentStatusEnum
     */
    Integer getStatus(IdempotentElement element);

    /**
     * 删除
     * @param element 消息基础信息
     */
    void delete(IdempotentElement element);

}
