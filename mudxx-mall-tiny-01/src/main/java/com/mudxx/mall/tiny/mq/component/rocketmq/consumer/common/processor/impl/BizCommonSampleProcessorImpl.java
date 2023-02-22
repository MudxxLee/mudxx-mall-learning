package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizCommonPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerExtraProperties;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor.IBizCommonSampleProcessor;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentBizResult;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentConfig;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResult;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentResultStatus;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.component.JDBCIdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.service.AbstractIdempotentService;
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
public class BizCommonSampleProcessorImpl extends AbstractIdempotentService implements IBizCommonSampleProcessor {

	@Autowired
	private BizCommonPropertiesConfig properties;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Override
	public String presetApplicationName() {
		return properties.getConsumer().getBizSample().getGroupName();
	}

	@Override
	public IdempotentComponent presetIdempotentComponent() {
		return new JDBCIdempotentComponent(jdbcTemplate);
	}

	@Override
	public IdempotentConfig presetIdempotentConfig() {
		ConsumerExtraProperties extra = properties.getConsumer().getBizSample().getExtra();
		return new IdempotentConfig(extra.getExpireMilliSeconds(), extra.getRetainExpireMilliSeconds());
	}

	@Override
	public void consumeMessage(RocketMqCommonMessageExt messageExt, boolean idempotent) {
		if (idempotent) {
			// 消息topic
			String topic = messageExt.getTopic();
			// 消息标签
			String tags = messageExt.getTags();
			// 消息幂等主键
			String msgUniqKey = messageExt.getKeys();
			// 幂等消费
			IdempotentResult result = super.idempotentConsume(topic, tags, msgUniqKey, messageExt);
			log.info("msgUniqKey={} 消息幂等处理结果 {}", msgUniqKey, result);
			if(IdempotentResultStatus.throwException(result.getResult())) {
				throw new RuntimeException(result.getResultMsg());
			}
			if(result.getBizResult() != null && result.getBizResult().getRetry()) {
				throw new RuntimeException("消息需重试");
			}
		} else {
			this.commonConsume(messageExt);
		}
	}

	@Override
	public IdempotentBizResult idempotentCallback(Object callbackMethodParam) {
		try {
			// 业务实现
			RocketMqCommonMessageExt messageExt = (RocketMqCommonMessageExt) callbackMethodParam;
			this.commonConsume(messageExt);
		} catch (Exception e) {
			// TODO 业务异常,决定是否删除幂等key记录允许重新消费
			return IdempotentBizResult.createDefaultFail();
		}
		return IdempotentBizResult.createSuccess();
	}

	/**
	 * 具体业务实现
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

			Thread.sleep(3000);

		} catch (Exception e) {
			log.error("msgId={} 消息处理异常: {}", msgId, e.getMessage(), e);
			// TODO 异常处理
			throw new RuntimeException(e);
		} finally {
			//log.info("msgId={} 消息处理耗时: {}ms", msgId, timer.intervalRestart());
		}
	}

}