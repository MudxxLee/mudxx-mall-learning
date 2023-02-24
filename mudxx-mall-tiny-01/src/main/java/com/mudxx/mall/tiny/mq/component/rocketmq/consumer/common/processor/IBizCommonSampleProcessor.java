package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor;

import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;

/**
 * @author laiwen
 */
public interface IBizCommonSampleProcessor {

	/**
	 * 消费消息
	 * @param messageExt 消息
	 * @return
	 */
	boolean idempotentConsume(RocketMqCommonMessageExt messageExt);

	/**
	 * 消费消息
	 * @param messageExt 消息
	 * @return
	 */
	boolean commonConsume(RocketMqCommonMessageExt messageExt);

}