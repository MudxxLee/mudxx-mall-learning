package com.mudxx.mall.tiny.mq.idempotent.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentBizResult;
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
 * 消息幂等-业务抽象类
 * @author laiwen
 */
@Slf4j
public abstract class AbstractIdempotentService {

	private String applicationName;

	private IdempotentStrategy strategy;

	/**
	 * 类构造完毕时初始化属性内容
	 */
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
				throw new RuntimeException("消息幂等策略业务初始化异常: applicationName未设置!");
			}
			// 幂等策略配置
			IdempotentConfig config = this.presetIdempotentConfig();
			if (config == null) {
				config = new IdempotentConfig();
			}
			config.setExpireMilliSeconds(ObjectUtil.defaultIfNull(config.getExpireMilliSeconds(), IdempotentConfig.DEFAULT_EXPIRE_MILLI_SECONDS));
			config.setRetainExpireMilliSeconds(ObjectUtil.defaultIfNull(config.getRetainExpireMilliSeconds(), IdempotentConfig.DEFAULT_RETAIN_EXPIRE_MILLI_SECONDS));
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
	 * @param topic 消息topic
	 * @param tags 消息标签
	 * @param msgUniqKey 消息幂等主键
	 * @param callbackMethodParam 回调方法入参
	 * @return
	 */
	protected IdempotentResult idempotentConsume(String topic, String tags, String msgUniqKey, Object callbackMethodParam) {
		// 封装幂等消息
		IdempotentElement element = IdempotentElement.builder()
				// 设置消费者组名称
				.applicationName(this.getApplicationName())
				// 设置消息的topic
				.topic(topic)
				// 设置消息的tag
				.tags(StrUtil.blankToDefault(tags, ""))
				// 设置消息的唯一键
				.msgUniqKey(msgUniqKey)
			.build();
		// 执行幂等策略
		return getStrategy().invoke(element, this::idempotentCallback, callbackMethodParam);
	}

	/**
	 * 业务真正消费方法(在幂等设置成功后将会调用)
	 * @param callbackMethodParam 方法入参
	 * @return IdempotentBizResult
	 */
	public abstract IdempotentBizResult idempotentCallback(Object callbackMethodParam);


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