CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `phone` VARCHAR(32) NOT NULL UNIQUE,
    `nickname` VARCHAR(64) DEFAULT NULL,
    `password` VARCHAR(128) DEFAULT NULL,
    `open_id` VARCHAR(64) DEFAULT NULL UNIQUE,
    `session_key` VARCHAR(64) DEFAULT NULL,
    `difficulty_level` VARCHAR(32) DEFAULT 'novice',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `user_library` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `item_id` VARCHAR(64) NOT NULL,
    `item_type` VARCHAR(32) NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_item` (`user_id`, `item_id`, `item_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `move` (
    `id` VARCHAR(32) PRIMARY KEY,
    `name` VARCHAR(64) NOT NULL,
    `difficulty` VARCHAR(32) NOT NULL,
    `model_url` VARCHAR(255) NOT NULL,
    `scoring_config_json` JSON NOT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `move` (`id`, `name`, `difficulty`, `model_url`, `scoring_config_json`) VALUES 
('m_squat', 'Squat', 'novice', 'https://oss.example.com/squat.onnx', '{"angleThreshold": 20, "holdTime": 2}');
