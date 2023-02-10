package com.mudxx.mall.tiny.controller;

import com.mudxx.mall.tiny.common.api.CommonResult;
import com.mudxx.mall.tiny.service.UmsMemberService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author laiw
 * @date 2023/2/10 10:05
 */
@Api(description = "会员登录注册管理")
@RestController
@RequestMapping("/sso")
public class UmsMemberController {

    @Autowired
    private UmsMemberService umsMemberService;

    @ApiOperation("获取验证码")
    @GetMapping("/getAuthCode")
    @ResponseBody
    public CommonResult getAuthCode(String telephone) {
        return umsMemberService.generateAuthCode(telephone);
    }

    @ApiOperation("获取验证码")
    @GetMapping("/verifyAuthCode")
    @ResponseBody
    public CommonResult verifyAuthCode(String telephone, String authCode) {
        return umsMemberService.verifyAuthCode(telephone, authCode);
    }



}
