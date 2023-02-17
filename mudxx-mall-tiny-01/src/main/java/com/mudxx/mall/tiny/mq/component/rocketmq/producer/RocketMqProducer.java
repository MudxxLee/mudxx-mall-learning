package com.mudxx.mall.tiny.mq.component.rocketmq.producer;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;

/**
 * 消息 生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
public class RocketMqProducer {

    private final DefaultMQProducer producer;

    public RocketMqProducer(String nameServer, RocketMqPropertiesConfig.ProducerProperties properties){
        this.producer = getDefaultMQProducer(nameServer, properties);
    }

    public DefaultMQProducer getProducer() {
        return producer;
    }

    private DefaultMQProducer getDefaultMQProducer(String nameServer, RocketMqPropertiesConfig.ProducerProperties properties) {
        if (StrUtil.isBlank(nameServer)) {
            throw new RuntimeException("rocketmq nameServer is null !!!");
        }
        try {
            DefaultMQProducer producer = new DefaultMQProducer(properties.getGroupName());
            producer.setInstanceName(properties.getInstanceName());
            producer.setNamesrvAddr(nameServer);
            // 允许发送的最大消息体大小4M
            producer.setMaxMessageSize(4194304);
            // 默认的发送超时时间
            producer.setSendMsgTimeout(3000);
            // 启动
            producer.start();
            log.info("[{}] rocketmq producer is start ! nameServer={}, groupName={}",
                    properties.getLogHeader(), nameServer, properties.getGroupName());
            return producer;

        } catch (Exception e) {
            log.error("[{}] rocketmq producer is error {}, {}", properties.getLogHeader(), e.getMessage(), e);
        }
        return null;
    }

}
