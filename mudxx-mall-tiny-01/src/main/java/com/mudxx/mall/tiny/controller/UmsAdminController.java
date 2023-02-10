package com.mudxx.mall.tiny.controller;

import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.common.api.CommonResult;
import com.mudxx.mall.tiny.dto.UmsAdminLoginParam;
import com.mudxx.mall.tiny.mbg.model.UmsAdmin;
import com.mudxx.mall.tiny.service.UmsAdminService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author laiw
 * @date 2023/2/10 13:36
 */
@Api(description = "后台用户管理")
@RestController
@RequestMapping("/admin")
public class UmsAdminController {

    @Autowired
    private UmsAdminService umsAdminService;
    @Value("${jwt.tokenHeader}")
    private String tokenHeader;
    @Value("${jwt.tokenHead}")
    private String tokenHead;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @ResponseBody
    public CommonResult<UmsAdmin> register(@RequestBody UmsAdmin umsAdminParam, BindingResult bindingResult) {
        UmsAdmin umsAdmin = umsAdminService.register(umsAdminParam);
        if (umsAdmin == null) {
            return CommonResult.failed("用户注册失败");
        }
        return CommonResult.success(umsAdmin);
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    @ResponseBody
    public CommonResult login(@RequestBody UmsAdminLoginParam umsAdminLoginParam, BindingResult bindingResult) {
        String token = umsAdminService.login(umsAdminLoginParam.getUsername(), umsAdminLoginParam.getPassword());
        if(StrUtil.isBlank(token)) {
            return CommonResult.validateFailed("用户名或密码错误");
        }
        Map<String, String> tokenMap = new HashMap<>();
        tokenMap.put("token", token);
        tokenMap.put("tokenHead", tokenHead);
        return CommonResult.success(tokenMap);
    }

    @ApiOperation("获取用户所有权限")
    @GetMapping("/permission/{adminId}")
    @ResponseBody
    public CommonResult getPermissionList(@PathVariable Long adminId) {
       return CommonResult.success(umsAdminService.getPermissionList(adminId));
    }

}
