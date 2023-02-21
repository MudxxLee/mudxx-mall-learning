package com.mudxx.mall.tiny.mq.component.rocketmq.config;

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
@ConfigurationProperties(prefix = "rocketmq")
public class RocketMqPropertiesConfig {

    private String nameServer;
    private BizCommon bizCommon = new BizCommon();
    private BizOrderly bizOrderly = new BizOrderly();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BizCommon {
        private ProducerProperties producer;
        private BizCommonConsumer consumer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BizCommonConsumer {
        private ConsumerProperties bizSample;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BizOrderly {
        private ProducerProperties producer;
        private BizCommonConsumer consumer;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BizOrderlyConsumer {
        private ConsumerProperties bizSample;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProducerProperties {
        private String logHeader;
        private String groupName;
        private String instanceName;
    }


    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerProperties {
        private ConsumerBasicProperties basic;
        private ConsumerExtraProperties extra;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerBasicProperties {
        private String groupName;
        private String instanceName;
        private String topic;
        private String tags;
        private Integer consumeThreadMin;
        private Integer consumeThreadMax;
        private Integer pullInterval;
        private Integer pullBatchSize;
        private Integer consumeMessageBatchMaxSize;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsumerExtraProperties {
        private String logHeader;
        private Long expireMilliSeconds;
        private Long retainExpireMilliSeconds;
    }

}
