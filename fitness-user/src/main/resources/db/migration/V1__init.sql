-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone` VARCHAR(255) COMMENT '加密手机号',
    `nickname` VARCHAR(100),
    `password` VARCHAR(255),
    `open_id` VARCHAR(100) COMMENT '微信 OpenID',
    `session_key` VARCHAR(255) COMMENT '微信 SessionKey',
    `difficulty_level` VARCHAR(20) DEFAULT 'novice',
    `total_score` INT DEFAULT 0 COMMENT '总积分',
    `total_duration` INT DEFAULT 0 COMMENT '总运动时长(秒)',
    `avatar` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_open_id` (`open_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 动作表
CREATE TABLE IF NOT EXISTS `move` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `difficulty` VARCHAR(20),
    `model_url` VARCHAR(500),
    `scoring_config_json` TEXT COMMENT '评分配置JSON',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 用户收藏表
CREATE TABLE IF NOT EXISTS `user_library` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `item_id` VARCHAR(100) NOT NULL,
    `item_type` VARCHAR(50) DEFAULT 'move',
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 课程表
CREATE TABLE IF NOT EXISTS `training_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `name` VARCHAR(100) NOT NULL,
    `difficulty` VARCHAR(20),
    `duration` INT COMMENT '预计时长(分)',
    `cover_url` VARCHAR(500),
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 课程-动作关联表
CREATE TABLE IF NOT EXISTS `session_move_relation` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `session_id` BIGINT NOT NULL,
    `move_id` BIGINT NOT NULL,
    `sort_order` INT DEFAULT 0,
    `duration_seconds` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 初始数据 (Optional, just to have something)
INSERT IGNORE INTO `move` (`name`, `difficulty`, `model_url`, `scoring_config_json`) VALUES
('Deep Squat', 'novice', 'http://example.com/squat.glb', '{"threshold": 0.8}'),
('Push Up', 'novice', 'http://example.com/pushup.glb', '{"threshold": 0.8}');
