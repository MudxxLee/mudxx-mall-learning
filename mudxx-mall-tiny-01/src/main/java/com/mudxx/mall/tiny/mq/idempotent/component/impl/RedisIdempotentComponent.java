package com.mudxx.mall.tiny.mq.idempotent.component.impl;

import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentStatusEnum;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 消息幂等策略组件-实现层-Redis
 * @author laiw
 * @date 2023/2/22 17:25
 */
public class RedisIdempotentComponent implements IdempotentComponent {

    private final StringRedisTemplate redisTemplate;

    public RedisIdempotentComponent(StringRedisTemplate redisTemplate) {
        if(redisTemplate == null) {
            throw new NullPointerException("redis template is null");
        }
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean setConsuming(IdempotentElement element, long expireMilliSeconds) {
        String redisKey = buildMessageRedisKey(element);
        Boolean execute = redisTemplate.execute((RedisCallback<Boolean>) redisConnection -> redisConnection.set(
                redisKey.getBytes(StandardCharsets.UTF_8),
                String.valueOf(IdempotentStatusEnum.Consuming.getStatus()).getBytes(StandardCharsets.UTF_8),
                Expiration.milliseconds(expireMilliSeconds),
                RedisStringCommands.SetOption.SET_IF_ABSENT)
        );
        return ObjectUtil.defaultIfNull(execute, false);
    }

    @Override
    public void markConsumed(IdempotentElement element, long retainExpireMilliSeconds) {
        String redisKey = buildMessageRedisKey(element);
        redisTemplate.opsForValue().set(
                redisKey,
                String.valueOf(IdempotentStatusEnum.Consumed.getStatus()),
                retainExpireMilliSeconds,
                TimeUnit.MILLISECONDS);
    }

    @Override
    public int getStatus(IdempotentElement element) {
        String redisKey = buildMessageRedisKey(element);
        String value = redisTemplate.opsForValue().get(redisKey);
        if(StrUtil.isBlank(value) || ! NumberUtil.isInteger(value)) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    @Override
    public void delete(IdempotentElement element) {
        String redisKey = buildMessageRedisKey(element);
        redisTemplate.delete(redisKey);
    }

    private String buildMessageRedisKey(IdempotentElement element) {
        String prefix = "MSG" + ":" +
                element.getApplicationName() + ":" +
                element.getTopic() + ":" +
                element.getTags() + ":";
        return prefix + element.getMsgUniqKey();
    }

}
