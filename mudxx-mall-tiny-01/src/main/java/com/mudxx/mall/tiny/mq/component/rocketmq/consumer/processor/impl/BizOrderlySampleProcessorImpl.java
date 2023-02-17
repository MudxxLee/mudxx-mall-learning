package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.processor.IBizOrderlySampleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Slf4j
@Service
public class BizOrderlySampleProcessorImpl implements IBizOrderlySampleProcessor {


	@Override
	public boolean consumeMessage(String msgId, String topic, String tags, String keys, byte[] body) {
		TimeInterval timer = DateUtil.timer().restart();
		try {
			final String bizFormJson = new String(body, StandardCharsets.UTF_8);
			log.info("msgId={} 消息接收内容: topic={}, tags={}, keys={}", msgId, topic, tags, keys);
			// TODO 业务处理


		} catch (Exception e) {
			log.error("msgId={} 消息处理异常: {}", msgId, e.getMessage(), e);
			// TODO 异常处理
			return Boolean.FALSE;
		} finally {
			log.info("msgId={} 消息处理耗时: {}ms", msgId, timer.intervalRestart());
		}
		return Boolean.TRUE;
	}

}