package com.mudxx.mall.tiny.mq.idempotent.component.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentElement;
import com.mudxx.mall.tiny.mq.idempotent.common.IdempotentStatusEnum;
import com.mudxx.mall.tiny.mq.idempotent.component.IdempotentComponent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

/**
 * 消息幂等策略组件-实现层-JDBC
 * @author laiw
 * @date 2023/2/17 10:37
 */
@Slf4j
public class JDBCIdempotentComponent implements IdempotentComponent {

    private final JdbcTemplate jdbcTemplate;

    public JDBCIdempotentComponent(JdbcTemplate jdbcTemplate) {
        if (jdbcTemplate == null) {
            throw new NullPointerException("jdbc template is null");
        }
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean setConsuming(IdempotentElement element, long expireMilliSeconds) {
        try {
            DateTime expireTime = DateUtil.date(System.currentTimeMillis() + expireMilliSeconds);
            jdbcTemplate.update("INSERT INTO biz_message_idempotent(application_name, topic, tags, msg_uniq_key, status, expire_time) values (?, ?, ?, ?, ?, ?)",
                    element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), IdempotentStatusEnum.Consuming.getStatus(), expireTime);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            log.warn("found consuming/consumed record, set setConsuming fail {}", element);
            // 可能存在因不可控因素导致数据库记录未能正常更新或者删除的记录,动态的删除这些记录后重试
            int  i = delete(element, true);
            if (i > 0) {
                log.info("for delete {} expire records, now retry again {}", i, element);
                return setConsuming(element, expireMilliSeconds);
            }
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
    public void markConsumed(IdempotentElement element, long retainExpireMilliSeconds) {
        update(element, IdempotentStatusEnum.Consumed.getStatus(), retainExpireMilliSeconds);
    }


    private void update(IdempotentElement element, Integer status, long retainExpireMilliSeconds) {
        DateTime expireTime = DateUtil.date(System.currentTimeMillis() + retainExpireMilliSeconds);
        jdbcTemplate.update("UPDATE biz_message_idempotent SET status = ? , expire_time = ? " +
                        "WHERE application_name = ? AND topic = ? AND tags = ? AND msg_uniq_key = ? ",
                status, expireTime, element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey());
    }

    @Override
    public void delete(IdempotentElement element) {
        delete(element, false);
    }

    private int delete(IdempotentElement element, boolean onlyExpire) {
        if (onlyExpire) {
            return jdbcTemplate.update("DELETE FROM biz_message_idempotent WHERE application_name = ? AND topic = ? AND tags = ? AND msg_uniq_key = ? and expire_time < ? ",
                    element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), DateUtil.date());
        }
        return jdbcTemplate.update("DELETE FROM biz_message_idempotent WHERE application_name = ? AND topic =? AND tags = ? AND msg_uniq_key = ? ",
                element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey());
    }

    @Override
    public int getStatus(IdempotentElement element) {
        Map<String, Object> res = jdbcTemplate.queryForMap("SELECT status FROM biz_message_idempotent " +
                        "WHERE application_name = ? AND topic = ? AND tags = ? AND msg_uniq_key = ? AND expire_time > ? ",
                element.getApplicationName(), element.getTopic(), element.getTags(), element.getMsgUniqKey(), DateUtil.date());
        return MapUtils.getInteger(res, "status", 0);
    }

}
