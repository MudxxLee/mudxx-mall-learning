package com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz;

import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.RocketMqProducer;
import lombok.extern.slf4j.Slf4j;

/**
 * 普通消息 生产者
 * @author laiw
 * @date 2023/2/13 17:54
 */
@Slf4j
public class BizCommonProducer extends RocketMqProducer {

    public BizCommonProducer(RocketMqPropertiesConfig config){
        super(config.getNameServer(), config.getBizCommon().getProducer());
    }

}
