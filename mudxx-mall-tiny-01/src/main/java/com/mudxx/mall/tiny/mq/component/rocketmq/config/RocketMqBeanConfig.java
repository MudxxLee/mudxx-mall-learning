package com.mudxx.mall.tiny.mq.component.rocketmq.config;

import com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz.BizCommonProducer;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz.BizOrderlyProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author laiw
 * @date 2023/2/14 11:03
 */
@Configuration
public class RocketMqBeanConfig {

    @Bean("bizCommonProducer")
    @ConditionalOnProperty(prefix="rocketmq.biz-common.producer", value="enabled", havingValue="true")
    public BizCommonProducer createRocketMqBizCommonProducer(RocketMqPropertiesConfig rocketMqConfig){
        return new BizCommonProducer(rocketMqConfig);
    }

    @Bean("bizOrderlyProducer")
    @ConditionalOnProperty(prefix="rocketmq.biz-orderly.producer", value="enabled", havingValue="true")
    public BizOrderlyProducer createRocketMqBizOrderlyProducer(RocketMqPropertiesConfig rocketMqConfig){
        return new BizOrderlyProducer(rocketMqConfig);
    }

}
