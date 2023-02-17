package com.mudxx.mall.tiny.mq.component.rocketmq.common;

/**
 * 延时消息 [1s 5s 10s 30s 1m 2m 3m 4m 5m 6m 7m 8m 9m 10m 20m 30m 1h 2h]
 *  level是级别，1表示配置里面的第一个级别，2表示第二个级别，延迟消费级别
 * 
 * @author laiw
 * @date 2023/2/15 14:45
 */
public enum RocketMqCommonDelay {
    /**
     *
     */
    S1(1, "1s"),
    S2(2, "5s"),
    S10(3, "10s"),
    S30(4, "30s"),
    M1(5, "1m"),
    M2(6, "2m"),
    M3(7, "3m"),
    M4(8, "4m"),
    M5(9, "5m"),
    M6(10, "6m"),
    M7(11, "7m"),
    M8(12, "8m"),
    M9(13, "9m"),
    M10(14, "10m"),
    M20(15, "20m"),
    M30(16, "30m"),
    H1(17, "1h"),
    H2(18, "2h");

    private final int level;
    private final String desc;

    RocketMqCommonDelay(int level, String desc) {
        this.level = level;
        this.desc = desc;
    }

    public int getLevel() {
        return level;
    }

    public String getDesc() {
        return desc;
    }
}
