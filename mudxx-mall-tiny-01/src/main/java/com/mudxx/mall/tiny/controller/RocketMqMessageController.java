package com.mudxx.mall.tiny.controller;

import com.mudxx.mall.tiny.common.api.CommonResult;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonDelay;
import com.mudxx.mall.tiny.mq.component.rocketmq.common.RocketMqCommonMessage;
import com.mudxx.mall.tiny.mq.component.rocketmq.config.RocketMqPropertiesConfig;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.sender.BizCommonMessageSender;
import com.mudxx.mall.tiny.mq.component.rocketmq.producer.sender.BizOrderlyMessageSender;
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
    private RocketMqPropertiesConfig rocketMqPropertiesConfig;
    @Autowired
    private BizCommonMessageSender bizCommonMessageSender;
    @Autowired
    private BizOrderlyMessageSender bizOrderlyMessageSender;

    @ApiOperation("发送RocketMQ消息")
    @PostMapping("/send-common")
    @ResponseBody
    public CommonResult sendCommon(@RequestParam("msg") String msg) {
        RocketMqCommonMessage message = new RocketMqCommonMessage(rocketMqPropertiesConfig.getBizCommon().getConsumer().getBizSample());
        message.setBody(msg.getBytes(StandardCharsets.UTF_8));
        return CommonResult.success(bizCommonMessageSender.sendCommonMessage(message));
    }

    @ApiOperation("发送RocketMQ延迟消息")
    @PostMapping("/send-common-delay")
    @ResponseBody
    public CommonResult sendCommonDelay(@RequestParam("msg") String msg) {
        RocketMqCommonMessage message = new RocketMqCommonMessage(rocketMqPropertiesConfig.getBizCommon().getConsumer().getBizSample());
        message.setBody(msg.getBytes(StandardCharsets.UTF_8));
        return CommonResult.success(bizCommonMessageSender.sendDelayMessage(message, RocketMqCommonDelay.S30));
    }

    @ApiOperation("发送RocketMQ顺序消息")
    @PostMapping("/send-orderly")
    @ResponseBody
    public CommonResult sendOrderly(@RequestParam("msg") String msg) {
        RocketMqCommonMessage message = new RocketMqCommonMessage(rocketMqPropertiesConfig.getBizOrderly().getConsumer().getBizSample());
        message.setBody(msg.getBytes(StandardCharsets.UTF_8));
        return CommonResult.success(bizOrderlyMessageSender.sendOrderlyMessage(message));
    }

}
