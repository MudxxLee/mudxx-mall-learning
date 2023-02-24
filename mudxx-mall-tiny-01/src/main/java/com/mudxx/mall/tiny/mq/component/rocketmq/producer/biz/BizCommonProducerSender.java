package com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz;

import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizCommonPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.DefaultCommonProducerSender;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 普通消息 生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Component
@ConditionalOnProperty(prefix="rocketmq.biz-common.producer", value="enabled", havingValue="true")
public class BizCommonProducerSender extends DefaultCommonProducerSender {

    public BizCommonProducerSender(BizCommonPropertiesConfig config){
        super(config.getNameServer(), config.getProducer());
    }

}
