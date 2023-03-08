package com.mudxx.mall.tiny.pwd;

import cn.hutool.core.text.PasswdStrength;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author laiw
 * @date 2023/2/10 14:14
 */
public class Test {

    public static void main(String[] args) {
        //BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        //System.out.println(passwordEncoder.encode("mudxx-mall"));
        String content = " CXCAS";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));

        content = "我@";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));

        content = "1_-";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));

        content = "1_";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));

        content = "a3132";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));

        content = "B312412vt它";
        System.out.println(ReUtil.isMatch("^[\\u4E00-\\u9FA5A-Za-z0-9_]+$", content));


    }

}
