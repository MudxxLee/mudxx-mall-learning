package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.idempotent;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentConfig;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.strategy.IdempotentStrategy;
import com.mudxx.mall.tiny.mq.idempotent.strategy.impl.NormalIdempotentStrategy;
import com.mudxx.mall.tiny.mq.idempotent.strategy.impl.PerfectIdempotentStrategy;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * @author laiwen
 */
@Slf4j
public abstract class AbstractRocketMqIdempotent {

	private String applicationName;

	private IdempotentStrategy strategy;

	public AbstractRocketMqIdempotent() {

	}

	@PostConstruct
	private void initialContext() {
		// 消息幂等策略组件
		IdempotentComponent component = this.presetIdempotentComponent();
		if(component == null) {
			// 默认
			this.strategy = new NormalIdempotentStrategy();
		} else {
			this.applicationName = this.presetApplicationName();
			if(StrUtil.isBlank(this.applicationName)) {
				throw new RuntimeException("消息幂等策略初始化异常: applicationName未设置!");
			}
			// 幂等策略配置
			IdempotentConfig config = this.presetIdempotentConfig();
			if (config == null) {
				config = new IdempotentConfig();
			}
			config.setExpireMilliSeconds(config.getExpireMilliSeconds() == null ?
					IdempotentConfig.DEFAULT_EXPIRE_MILLI_SECONDS : config.getExpireMilliSeconds());
			config.setRetainExpireMilliSeconds(config.getRetainExpireMilliSeconds() == null ?
					IdempotentConfig.DEFAULT_RETAIN_EXPIRE_MILLI_SECONDS : config.getRetainExpireMilliSeconds());
			// 幂等策略
			this.strategy = new PerfectIdempotentStrategy(component, config);
		}
	}

	/**
	 * 预设定-消费的应用名
	 * @return
	 */
	public abstract String presetApplicationName();

	/**
	 * 预设定-消息幂等策略组件
	 * @return
	 */
	public abstract IdempotentComponent presetIdempotentComponent();

	/**
	 * 预设定-消息幂等策略配置
	 * @return
	 */
	public abstract IdempotentConfig presetIdempotentConfig();

	/**
	 * 实现消息幂等策略
	 * @param msgUniqKey 消息幂等主键
	 * @param messageExt 消息
	 * @param callbackMethodParam 回调方法入参
	 * @return
	 */
	protected IdempotentResult idempotentConsume(String msgUniqKey, RocketMqCommonMessageExt messageExt, Object callbackMethodParam) {
		// 封装幂等消息
		IdempotentElement element = IdempotentElement.builder()
				// 设置消费者组名称
				.applicationName(this.getApplicationName())
				// 设置消息的topic
				.topic(messageExt.getTopic())
				// 设置消息的tag
				.tags(StrUtil.isBlank(messageExt.getTags()) ? "" : messageExt.getTags())
				// 设置消息的唯一键
				.msgUniqKey(msgUniqKey)
			.build();
		// 执行幂等策略
		return getStrategy().invoke(element, this::idempotentCallback, callbackMethodParam);
	}

	/**
	 * 业务回调方法
	 * @param callbackMethodParam 回调方法入参
	 * @return
	 */
	public abstract boolean idempotentCallback(Object callbackMethodParam);


	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public IdempotentStrategy getStrategy() {
		return strategy;
	}

	public void setStrategy(IdempotentStrategy strategy) {
		this.strategy = strategy;
	}

}