package com.mudxx.mall.tiny.mq.idempotent.strategy;

import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentConfig;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import lombok.extern.slf4j.Slf4j;


/**
 * 消息幂等策略-抽象类
 * @author laiw
 * @date 2023/2/17 16:09
 */
@Slf4j
public abstract class AbstractIdempotentStrategy implements IdempotentStrategy {

    private IdempotentComponent component;

    private IdempotentConfig config;

    public AbstractIdempotentStrategy() {

    }

    public AbstractIdempotentStrategy(IdempotentComponent component, IdempotentConfig config) {
        this.component = component;
        this.config = config;
    }

    public IdempotentComponent getComponent() {
        return component;
    }

    public void setComponent(IdempotentComponent component) {
        this.component = component;
    }

    public IdempotentConfig getConfig() {
        return config;
    }

    public void setConfig(IdempotentConfig config) {
        this.config = config;
    }
}
