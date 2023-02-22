package com.mudxx.mall.tiny.controller;

import com.mudxx.mall.tiny.common.api.CommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonDelay;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.BizCommonPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.biz.BizCommonProducerSender;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

/**
 * @author laiw
 * @date 2023/2/13 17:18
 */
@Api(description = "RocketMQ消息管理")
@RestController
@RequestMapping("/rocketmq/message")
public class RocketMqMessageController {

    @Autowired
    private BizCommonPropertiesConfig bizCommonPropertiesConfig;
    @Autowired(required = false)
    private BizCommonProducerSender bizCommonProducerSender;

    @ApiOperation("发送RocketMQ消息")
    @PostMapping("/send-common")
    @ResponseBody
    public CommonResult sendCommon(@RequestParam("msg") String msg, @RequestParam("keys") String keys) {
        RocketMqCommonMessage message = new RocketMqCommonMessage(bizCommonPropertiesConfig.getConsumer().getBizSample());
        message.setBody(msg.getBytes(StandardCharsets.UTF_8));
        message.setKeys(keys);
        return CommonResult.success(bizCommonProducerSender.sendCommonMessage(message));
    }

    @ApiOperation("发送RocketMQ延迟消息")
    @PostMapping("/send-common-delay")
    @ResponseBody
    public CommonResult sendCommonDelay(@RequestParam("msg") String msg, @RequestParam("keys") String keys) {
        RocketMqCommonMessage message = new RocketMqCommonMessage(bizCommonPropertiesConfig.getConsumer().getBizSample());
        message.setBody(msg.getBytes(StandardCharsets.UTF_8));
        message.setKeys(keys);
        return CommonResult.success(bizCommonProducerSender.sendDelayMessage(message, RocketMqCommonDelay.S10));
    }

}
