package com.mudxx.mall.tiny.pwd;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * @author laiw
 * @date 2023/2/10 14:14
 */
public class Test {

    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("mudxx-mall"));
    }

}
