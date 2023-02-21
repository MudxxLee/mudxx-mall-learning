package com.mudxx.mall.tiny.mq.idempotent.component;


import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;

/**
 * 消息幂等策略组件-接口层
 * @author laiwen
 */
public interface IdempotentComponent {

    /**
     * 设置消息正在消费
     * @param element 消息基础信息
     * @param expireMilliSeconds 过期时长
     * @return true: 允许消费 false: 不允许消费
     */
    boolean setConsuming(IdempotentElement element, long expireMilliSeconds);

    /**
     * 标记消息消费完成
     * @param element 消息基础信息
     * @param retainExpireMilliSeconds 留存过期时长
     */
    void markConsumed(IdempotentElement element, long retainExpireMilliSeconds);

    /**
     * 获取消息状态
     * @param element 消息基础信息
     * @return int
     */
    int getStatus(IdempotentElement element);

    /**
     * 删除
     * @param element 消息基础信息
     */
    void delete(IdempotentElement element);

}
