
CREATE TABLE `biz_message_idempotent` (
  `application_name` varchar(255) NOT NULL COMMENT '消费的应用名（可以用消费者组名称）',
  `topic` varchar(255) NOT NULL COMMENT '消息来源的topic（不同topic消息不会认为重复）',
  `tags` varchar(16) NOT NULL COMMENT '消息的tag（同一个topic不同的tags，就算去重键一样也不会认为重复），没有tags则存""字符串',
  `msg_uniq_key` varchar(255) NOT NULL COMMENT '消息的唯一键（建议使用业务主键）',
  `status` tinyint(2) NOT NULL COMMENT '消费状态(1：消费进行中、2：消费完成、-1：消费失败、-2：更新异常)',
  `expire_time` datetime NOT NULL COMMENT '消息过期时间',
  UNIQUE KEY `biz_m_i_uniq_key` (`application_name`, `topic`, `tags`, `msg_uniq_key`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=COMPACT;
