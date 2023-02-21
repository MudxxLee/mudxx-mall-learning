package com.mudxx.mall.tiny.mq.idempotent.strategy.impl;

import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;
import com.mudxx.mall.tiny.mq.idempotent.strategy.AbstractIdempotentStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

/**
 * 消息幂等策略-无策略
 * @author laiw
 * @date 2023/2/17 16:09
 */
@Slf4j
public class NormalIdempotentStrategy extends AbstractIdempotentStrategy {

    public NormalIdempotentStrategy() {

    }

    @Override
    public IdempotentResult invoke(IdempotentElement element, Function<Object, Boolean> callbackMethod, Object callbackMethodParam) {
        boolean bizResult = false;
        try {
            bizResult = callbackMethod.apply(callbackMethodParam);
        } catch (Exception e) {
            log.error("msgUniqKey={} 业务消费异常(忽略异常): {}", element.getMsgUniqKey(), e.getMessage());
        }
        return IdempotentResult.createSuccess(bizResult);
    }
}
