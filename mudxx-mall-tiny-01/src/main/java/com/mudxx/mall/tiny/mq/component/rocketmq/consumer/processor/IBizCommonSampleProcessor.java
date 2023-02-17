package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor;

/**
 * @author laiwen
 */
public interface IBizCommonSampleProcessor {

	/**
	 * 消费消息
	 * @param msgId
	 * @param topic
	 * @param tags
	 * @param keys
	 * @param body
	 * @return
	 */
	boolean consumeMessage(String msgId, String topic, String tags, String keys, byte[] body);
	
}