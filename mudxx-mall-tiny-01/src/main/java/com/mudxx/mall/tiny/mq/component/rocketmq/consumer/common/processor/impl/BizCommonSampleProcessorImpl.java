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
import com.mudxx.mall.tiny.mq.idempotent.component.impl.RedisIdempotentComponent;
import com.mudxx.mall.tiny.mq.idempotent.service.AbstractIdempotentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
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
	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	@Override
	public String presetApplicationName() {
		return properties.getConsumer().getBizSample().getGroupName();
	}

	@Override
	public IdempotentComponent presetIdempotentComponent() {
		return new RedisIdempotentComponent(stringRedisTemplate);
	}

	@Override
	public IdempotentConfig presetIdempotentConfig() {
		ConsumerExtraProperties extra = properties.getConsumer().getBizSample().getExtra();
		return new IdempotentConfig(extra.getExpireMilliSeconds(), extra.getRetainExpireMilliSeconds());
	}

	@Override
	public boolean idempotentConsume(RocketMqCommonMessageExt messageExt) {
		// 消息topic
		String topic = messageExt.getTopic();
		// 消息标签
		String tags = messageExt.getTags();
		// 消息幂等主键
		String msgUniqKey = messageExt.getKeys();
		// 幂等消费
		IdempotentResult result = super.idempotentConsume(topic, tags, msgUniqKey, messageExt);
		log.info("msgUniqKey={} 消息幂等处理结果 {}", msgUniqKey, result);
		// 稍后重试-执行幂等异常
		if(IdempotentResultStatus.isErrorStatus(result.getResult())) {
			return Boolean.FALSE;
		}
		IdempotentBizResult bizResult = result.getBizResult();
		// 稍后重试-业务返回已删除幂等主键记录且需要重试
		if(bizResult != null && bizResult.getDelete() && bizResult.getRetry()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public IdempotentBizResult idempotentCallback(Object callbackMethodParam) {
		try {
			// 业务实现
			RocketMqCommonMessageExt messageExt = (RocketMqCommonMessageExt) callbackMethodParam;
			boolean consume = this.commonConsume(messageExt);
			if (consume) {
				return IdempotentBizResult.createSuccess();
			} else {
				return IdempotentBizResult.createFail();
			}
		} catch (Exception e) {
			// TODO 业务异常 是否删除消息记录、是否延迟重试消息
			return IdempotentBizResult.createFail();
		}
	}

	/**
	 * 具体业务实现
	 */
	@Override
	public boolean commonConsume(RocketMqCommonMessageExt messageExt) {
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

			//int i = 1/0;

		} catch (Exception e) {
			log.error("msgId={} 消息处理异常: {}", msgId, e.getMessage(), e);
			// 稍后重试
			return Boolean.FALSE;
		} finally {
			log.info("msgId={} 消息处理耗时: {}ms", msgId, timer.intervalRestart());
		}
		return Boolean.TRUE;
	}

}