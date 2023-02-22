package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor;

import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;

/**
 * @author laiwen
 */
public interface IBizCommonSampleProcessor {

	/**
	 * 消费消息
	 * @param messageExt 消息
	 * @param idempotent 是否幂等消费
	 * @return
	 */
	void consumeMessage(RocketMqCommonMessageExt messageExt, boolean idempotent);

}