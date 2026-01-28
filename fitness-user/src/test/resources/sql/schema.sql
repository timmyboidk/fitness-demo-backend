CREATE TABLE IF NOT EXISTS `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` varchar(255) DEFAULT NULL,
  `nickname` varchar(255) DEFAULT NULL,
  `password` varchar(255) DEFAULT NULL,
  `open_id` varchar(255) DEFAULT NULL,
  `session_key` varchar(255) DEFAULT NULL,
  `difficulty_level` varchar(50) DEFAULT NULL,
  `avatar` varchar(255) DEFAULT NULL,
  `total_score` int(11) DEFAULT '0',
  `total_duration` int(11) DEFAULT '0',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
