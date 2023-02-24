package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.mudxx.mall.tiny.common.utils.SpringUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessageExt;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizCommonPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.RocketMqConsumerFactory;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.common.processor.IBizCommonSampleProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 并发消费者 无序消息（可靠同步，可靠异步，单向，延迟）
 * 1、并发消费的消费速度要比有序消费更快
 * 2、并发消费模式返回ConsumeConcurrentlyStatus.RECONSUME_LATER后，要过好几秒甚至十几秒才会再次消费,同一个消息到达最大消费次数之后就不会再出现
 *
 * @author laiw
 * @date 2023/2/14 10:22
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix="rocketmq.biz-common.consumer.biz-sample", value="enabled", havingValue="true")
public class BizCommonSampleConsumer {

    @Autowired
    private BizCommonPropertiesConfig propertiesConfig;

    @Bean(name = "bizCommonSamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        return RocketMqConsumerFactory.getMonitorRocketMqConsumer(
                propertiesConfig.getNameServer(),
                propertiesConfig.getConsumer().getBizSample(),
                new BizMessageListenerConcurrentlyImpl()
        );
    }

    private static class BizMessageListenerConcurrentlyImpl implements MessageListenerConcurrently {

        @Override
        public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeConcurrentlyContext context) {
            TimeInterval timer = DateUtil.timer().restart();
            IBizCommonSampleProcessor processor = SpringUtil.getBean(IBizCommonSampleProcessor.class);
            for (MessageExt messageExt : messageExtList) {
                boolean result = false;
                try {
                    // for循环单线程消费
                    RocketMqCommonMessageExt commonMessageExt = RocketMqCommonMessageExt.builder()
                            .msgId(messageExt.getMsgId())
                            .topic(messageExt.getTopic())
                            .tags(messageExt.getTags())
                            .keys(messageExt.getKeys())
                            .body(messageExt.getBody())
                            .reconsumeTimes(messageExt.getReconsumeTimes())
                            .build();
                    result = processor.idempotentConsume(commonMessageExt);
                } catch (Throwable e) {
                    log.error("msgId={} 消费异常(reconsumeTimes={}) {}", messageExt.getMsgId(), messageExt.getReconsumeTimes(), e.getMessage(), e);
                }
                if ( ! result) {
                    if(messageExt.getReconsumeTimes() < 2) {
                        log.error("msgId={} 消费失败,稍后重试(reconsumeTimes={})", messageExt.getMsgId(), messageExt.getReconsumeTimes());
                        return ConsumeConcurrentlyStatus.RECONSUME_LATER;
                    } else {
                        // TODO 记录异常
                        log.error("msgId={} 消费失败,超过重试次数,记录异常不再重试(reconsumeTimes={})", messageExt.getMsgId(), messageExt.getReconsumeTimes());
                    }
                }
            }
            log.info("[topic:{} queueId:{}] rocketmq common-message listener 本次拉取数量: {} 耗时{}ms", context.getMessageQueue().getTopic(),
                    context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        }
    }

}
