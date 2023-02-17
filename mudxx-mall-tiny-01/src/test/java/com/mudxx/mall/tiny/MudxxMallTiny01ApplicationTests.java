package com.mudxx.mall.tiny;

import com.alibaba.fastjson.JSONObject;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MudxxMallTiny01ApplicationTests {

    @Autowired
    private RocketMqPropertiesConfig config;

    @Test
    public void test() {
        System.out.println(JSONObject.toJSONString(config));
        System.out.println(config.getBizCommon().getProducer().getGroupName());
        System.out.println(config.getBizOrderly().getConsumer().getBizSample().getConsumeThreadMin());
        System.out.println(config.getBizTransactional().getConsumer().getBizSample().getConsumeThreadMin());

    }

}
