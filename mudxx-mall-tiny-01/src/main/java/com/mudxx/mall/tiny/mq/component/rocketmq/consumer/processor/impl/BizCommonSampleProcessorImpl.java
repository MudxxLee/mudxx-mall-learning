package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.idempotent.AbstractRocketMqIdempotent;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.IBizCommonSampleProcessor;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentConfig;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.component.JDBCIdempotentComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

/**
 * @author laiwen
 */
@Slf4j
@Service
public class BizCommonSampleProcessorImpl extends AbstractRocketMqIdempotent implements IBizCommonSampleProcessor {

	@Autowired
	private RocketMqPropertiesConfig properties;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public String presetApplicationName() {
		return properties.getBizCommon().getConsumer().getBizSample().getBasic().getGroupName();
	}

	@Override
	public IdempotentComponent presetIdempotentComponent() {
		return new JDBCIdempotentComponent(jdbcTemplate);
	}

	@Override
	public IdempotentConfig presetIdempotentConfig() {
		RocketMqPropertiesConfig.ConsumerExtraProperties extra = properties.getBizCommon().getConsumer().getBizSample().getExtra();
		return new IdempotentConfig(extra.getExpireMilliSeconds(), extra.getRetainExpireMilliSeconds());
	}

	@Override
	public void consumeMessage(RocketMqCommonMessageExt messageExt, boolean idempotent) {
		if (idempotent) {
			// 消息幂等主键
			String msgUniqKey = messageExt.getKeys();
			// 幂等消费
			IdempotentResult result = super.idempotentConsume(msgUniqKey, messageExt, messageExt);
			log.info("msgUniqKey={} 消息幂等处理结果 {}", msgUniqKey, result);
		} else {
			this.commonConsume(messageExt);
		}
	}

	@Override
	public boolean idempotentCallback(Object callbackMethodParam) {
		try {
			// 业务实现
			RocketMqCommonMessageExt messageExt = (RocketMqCommonMessageExt) callbackMethodParam;
			this.commonConsume(messageExt);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			// TODO 业务异常,决定是否删除幂等key记录允许重新消费
			return true;
		}
	}

	/**
	 * 具体业务实现
	 * @param messageExt
	 */
	private void commonConsume(RocketMqCommonMessageExt messageExt) {
		TimeInterval timer = DateUtil.timer().restart();
		String msgId = messageExt.getMsgId();
		String topic = messageExt.getTopic();
		String tags = messageExt.getTags();
		String keys = messageExt.getKeys();
		try {
			final String bizFormJson = new String(messageExt.getBody(), StandardCharsets.UTF_8);
			log.info("msgId={} 消息接收内容: topic={}, tags={}, keys={}, body={}", msgId, topic, tags, keys, bizFormJson);
			// TODO 业务处理


		} catch (Exception e) {
			log.error("msgId={} 消息处理异常: {}", msgId, e.getMessage(), e);
			// TODO 异常处理
		} finally {
			//log.info("msgId={} 消息处理耗时: {}ms", msgId, timer.intervalRestart());
		}
	}

}