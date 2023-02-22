package com.mudxx.mall.tiny.mq.component.rocketmq.config;

import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ConsumerProperties;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.properties.ProducerProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author laiw
 * @date 2023/2/14 09:13
 */
@Data
@Component
@ConfigurationProperties(prefix = "rocketmq.biz-common")
public class BizCommonPropertiesConfig {

    private String nameServer;
    private ProducerProperties producer;
    private MultiConsumerProperties consumer;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MultiConsumerProperties {
        private ConsumerProperties bizSample;
        private ConsumerProperties bizOrderly;
    }



}
