package com.mudxx.mall.tiny.mq.idempotent.strategy;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentStatusEnum;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentVersionEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * @author laiw
 * @date 2023/2/17 10:37
 */
@Slf4j
public class JDBCIdempotentStrategy implements IdempotentStrategy {

    private final JdbcTemplate jdbcTemplate;

    public JDBCIdempotentStrategy(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean setConsuming(IdempotentElement element, long consumingExpireMilliSeconds) {
        try {
            DateTime expireTime = DateUtil.date(System.currentTimeMillis() + consumingExpireMilliSeconds);
            int  i = jdbcTemplate.update("INSERT INTO biz_message_idempotent(application_name, topic, tag, msg_uniq_key, msg_version, status, expire_time) values (?, ?, ?, ?, ?, ?, ?)",
                    element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(),
                    IdempotentVersionEnum.PRESENT.getStatus(), IdempotentStatusEnum.CONSUMING.getStatus(), expireTime);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("found consuming/consumed record, set setConsuming fail {}", element);
            // 存在已经在消费中的消息
            return false;
        } catch (Exception e) {
            log.error("unknown error when jdbc insert, will consider success", e);
            return true;
        }
        // 设置成功
        return true;
    }

    @Override
    public void markConsumed(IdempotentElement element, long consumedExpireMilliSeconds) {
        DateTime expireTime = DateUtil.date(System.currentTimeMillis() + consumedExpireMilliSeconds);
        jdbcTemplate.update("UPDATE biz_message_idempotent SET msg_version = ? , status = ? , expire_time = ? " +
                        "WHERE application_name = ? AND topic = ? AND tag = ? AND msg_uniq_key = ? AND msg_version = ? ",
                IdempotentVersionEnum.HISTORY.getStatus(), IdempotentStatusEnum.CONSUMED.getStatus(), expireTime,
                element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), IdempotentVersionEnum.PRESENT.getStatus());
    }

    @Override
    public Integer getStatus(IdempotentElement element) {
        Map<String, Object> res = jdbcTemplate.queryForMap("SELECT status FROM biz_message_idempotent " +
                        "WHERE application_name = ? AND topic = ? AND tag = ? AND msg_uniq_key  = ? and msg_version = ? ",
                element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), IdempotentVersionEnum.PRESENT.getStatus());
        return MapUtils.getInteger(res, "status");
    }

    @Override
    public void delete(IdempotentElement element) {
        jdbcTemplate.update("DELETE FROM biz_message_idempotent WHERE application_name = ? AND topic =? AND tag = ? AND msg_uniq_key = ? and msg_version = ? ",
            element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), IdempotentVersionEnum.PRESENT.getStatus());
    }

}
