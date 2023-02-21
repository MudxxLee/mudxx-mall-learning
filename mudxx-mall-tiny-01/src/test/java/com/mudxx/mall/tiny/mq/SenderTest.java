package com.mudxx.mall.tiny.mq;

import cn.hutool.core.map.MapUtil;
import cn.hutool.http.HttpUtil;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author laiw
 * @date 2023/2/21 14:55
 */
public class SenderTest {

    public static final String url = "http://127.0.0.1:9090/rocketmq/message/send-common-delay";

    public static void main(String[] args) {
        ExecutorService executor = new ThreadPoolExecutor(
                5, 20, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>());
        for (int i = 0; i < 100; i++) {
            Random random = new Random();
            int nextInt = random.nextInt(50);
            executor.submit(() -> {
                try {
                    Map<String, Object> paramMap = MapUtil.newHashMap();
                    paramMap.put("keys", nextInt);
                    paramMap.put("msg", "我是: " + nextInt);
                    HttpUtil.post(url, paramMap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        executor.shutdown();
    }

}
