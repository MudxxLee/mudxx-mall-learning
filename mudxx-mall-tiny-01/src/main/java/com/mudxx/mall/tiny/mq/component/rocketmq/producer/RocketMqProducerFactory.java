package com.mudxx.mall.tiny.mq.component.rocketmq.producer;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ProducerProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * 消息生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
public class RocketMqProducerFactory {

    public static DefaultMQProducer getDefaultMQProducer(String nameServer, ProducerProperties properties) {
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq producer nameServer is null !!!");
        }
        if (StrUtil.isBlank(properties.getGroupName())) {
            throw new RuntimeException("rocketmq producer groupName is null !!!");
        }
        try {
            DefaultMQProducer producer = new DefaultMQProducer(properties.getGroupName());
            if(StrUtil.isNotBlank(properties.getInstanceName())) {
                producer.setInstanceName(properties.getInstanceName());
            }
            producer.setNamesrvAddr(nameServer);
            // 不开启vip通道 (当消费者监听顺序消息时，消费者也需设置为VipChannelEnabled(false)，否则消费不到)
            producer.setVipChannelEnabled(false);
            // 允许发送的最大消息体大小4M
            producer.setMaxMessageSize(4194304);
            // 默认的发送超时时间
            producer.setSendMsgTimeout(3000);
            // 启动
            producer.start();
            log.info("rocketmq producer is start !!! nameServer={}, groupName={}", nameServer, properties.getGroupName());
            return producer;
        } catch (Exception e) {
            log.error("rocketmq producer is error !!! nameServer={}, groupName={}", nameServer, properties.getGroupName(), e);
            throw new RuntimeException("rocketmq producer is error", e);
        }
    }

}
