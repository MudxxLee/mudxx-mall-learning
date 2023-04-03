package com.mudxx.mall.tiny.mq.component.rocketmq.consumer.orderly;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.TimeInterval;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizOrderlyPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.consumer.RocketMqConsumerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 顺序消费者（顺序消息只支持可靠同步发送）
 * MessageListenerOrderly采用的是分段锁，它不是锁整个Broker而是锁里面的单个Queue
 * 保证：
 * 1、生产端 同一orderID的订单放到同一个queue。
 * 2、消费端 同一个queue取出消息的时候锁住整个queue，直到消费后再解锁。
 * 弊端：
 * 1、降低了吞吐量
 * 2、前一条消息消费出现问题，后续的处理流程会阻塞，业务需做相应处理
 * 3、有序消费模式返回ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT后，消费者会立刻消费这条消息
 * 4、同一个消息如果一直无法消费成功则会无限消费
 *
 * @author laiw
 * @date 2023/2/14 10:22
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix="rocketmq.biz-orderly.consumer.biz-sample", value="enabled", havingValue="true")
public class BizOrderlySampleConsumer {

    @Autowired
    private BizOrderlyPropertiesConfig propertiesConfig;

    @Bean(name = "bizOrderlySamplePushConsumer")
    public DefaultMQPushConsumer getMonitorRocketMqConsumer() {
        return RocketMqConsumerFactory.getMonitorRocketMqConsumer(
                propertiesConfig.getNameServer(),
                propertiesConfig.getConsumer().getBizSample(),
                new BizMessageListenerOrderlyImpl()
        );
    }

    private static class BizMessageListenerOrderlyImpl implements MessageListenerOrderly {

        @Override
        public ConsumeOrderlyStatus consumeMessage(List<MessageExt> messageExtList, ConsumeOrderlyContext context) {
            TimeInterval timer = DateUtil.timer().restart();
            // 顺序消息默认控制消费一个
            MessageExt messageExt = messageExtList.get(0);
            try {

            } catch (Exception e) {
                int reconsumeTimes = messageExt.getReconsumeTimes();
                log.error("msgId={} reconsumeTimes={}  消息处理异常: {}", messageExt.getMsgId(), reconsumeTimes, e.getMessage(), e);
                if (reconsumeTimes < 2) {
                    // 消费者会立刻消费这条消息
                    return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
                } else {
                    // TODO 记录异常
                    log.error("msgId={} reconsumeTimes={}  超过重试次数,记录异常", messageExt.getMsgId(), reconsumeTimes);
                }
            }
            log.info("[topic:{} queueId:{}] rocketmq orderly-message listener 本次拉取数量: {} 耗时{}ms", context.getMessageQueue().getTopic(),
                    context.getMessageQueue().getQueueId(), messageExtList.size(), timer.intervalRestart());
            return ConsumeOrderlyStatus.SUCCESS;
        }
    }

}
