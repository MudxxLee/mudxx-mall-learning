package com.mudxx.mall.tiny.service;

import com.mudxx.mall.tiny.common.api.CommonResult;

/**
 * @author laiw
 * @date 2023/2/9 14:33
 */
public interface UmsMemberService {

    /**
     * 生成验证码
     */
    CommonResult generateAuthCode(String telephone);

    /**
     * 判断验证码和手机号码是否匹配
     */
    CommonResult verifyAuthCode(String telephone, String authCode);

}
