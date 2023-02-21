package com.mudxx.mall.tiny.mq.idempotent.strategy.impl;

import com.mudxx.mall.tiny.mq.idempotent.common.*;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.strategy.AbstractIdempotentStrategy;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;


/**
 * 消息幂等策略-良好
 *  接近于EXACTLY-ONCE语义（消息只会且仅会被成功消费一次）
 *  极端场景下则为ATLEAST-ONCE语义（消息至少被成功消费一次，不会因为去重的增强而丢失消息）
 * @author laiw
 * @date 2023/2/17 16:09
 */
@Slf4j
public class PerfectIdempotentStrategy extends AbstractIdempotentStrategy {

    public PerfectIdempotentStrategy(IdempotentComponent component, IdempotentConfig config) {
        super(component, config);
    }

    @Override
    public IdempotentResult invoke(IdempotentElement element, Function<Object, Boolean> callbackMethod, Object callbackMethodParam) {
        IdempotentComponent component = this.getComponent();
        try {
            // 设置消息正在消费
            boolean setting = component.setConsuming(element, this.getConfig().getExpireMilliSeconds());
            if(setting) {
                // 设置成功
                return doBizApply(element, callbackMethod, callbackMethodParam);
            } else {
                // 设置失败
                int status = component.getStatus(element);
                if (IdempotentStatusEnum.Consuming.getStatus() == status) {
                    // 正在消费中，稍后重试
                    log.warn("msgUniqKey={} 相同主键的消息正在消费中，本消息默认消费成功 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                    return IdempotentResult.create(IdempotentResultStatus.CONSUMING, "相同主键的消息正在消费中，本消息默认消费成功");
                } else if(IdempotentStatusEnum.Consumed.getStatus() == status) {
                    // 已消费完成，默认消费成功
                    log.warn("msgUniqKey={} 相同主键的消息已经消费完成，本消息默认消费成功 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                    return IdempotentResult.create(IdempotentResultStatus.FINISHED, "相同主键的消息已经消费完成，本消息默认消费成功");
                } else {
                    // 非法结果，降级为直接消费
                    log.warn("msgUniqKey={} 消息状态非法，降级为直接消费 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                    return doBizApply(element, callbackMethod, callbackMethodParam);
                }
            }
        } catch (Exception e) {
            log.error("msgUniqKey={} 执行幂等异常 (component={}) : {} ", element.getMsgUniqKey(), component.getClass().getSimpleName(), e.getMessage(), e);
            return IdempotentResult.createSystemError("执行幂等异常: " + e.getMessage());
        }
    }

    /**
     * 消费消息，末尾消费失败会删除消费记录，消费成功则更新消费状态
     */
    private IdempotentResult doBizApply(IdempotentElement element, final Function<Object, Boolean> callbackMethod, final Object callbackMethodParam) {
        IdempotentComponent component = this.getComponent();
        boolean bizResult = false;
        try {
            // 执行真正的消费
            bizResult = callbackMethod.apply(callbackMethodParam);
        } catch (Throwable e) {
            log.error("msgUniqKey={} 业务消费异常(忽略异常) (component={}): {}", element.getMsgUniqKey(), component.getClass().getSimpleName(), e.getMessage());
        }
        try {
            if(bizResult) {
                // 标记消费完成
                log.info("msgUniqKey={} 业务消费完成,执行标记 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                component.markConsumed(element, this.getConfig().getRetainExpireMilliSeconds());
            } else {
                // 消费返回失败
                log.info("msgUniqKey={} 业务消费失败,执行删除 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                component.delete(element);
            }
        } catch (Exception e) {
            log.error("msgUniqKey={} 消费去重收尾工作异常(忽略异常) (component={}) : {} ", element.getMsgUniqKey(), component.getClass().getSimpleName(), e.getMessage());
        }
        return IdempotentResult.createSuccess(bizResult);
    }
}
