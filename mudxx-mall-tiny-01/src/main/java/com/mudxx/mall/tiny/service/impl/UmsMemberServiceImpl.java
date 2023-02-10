package com.mudxx.mall.tiny.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.api.CommonResult;
import com.mudxx.mall.tiny.service.RedisService;
import com.mudxx.mall.tiny.service.UmsMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * @author laiw
 * @date 2023/2/9 14:35
 */
@Service
public class UmsMemberServiceImpl implements UmsMemberService {

    @Value("${redis.key.prefix.authCode}")
    private String redisKeyPrefixAuthCode;
    @Value("${redis.key.expire.authCode}")
    private Long redisKeyExpireSeconds;

    @Autowired
    private RedisService redisService;

    @Override
    public CommonResult generateAuthCode(String telephone) {
        if(StrUtil.isBlank(telephone)) {
            return CommonResult.validateFailed("请输入手机号码");
        }
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        redisService.set(redisKeyPrefixAuthCode + telephone, sb.toString());
        redisService.expire(redisKeyPrefixAuthCode + telephone, redisKeyExpireSeconds);
        return CommonResult.success(sb.toString(), "验证码获取成功");
    }

    @Override
    public CommonResult verifyAuthCode(String telephone, String authCode) {
        if(StrUtil.isBlank(telephone)) {
            return CommonResult.validateFailed("请输入手机号码");
        }
        if(StrUtil.isBlank(authCode)) {
            return CommonResult.validateFailed("请输入验证码");
        }
        String authCodeValue = redisService.get(redisKeyPrefixAuthCode + telephone);
        if(StrUtil.equals(authCode, authCodeValue)) {
            return CommonResult.success(null, "验证码校验成功");
        }
        return CommonResult.failed("验证码不正确");
    }
}
