package com.mudxx.mall.tiny.mq.idempotent.strategy;

import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentBizResult;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;

import java.util.function.Function;

/**
 * 消息幂等策略
 * @author laiw
 * @date 2023/2/17 16:09
 */
public interface IdempotentStrategy {

    /**
     * 具体实现
     * @param element 消息幂等基础信息
     * @param callbackMethod 回调方法
     * @param callbackMethodParam 回调方法参数
     * @return
     */
    IdempotentResult invoke(IdempotentElement element, Function<Object, IdempotentBizResult> callbackMethod, Object callbackMethodParam);

    /**
     * 执行真正的消费(默认实现)
     * @param callbackMethod 回调方法
     * @param callbackMethodParam 回调方法参数
     * @return
     */
    default IdempotentBizResult defaultBizApply(final Function<Object, IdempotentBizResult> callbackMethod, final Object callbackMethodParam) {
        IdempotentBizResult bizResult = null;
        try {
            // 执行真正的消费
            bizResult = callbackMethod.apply(callbackMethodParam);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if(bizResult == null) {
            // 默认删除且重试
            bizResult = IdempotentBizResult.createFail();
        }
        return bizResult;
    }

}
