package com.mudxx.mall.tiny.mq.idempotent.strategy.impl;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.idempotent.common.*;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.strategy.IdempotentStrategy;
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
public class PerfectIdempotentStrategy implements IdempotentStrategy {

    private IdempotentComponent component;

    private IdempotentConfig config;

    public PerfectIdempotentStrategy(IdempotentComponent component, IdempotentConfig config) {
        this.component = component;
        this.config = config;
    }

    @Override
    public IdempotentResult invoke(IdempotentElement element, Function<Object, IdempotentBizResult> callbackMethod, Object callbackMethodParam) {
        IdempotentComponent component = this.getComponent();
        if(StrUtil.isBlank(element.getMsgUniqKey())) {
            // 消息主键为空默认不设置幂等
            return IdempotentResult.createSuccess(this.defaultBizApply(callbackMethod, callbackMethodParam));
        }
        boolean setting = false;
        try {
            // 设置消息正在消费
            setting = component.setConsuming(element, this.getConfig().getExpireMilliSeconds());
            if(setting) {
                // 设置成功
                return doBizApplyAndUpdateStatus(element, callbackMethod, callbackMethodParam);
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
                    return doBizApplyAndUpdateStatus(element, callbackMethod, callbackMethodParam);
                }
            }
        } catch (Exception e) {
            log.error("msgUniqKey={} 幂等处理异常 (component={}) : {} ", element.getMsgUniqKey(), component.getClass().getSimpleName(), e.getMessage(), e);
            if (setting) {
                // 系统异常-删除幂等主键
                component.delete(element);
            }
            return IdempotentResult.createSystemError("幂等处理异常: " + e.getMessage());
        }
    }

    /**
     * 1.执行真正的消费
     * 2.消费失败会删除消费记录，消费成功则更新消费状态
     */
    private IdempotentResult doBizApplyAndUpdateStatus(IdempotentElement element, final Function<Object, IdempotentBizResult> callbackMethod, final Object callbackMethodParam) {
        // 执行真正的消费
        IdempotentBizResult bizResult = this.defaultBizApply(callbackMethod, callbackMethodParam);
        // 消费失败会删除消费记录，消费成功则更新消费状态
        this.doUpdateOrDelete(element, bizResult);
        return IdempotentResult.createSuccess(bizResult);
    }

    /**
     * 消费失败会删除消费记录，消费成功则更新消费状态
     */
    private void doUpdateOrDelete(IdempotentElement element, IdempotentBizResult bizResult) {
        IdempotentComponent component = this.getComponent();
        try {
            if(bizResult.getResult()) {
                // 标记消费完成
                log.info("msgUniqKey={} 业务消费完成,执行标记 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                component.markConsumed(element, this.getConfig().getRetainExpireMilliSeconds());
            } else {
                // 消费返回失败
                if (bizResult.getDelete()) {
                    log.info("msgUniqKey={} 业务消费失败,执行删除 (component={}) ", element.getMsgUniqKey(), component.getClass().getSimpleName());
                    component.delete(element);
                }
            }
        } catch (Exception e) {
            log.error("msgUniqKey={} 消费去重收尾工作异常(忽略异常) (component={}) : {} ", element.getMsgUniqKey(), component.getClass().getSimpleName(), e.getMessage());
        }
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
