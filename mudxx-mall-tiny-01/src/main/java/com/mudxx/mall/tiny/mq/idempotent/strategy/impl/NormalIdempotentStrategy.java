package com.mudxx.mall.tiny.mq.idempotent.strategy.impl;

import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentBizResult;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;
import com.mudxx.mall.tiny.mq.idempotent.strategy.IdempotentStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * 消息幂等策略-无策略
 * @author laiw
 * @date 2023/2/17 16:09
 */
@Slf4j
public class NormalIdempotentStrategy implements IdempotentStrategy {

    @Override
    public IdempotentResult invoke(IdempotentElement element, Function<Object, IdempotentBizResult> callbackMethod, Object callbackMethodParam) {
        IdempotentBizResult bizResult = null;
        try {
            bizResult = callbackMethod.apply(callbackMethodParam);
        } catch (Exception e) {
            log.error("msgUniqKey={} 业务消费异常(忽略异常): {}", element.getMsgUniqKey(), e.getMessage());
        }
        if(bizResult == null) {
            bizResult = IdempotentBizResult.createFail();
        }
        return IdempotentResult.createSuccess(bizResult);
    }
}
